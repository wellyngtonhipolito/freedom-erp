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
 * Entidade que representa uma Venda Rápida de Balcão (NFC-e) no Ponto de Venda.
 */
@Entity
@Table(name = "venda_pdv", schema = "ponto_de_venda")
public class VendaPdv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "caixa_sessao_id", nullable = false)
    private Long caixaSessaoId;

    @Column(name = "numero_cupom", length = 30, nullable = false)
    private String numeroCupom;

    @Column(name = "data_venda", nullable = false)
    private Instant dataVenda = Instant.now();

    @Column(name = "valor_subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorSubtotal = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "valor_pago", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "valor_troco", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTroco = BigDecimal.ZERO;

    @Column(name = "forma_pagamento", length = 20, nullable = false)
    private String formaPagamento = "DINHEIRO";

    // Reforma 2026
    @Column(name = "valor_cbs", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorCbs = BigDecimal.ZERO;

    @Column(name = "valor_ibs", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorIbs = BigDecimal.ZERO;

    @Column(name = "chave_nfce", length = 44)
    private String chaveNfce;

    @Column(name = "status_venda", length = 20, nullable = false)
    private String statusVenda = "CONCLUIDA";

    public VendaPdv() {
    }

    public VendaPdv(Long caixaSessaoId, String numeroCupom, BigDecimal valorTotal, BigDecimal valorPago, String formaPagamento) {
        this.caixaSessaoId = caixaSessaoId;
        this.numeroCupom = numeroCupom;
        this.valorSubtotal = valorTotal;
        this.valorTotal = valorTotal;
        this.valorPago = valorPago;
        this.valorTroco = valorPago.compareTo(valorTotal) > 0 ? valorPago.subtract(valorTotal) : BigDecimal.ZERO;
        this.formaPagamento = formaPagamento;
        this.statusVenda = "CONCLUIDA";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCaixaSessaoId() { return caixaSessaoId; }
    public void setCaixaSessaoId(Long caixaSessaoId) { this.caixaSessaoId = caixaSessaoId; }

    public String getNumeroCupom() { return numeroCupom; }
    public void setNumeroCupom(String numeroCupom) { this.numeroCupom = numeroCupom; }

    public Instant getDataVenda() { return dataVenda; }
    public void setDataVenda(Instant dataVenda) { this.dataVenda = dataVenda; }

    public BigDecimal getValorSubtotal() { return valorSubtotal; }
    public void setValorSubtotal(BigDecimal valorSubtotal) { this.valorSubtotal = valorSubtotal; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getValorPago() { return valorPago; }
    public void setValorPago(BigDecimal valorPago) { this.valorPago = valorPago; }

    public BigDecimal getValorTroco() { return valorTroco; }
    public void setValorTroco(BigDecimal valorTroco) { this.valorTroco = valorTroco; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public BigDecimal getValorCbs() { return valorCbs; }
    public void setValorCbs(BigDecimal valorCbs) { this.valorCbs = valorCbs; }

    public BigDecimal getValorIbs() { return valorIbs; }
    public void setValorIbs(BigDecimal valorIbs) { this.valorIbs = valorIbs; }

    public String getChaveNfce() { return chaveNfce; }
    public void setChaveNfce(String chaveNfce) { this.chaveNfce = chaveNfce; }

    public String getStatusVenda() { return statusVenda; }
    public void setStatusVenda(String statusVenda) { this.statusVenda = statusVenda; }
}

