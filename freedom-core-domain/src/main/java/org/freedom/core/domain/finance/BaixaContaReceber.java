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
 * Registro de Baixa / Liquidação de Título a Receber.
 */
@Entity
@Table(name = "baixa_conta_receber", schema = "financeiro")
public class BaixaContaReceber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conta_receber_id", nullable = false)
    private Long contaReceberId;

    @Column(name = "conta_bancaria_id")
    private Long contaBancariaId;

    @Column(name = "data_baixa", nullable = false)
    private LocalDate dataBaixa;

    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPago;

    @Column(name = "valor_juros", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorJuros = BigDecimal.ZERO;

    @Column(name = "valor_multa", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorMulta = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "forma_pagamento", length = 20, nullable = false)
    private String formaPagamento = "PIX";

    @Column(name = "comprovante_transacao", length = 100)
    private String comprovanteTransacao;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public BaixaContaReceber() {
    }

    public BaixaContaReceber(Long contaReceberId, Long contaBancariaId, LocalDate dataBaixa, BigDecimal valorPago, String formaPagamento) {
        this.contaReceberId = contaReceberId;
        this.contaBancariaId = contaBancariaId;
        this.dataBaixa = dataBaixa;
        this.valorPago = valorPago;
        this.formaPagamento = formaPagamento;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getContaReceberId() { return contaReceberId; }
    public void setContaReceberId(Long contaReceberId) { this.contaReceberId = contaReceberId; }

    public Long getContaBancariaId() { return contaBancariaId; }
    public void setContaBancariaId(Long contaBancariaId) { this.contaBancariaId = contaBancariaId; }

    public LocalDate getDataBaixa() { return dataBaixa; }
    public void setDataBaixa(LocalDate dataBaixa) { this.dataBaixa = dataBaixa; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public BigDecimal getValorJuros() { return valorJuros; }
    public void setValorJuros(BigDecimal valorJuros) { this.valorJuros = valorJuros; }

    public BigDecimal getValorMulta() { return valorMulta; }
    public void setValorMulta(BigDecimal valorMulta) { this.valorMulta = valorMulta; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getComprovanteTransacao() { return comprovanteTransacao; }
    public void setComprovanteTransacao(String comprovanteTransacao) { this.comprovanteTransacao = comprovanteTransacao; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Instant getCriadoEm() { return criadoEm; }
}

