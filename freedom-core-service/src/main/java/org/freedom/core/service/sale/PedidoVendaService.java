package org.freedom.core.service.sale;

import org.freedom.core.domain.finance.ContaReceber;
import org.freedom.core.domain.sale.ItemPedidoVenda;
import org.freedom.core.domain.sale.PedidoVenda;
import org.freedom.core.domain.tax.TributacaoCalculada;
import org.freedom.core.service.finance.ContaReceberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de aplicação para Gestão de Pedidos de Venda e Orçamentos:
 * - Totalização dos itens
 * - Apuração automática dos tributos da Reforma 2026 (CBS 0.9% e IBS 0.1%)
 * - Aprovação e integração direta gerando título a receber no Financeiro
 */
public class PedidoVendaService {

    private static final Logger log = LoggerFactory.getLogger(PedidoVendaService.class);

    private final PedidoVendaRepository repository;
    private final ContaReceberService financeiroService;

    public PedidoVendaService() {
        this.repository = new PedidoVendaRepository();
        this.financeiroService = new ContaReceberService();
    }

    public PedidoVendaService(PedidoVendaRepository repository, ContaReceberService financeiroService) {
        this.repository = repository;
        this.financeiroService = financeiroService;
    }

    /**
     * Calcula tributos e salva o pedido de venda.
     */
    public PedidoVenda salvarPedido(PedidoVenda pedido, List<ItemPedidoVenda> itens) {
        if (pedido.getClienteId() == null) {
            throw new IllegalArgumentException("O cliente é obrigatório para abrir um pedido de venda.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("O pedido deve conter pelo menos um item.");
        }

        BigDecimal valorItens = BigDecimal.ZERO;
        BigDecimal totalCbs = BigDecimal.ZERO;
        BigDecimal totalIbs = BigDecimal.ZERO;
        BigDecimal totalIcms = BigDecimal.ZERO;

        for (ItemPedidoVenda item : itens) {
            BigDecimal subtotal = item.getQuantidade().multiply(item.getPrecoUnitario()).subtract(item.getValorDesconto());
            item.setValorTotal(subtotal.setScale(2, RoundingMode.HALF_UP));
            valorItens = valorItens.add(item.getValorTotal());

            // Calcula tributos da Reforma Tributária 2026 para o item
            TributacaoCalculada trib = TributacaoCalculada.calcularParaAno2026(
                item.getValorTotal(),
                item.getAliquotaIcms().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
            );

            item.setValorCbs(trib.valorCbs());
            item.setValorIbs(trib.valorIbs());
            item.setValorIcms(trib.valorIcms());

            totalCbs = totalCbs.add(trib.valorCbs());
            totalIbs = totalIbs.add(trib.valorIbs());
            totalIcms = totalIcms.add(trib.valorIcms());
        }

        pedido.setValorItens(valorItens);
        BigDecimal totalPedido = valorItens.add(pedido.getValorFrete()).subtract(pedido.getValorDesconto());
        pedido.setValorTotal(totalPedido.setScale(2, RoundingMode.HALF_UP));

        pedido.setValorCbs(totalCbs);
        pedido.setValorIbs(totalIbs);
        pedido.setValorIcms(totalIcms);

        return repository.salvarPedido(pedido, itens);
    }

    /**
     * Aprova o pedido e gera automaticamente o título no Contas a Receber (Financeiro).
     */
    public void aprovarPedido(PedidoVenda pedido) {
        pedido.setStatusPedido("APROVADO");

        // Integração Financeira: Criação automática de ContaReceber
        ContaReceber titulo = new ContaReceber(
            pedido.getFilialId(),
            pedido.getClienteId(),
            "PED-" + pedido.getNumeroPedido(),
            pedido.getDataEmissao(),
            pedido.getDataPrevisaoEntrega() != null ? pedido.getDataPrevisaoEntrega() : LocalDate.now().plusDays(30),
            pedido.getValorTotal()
        );
        titulo.setTipoCobranca(pedido.getFormaPagamento());
        titulo.setHistorico("Gerado automaticamente pelo Pedido de Venda #" + pedido.getNumeroPedido());

        financeiroService.emitirTitulo(titulo);
        log.info("Pedido #{} aprovado com sucesso! Título financeiro emitido.", pedido.getNumeroPedido());
    }

    public CompletableFuture<List<PedidoVenda>> listarPedidosAsync() {
        return CompletableFuture.supplyAsync(() -> repository.listarTodos(), runnable -> Thread.ofVirtual().start(runnable));
    }
}

