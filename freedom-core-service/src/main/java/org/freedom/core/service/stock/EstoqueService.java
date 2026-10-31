package org.freedom.core.service.stock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço de aplicação para Gestão de Estoque e Compras:
 * - Entradas de mercadoria com recálculo de custo médio ponderado
 * - Saídas por venda com verificação de disponibilidade
 * - Consultas assíncronas em Virtual Threads
 */
public class EstoqueService {

    private static final Logger log = LoggerFactory.getLogger(EstoqueService.class);

    private final EstoqueRepository repository;

    public EstoqueService() {
        this.repository = new EstoqueRepository();
    }

    public EstoqueService(EstoqueRepository repository) {
        this.repository = repository;
    }

    public void darEntradaEstoque(Long produtoId, BigDecimal quantidade, BigDecimal custoUnitario, String docRef, String obs) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade de entrada deve ser positiva.");
        }
        if (custoUnitario == null || custoUnitario.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("O custo unitário não pode ser negativo.");
        }

        repository.registrarMovimento(1L, produtoId, "ENTRADA", "COMPRA", docRef, quantidade, custoUnitario, obs);
    }

    public void darSaidaEstoque(Long produtoId, BigDecimal quantidade, String docRef, String obs) {
        if (quantidade == null || quantidade.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A quantidade de saída deve ser positiva.");
        }

        repository.registrarMovimento(1L, produtoId, "SAIDA", "VENDA", docRef, quantidade, BigDecimal.ZERO, obs);
    }

    public CompletableFuture<List<EstoqueRepository.SaldoVisualizacaoDto>> listarSaldosAsync() {
        return CompletableFuture.supplyAsync(() -> repository.listarSaldosGerais(), runnable -> Thread.ofVirtual().start(runnable));
    }
}

