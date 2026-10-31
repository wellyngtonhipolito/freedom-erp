package org.freedom.core.domain.finance;

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
 * Entidade que representa os Títulos a Receber (antiga Fnreceber).
 * Mapeada para a tabela financeiro.conta_receber no PostgreSQL 16+.
 */
@Entity
@Table(name = "conta_receber", schema = "financeiro")
public class ContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filial_id", nullable = false)
    private Long filialId;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "numero_documento", length = 30, nullable = false)
    private String numeroDocumento;

    @Column(name = "parcela", nullable = false)
    private Integer parcela = 1;

    @Column(name = "total_parcelas", nullable = false)
    private Integer totalParcelas = 1;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_liquidacao")
    private LocalDate dataLiquidacao;

    @Column(name = "valor_nominal", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorNominal;

    @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_juros", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorJuros = BigDecimal.ZERO;

    @Column(name = "valor_multa", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "saldo_devedor", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDevedor;

    @Column(name = "status_titulo", length = 20, nullable = false)
    private String statusTitulo = "PENDENTE"; // PENDENTE, QUITADO_PARCIAL, QUITADO, CANCELADO

    @Column(name = "tipo_cobranca", length = 20, nullable = false)
    private String tipoCobranca = "BOLETO";

    @Column(name = "chave_transacao_pix", length = 100)
    private String chaveTransacaoPix;

    @Column(name = "historico", columnDefinition = "TEXT")
    private String historico;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public ContaReceber() {
    }

    public ContaReceber(Long filialId, Long clienteId, String numeroDocumento, LocalDate dataEmissao, LocalDate dataVencimento, BigDecimal valorNominal) {
        this.filialId = filialId;
        this.clienteId = clienteId;
        this.numeroDocumento = numeroDocumento;
        this.dataEmissao = dataEmissao;
        this.dataVencimento = dataVencimento;
        this.valorNominal = valorNominal;
        this.saldoDevedor = valorNominal;
        this.statusTitulo = "PENDENTE";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFilialId() { return filialId; }
    public void setFilialId(Long filialId) { this.filialId = filialId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public Integer getParcela() { return parcela; }
    public void setParcela(Integer parcela) { this.parcela = parcela; }

    public Integer getTotalParcelas() { return totalParcelas; }
    public void setTotalParcelas(Integer totalParcelas) { this.totalParcelas = totalParcelas; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDate getDataVencimento() { return dataVencimento; }
    public void setDataVencimento(LocalDate dataVencimento) { this.dataVencimento = dataVencimento; }

    public LocalDate getDataLiquidacao() { return dataLiquidacao; }
    public void setDataLiquidacao(LocalDate dataLiquidacao) { this.dataLiquidacao = dataLiquidacao; }

    public BigDecimal getValorNominal() { return valorNominal; }
    public void setValorNominal(BigDecimal valorNominal) { this.valorNominal = valorNominal; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public BigDecimal getValorJuros() { return valorJuros; }
    public void setValorJuros(BigDecimal valorJuros) { this.valorJuros = valorJuros; }

    public BigDecimal getValorMulta() { return valorMulta; }
    public void setValorMulta(BigDecimal valorMulta) { this.valorMulta = valorMulta; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public BigDecimal getSaldoDevedor() { return saldoDevedor; }
    public void setSaldoDevedor(BigDecimal saldoDevedor) { this.saldoDevedor = saldoDevedor; }

    public String getStatusTitulo() { return statusTitulo; }
    public void setStatusTitulo(String statusTitulo) { this.statusTitulo = statusTitulo; }

    public String getTipoCobranca() { return tipoCobranca; }
    public void setTipoCobranca(String tipoCobranca) { this.tipoCobranca = tipoCobranca; }

    public String getChaveTransacaoPix() { return chaveTransacaoPix; }
    public void setChaveTransacaoPix(String chaveTransacaoPix) { this.chaveTransacaoPix = chaveTransacaoPix; }

    public String getHistorico() { return historico; }
    public void setHistorico(String historico) { this.historico = historico; }

    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}

