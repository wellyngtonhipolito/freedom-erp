package org.freedom.core.service.production;

import org.freedom.core.domain.production.OrdemProducao;
import org.freedom.core.service.stock.EstoqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de aplicação para Gestão de Produção & PCP:
 * - Abertura de OP
 * - Finalização com entrada automática do produto acabado no estoque
 */
public class OrdemProducaoService {

    private static final Logger log = LoggerFactory.getLogger(OrdemProducaoService.class);

    private final OrdemProducaoRepository repository;
    private final EstoqueService estoqueService;

    public OrdemProducaoService() {
        this.repository = new OrdemProducaoRepository();
        this.estoqueService = new EstoqueService();
    }

    public OrdemProducaoService(OrdemProducaoRepository repository, EstoqueService estoqueService) {
        this.repository = repository;
        this.estoqueService = estoqueService;
    }

    public OrdemProducao abrirOrdemProducao(OrdemProducao op) {
        if (op.getProdutoId() == null) {
            throw new IllegalArgumentException("O produto a ser fabricado é obrigatório.");
        }
        if (op.getQuantidadePlanejada() == null || op.getQuantidadePlanejada().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade planejada deve ser positiva.");
        }

        op.setStatusOp("PLANEJADA");
        return repository.salvar(op);
    }

    /**
     * Finaliza a OP, marcando como FINALIZADA e dando entrada física no estoque do produto acabado.
     */
    public void finalizarOrdemProducao(OrdemProducao op, BigDecimal quantidadeProduzida, BigDecimal custoFabricacaoUnitario) {
        if (quantidadeProduzida == null || quantidadeProduzida.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade produzida deve ser maior que zero.");
        }

        op.setStatusOp("FINALIZADA");
        op.setQuantidadeProduzida(quantidadeProduzida);
        op.setDataFinalizacao(LocalDate.now());

        // Entrada do produto acabado no Almoxarifado Central
        estoqueService.darEntradaEstoque(
            op.getProdutoId(),
            quantidadeProduzida,
            custoFabricacaoUnitario != null ? custoFabricacaoUnitario : BigDecimal.ZERO,
            "OP-" + op.getNumeroOp(),
            "Entrada por apontamento de Ordem de Produção #" + op.getNumeroOp()
        );

        log.info("Ordem de Produção #{} finalizada. Produto fabricado deu entrada no estoque.", op.getNumeroOp());
    }

    public CompletableFuture<List<OrdemProducaoRepository.OrdemProducaoVisualizacaoDto>> listarOrdensAsync() {
        return CompletableFuture.supplyAsync(() -> repository.listarTodas(), runnable -> Thread.ofVirtual().start(runnable));
    }
}

