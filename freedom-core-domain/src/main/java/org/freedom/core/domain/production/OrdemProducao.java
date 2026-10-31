package org.freedom.core.domain.production;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidade que representa a Ordem de Produção (OP).
 * Mapeada para producao.ordem_producao no PostgreSQL 16+.
 */
@Entity
@Table(name = "ordem_producao", schema = "producao")
public class OrdemProducao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filial_id", nullable = false)
    private Long filialId = 1L;

    @Column(name = "numero_op", length = 30, nullable = false, unique = true)
    private String numeroOp;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "almoxarifado_id", nullable = false)
    private Long almoxarifadoId = 1L;

    @Column(name = "quantidade_planejada", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadePlanejada;

    @Column(name = "quantidade_produzida", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidadeProduzida = BigDecimal.ZERO;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao = LocalDate.now();

    @Column(name = "data_previsao_fim")
    private LocalDate dataPrevisaoFim;

    @Column(name = "data_finalizacao")
    private LocalDate dataFinalizacao;

    @Column(name = "custo_total_fabricacao", nullable = false, precision = 15, scale = 2)
    private BigDecimal custoTotalFabricacao = BigDecimal.ZERO;

    @Column(name = "status_op", length = 20, nullable = false)
    private String statusOp = "PLANEJADA"; // PLANEJADA, EM_PROCESSO, FINALIZADA, CANCELADA

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public OrdemProducao() {
    }

    public OrdemProducao(Long filialId, String numeroOp, Long produtoId, Long almoxarifadoId, BigDecimal quantidadePlanejada) {
        this.filialId = filialId != null ? filialId : 1L;
        this.numeroOp = numeroOp;
        this.produtoId = produtoId;
        this.almoxarifadoId = almoxarifadoId != null ? almoxarifadoId : 1L;
        this.quantidadePlanejada = quantidadePlanejada;
        this.statusOp = "PLANEJADA";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFilialId() { return filialId; }
    public void setFilialId(Long filialId) { this.filialId = filialId; }

    public String getNumeroOp() { return numeroOp; }
    public void setNumeroOp(String numeroOp) { this.numeroOp = numeroOp; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public Long getAlmoxarifadoId() { return almoxarifadoId; }
    public void setAlmoxarifadoId(Long almoxarifadoId) { this.almoxarifadoId = almoxarifadoId; }

    public BigDecimal getQuantidadePlanejada() { return quantidadePlanejada; }
    public void setQuantidadePlanejada(BigDecimal quantidadePlanejada) { this.quantidadePlanejada = quantidadePlanejada; }

    public BigDecimal getQuantidadeProduzida() { return quantidadeProduzida; }
    public void setQuantidadeProduzida(BigDecimal quantidadeProduzida) { this.quantidadeProduzida = quantidadeProduzida; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDate getDataPrevisaoFim() { return dataPrevisaoFim; }
    public void setDataPrevisaoFim(LocalDate dataPrevisaoFim) { this.dataPrevisaoFim = dataPrevisaoFim; }

    public LocalDate getDataFinalizacao() { return dataFinalizacao; }
    public void setDataFinalizacao(LocalDate dataFinalizacao) { this.dataFinalizacao = dataFinalizacao; }

    public BigDecimal getCustoTotalFabricacao() { return custoTotalFabricacao; }
    public void setCustoTotalFabricacao(BigDecimal custoTotalFabricacao) { this.custoTotalFabricacao = custoTotalFabricacao; }

    public String getStatusOp() { return statusOp; }
    public void setStatusOp(String statusOp) { this.statusOp = statusOp; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
