package org.freedom.core.domain.pos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidade que representa uma Sessão / Turno de Caixa do Ponto de Venda.
 */
@Entity
@Table(name = "caixa_sessao", schema = "ponto_de_venda")
public class CaixaSessao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filial_id", nullable = false)
    private Long filialId = 1L;

    @Column(name = "numero_terminal", nullable = false)
    private int numeroTerminal = 1;

    @Column(name = "operador_nome", length = 60, nullable = false)
    private String operadorNome;

    @Column(name = "data_abertura", nullable = false)
    private Instant dataAbertura = Instant.now();

    @Column(name = "data_fechamento")
    private Instant dataFechamento;

    @Column(name = "valor_fundo_troco", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFundoTroco = BigDecimal.ZERO;

    @Column(name = "total_entradas", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalEntradas = BigDecimal.ZERO;

    @Column(name = "total_saidas_sangria", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalSaidasSangria = BigDecimal.ZERO;

    @Column(name = "saldo_final_fechamento", precision = 15, scale = 2)
    private BigDecimal saldoFinalFechamento;

    @Column(name = "status_caixa", length = 20, nullable = false)
    private String statusCaixa = "ABERTO"; // ABERTO, FECHADO

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    public CaixaSessao() {
    }

    public CaixaSessao(Long filialId, int numeroTerminal, String operadorNome, BigDecimal valorFundoTroco) {
        this.filialId = filialId != null ? filialId : 1L;
        this.numeroTerminal = numeroTerminal;
        this.operadorNome = operadorNome;
        this.valorFundoTroco = valorFundoTroco != null ? valorFundoTroco : BigDecimal.ZERO;
        this.statusCaixa = "ABERTO";
    }

    public BigDecimal getSaldoAtualCalculado() {
        return valorFundoTroco.add(totalEntradas).subtract(totalSaidasSangria);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFilialId() { return filialId; }
    public void setFilialId(Long filialId) { this.filialId = filialId; }

    public int getNumeroTerminal() { return numeroTerminal; }
    public void setNumeroTerminal(int numeroTerminal) { this.numeroTerminal = numeroTerminal; }

    public String getOperadorNome() { return operadorNome; }
    public void setOperadorNome(String operadorNome) { this.operadorNome = operadorNome; }

    public Instant getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(Instant dataAbertura) { this.dataAbertura = dataAbertura; }

    public Instant getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(Instant dataFechamento) { this.dataFechamento = dataFechamento; }

    public BigDecimal getValorFundoTroco() { return valorFundoTroco; }
    public void setValorFundoTroco(BigDecimal valorFundoTroco) { this.valorFundoTroco = valorFundoTroco; }

    public BigDecimal getTotalEntradas() { return totalEntradas; }
    public void setTotalEntradas(BigDecimal totalEntradas) { this.totalEntradas = totalEntradas; }

    public BigDecimal getTotalSaidasSangria() { return totalSaidasSangria; }
    public void setTotalSaidasSangria(BigDecimal totalSaidasSangria) { this.totalSaidasSangria = totalSaidasSangria; }

    public BigDecimal getSaldoFinalFechamento() { return saldoFinalFechamento; }
    public void setSaldoFinalFechamento(BigDecimal saldoFinalFechamento) { this.saldoFinalFechamento = saldoFinalFechamento; }

    public String getStatusCaixa() { return statusCaixa; }
    public void setStatusCaixa(String statusCaixa) { this.statusCaixa = statusCaixa; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
}
