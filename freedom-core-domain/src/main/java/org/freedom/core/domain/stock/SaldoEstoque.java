package org.freedom.core.domain.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidade que representa a posição e saldo atual de estoque de um produto em um almoxarifado.
 */
@Entity
@Table(name = "saldo_estoque", schema = "estoque_compras")
public class SaldoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "almoxarifado_id", nullable = false)
    private Long almoxarifadoId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "quantidade_atual", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeAtual = BigDecimal.ZERO;

    @Column(name = "quantidade_reservada", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeReservada = BigDecimal.ZERO;

    @Column(name = "custo_medio_ponderado", nullable = false, precision = 15, scale = 4)
    private BigDecimal custoMedioPonderado = BigDecimal.ZERO;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public SaldoEstoque() {
    }

    public SaldoEstoque(Long almoxarifadoId, Long produtoId, BigDecimal quantidadeAtual, BigDecimal custoMedioPonderado) {
        this.almoxarifadoId = almoxarifadoId;
        this.produtoId = produtoId;
        this.quantidadeAtual = quantidadeAtual != null ? quantidadeAtual : BigDecimal.ZERO;
        this.custoMedioPonderado = custoMedioPonderado != null ? custoMedioPonderado : BigDecimal.ZERO;
    }

    public BigDecimal getSaldoDisponivel() {
        return quantidadeAtual.subtract(quantidadeReservada);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAlmoxarifadoId() { return almoxarifadoId; }
    public void setAlmoxarifadoId(Long almoxarifadoId) { this.almoxarifadoId = almoxarifadoId; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public BigDecimal getQuantidadeAtual() { return quantidadeAtual; }
    public void setQuantidadeAtual(BigDecimal quantidadeAtual) { this.quantidadeAtual = quantidadeAtual; }

    public BigDecimal getQuantidadeReservada() { return quantidadeReservada; }
    public void setQuantidadeReservada(BigDecimal quantidadeReservada) { this.quantidadeReservada = quantidadeReservada; }

    public BigDecimal getCustoMedioPonderado() { return custoMedioPonderado; }
    public void setCustoMedioPonderado(BigDecimal custoMedioPonderado) { this.custoMedioPonderado = custoMedioPonderado; }

    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}

