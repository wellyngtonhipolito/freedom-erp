package org.freedom.core.service.pos;

import org.freedom.core.domain.pos.CaixaSessao;
import org.freedom.core.domain.pos.VendaPdv;
import org.freedom.core.domain.tax.TributacaoCalculada;
import org.freedom.core.service.stock.EstoqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de aplicação do Ponto de Venda (PDV):
 * - Abertura e validação de Caixa
 * - Venda rápida de balcão com cálculo de troco e tributos 2026
 * - Baixa automática de estoque
 */
public class PdvService {

    private static final Logger log = LoggerFactory.getLogger(PdvService.class);

    private final PdvRepository repository;
    private final EstoqueService estoqueService;

    public PdvService() {
        this.repository = new PdvRepository();
        this.estoqueService = new EstoqueService();
    }

    public PdvService(PdvRepository repository, EstoqueService estoqueService) {
        this.repository = repository;
        this.estoqueService = estoqueService;
    }

    public CaixaSessao abrirCaixa(int terminal, String operador, BigDecimal fundoTroco) {
        Optional<CaixaSessao> aberto = repository.buscarCaixaAberto(terminal);
        if (aberto.isPresent()) {
            throw new IllegalStateException("Já existe uma sessão de caixa aberta para este terminal (#" + terminal + ").");
        }

        CaixaSessao novaSessao = new CaixaSessao(1L, terminal, operador, fundoTroco);
        return repository.abrirCaixa(novaSessao);
    }

    public Optional<CaixaSessao> obterCaixaAberto(int terminal) {
        return repository.buscarCaixaAberto(terminal);
    }

    /**
     * Finaliza a venda rápida de balcão:
     * - Calcula troco
     * - Aplica CBS 0.9% e IBS 0.1% da Reforma 2026
     * - Registra no caixa
     */
    public VendaPdv registrarVendaRapida(Long caixaSessaoId, String numeroCupom, BigDecimal valorTotal,
                                        BigDecimal valorPago, String formaPagamento,
                                        List<Long> produtosVendidosIds) {

        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor total da venda deve ser maior que zero.");
        }
        if (valorPago == null || valorPago.compareTo(valorTotal) < 0) {
            throw new IllegalArgumentException("O valor pago é insuficiente para cobrir o total da venda.");
        }

        BigDecimal troco = valorPago.subtract(valorTotal);

        // Apuração tributária da Reforma 2026
        BigDecimal aliquotaIcmsPadrao = new BigDecimal("0.18");
        TributacaoCalculada tributos = TributacaoCalculada.calcularParaAno2026(valorTotal, aliquotaIcmsPadrao);

        VendaPdv venda = new VendaPdv(caixaSessaoId, numeroCupom, valorTotal, valorPago, formaPagamento);
        venda.setValorTroco(troco.setScale(2, RoundingMode.HALF_UP));
        venda.setValorCbs(tributos.valorCbs());
        venda.setValorIbs(tributos.valorIbs());

        repository.registrarVenda(venda);

        // Baixa automática de estoque para os produtos vendidos
        if (produtosVendidosIds != null) {
            for (Long prodId : produtosVendidosIds) {
                try {
                    estoqueService.darSaidaEstoque(prodId, BigDecimal.ONE, "NFCe-" + numeroCupom, "Venda PDV Balcão");
                } catch (Exception ex) {
                    log.warn("Alerta de estoque ao baixar produto #{}: {}", prodId, ex.getMessage());
                }
            }
        }

        log.info("Venda de balcão concluída! Cupom #{}, Total: R$ {}, Troco: R$ {}", numeroCupom, valorTotal, troco);
        return venda;
    }

    public CompletableFuture<List<VendaPdv>> listarVendasAsync(Long caixaSessaoId) {
        return CompletableFuture.supplyAsync(() -> repository.listarUltimasVendas(caixaSessaoId), runnable -> Thread.ofVirtual().start(runnable));
    }
}
