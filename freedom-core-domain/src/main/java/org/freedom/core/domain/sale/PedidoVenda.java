package org.freedom.core.domain.sale;

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
 * Entidade que representa o Pedido de Venda ou Orçamento.
 * Mapeada para a tabela comercial.pedido_venda no PostgreSQL 16+.
 */
@Entity
@Table(name = "pedido_venda", schema = "comercial")
public class PedidoVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filial_id", nullable = false)
    private Long filialId = 1L;

    @Column(name = "cliente_id", nullable = false)
    private Long clienteId;

    @Column(name = "numero_pedido", length = 30, nullable = false)
    private String numeroPedido;

    @Column(name = "data_emissao", nullable = false)
    private LocalDate dataEmissao = LocalDate.now();

    @Column(name = "data_previsao_entrega")
    private LocalDate dataPrevisaoEntrega;

    @Column(name = "valor_itens", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorItens = BigDecimal.ZERO;

    @Column(name = "valor_frete", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorFrete = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    // Reforma Tributária 2026 (EC 132/23)
    @Column(name = "valor_icms", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorIcms = BigDecimal.ZERO;

    @Column(name = "valor_cbs", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorCbs = BigDecimal.ZERO;

    @Column(name = "valor_ibs", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorIbs = BigDecimal.ZERO;

    @Column(name = "valor_imposto_seletivo", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorImpostoSeletivo = BigDecimal.ZERO;

    @Column(name = "status_pedido", length = 20, nullable = false)
    private String statusPedido = "ORCAMENTO"; // ORCAMENTO, APROVADO, FATURADO, CANCELADO

    @Column(name = "forma_pagamento", length = 20, nullable = false)
    private String formaPagamento = "PIX";

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public PedidoVenda() {
    }

    public PedidoVenda(Long filialId, Long clienteId, String numeroPedido) {
        this.filialId = filialId != null ? filialId : 1L;
        this.clienteId = clienteId;
        this.numeroPedido = numeroPedido;
        this.statusPedido = "ORCAMENTO";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFilialId() { return filialId; }
    public void setFilialId(Long filialId) { this.filialId = filialId; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }

    public LocalDate getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(LocalDate dataEmissao) { this.dataEmissao = dataEmissao; }

    public LocalDate getDataPrevisaoEntrega() { return dataPrevisaoEntrega; }
    public void setDataPrevisaoEntrega(LocalDate dataPrevisaoEntrega) { this.dataPrevisaoEntrega = dataPrevisaoEntrega; }

    public BigDecimal getValorItens() { return valorItens; }
    public void setValorItens(BigDecimal valorItens) { this.valorItens = valorItens; }

    public BigDecimal getValorFrete() { return valorFrete; }
    public void setValorFrete(BigDecimal valorFrete) { this.valorFrete = valorFrete; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getValorIcms() { return valorIcms; }
    public void setValorIcms(BigDecimal valorIcms) { this.valorIcms = valorIcms; }

    public BigDecimal getValorCbs() { return valorCbs; }
    public void setValorCbs(BigDecimal valorCbs) { this.valorCbs = valorCbs; }

    public BigDecimal getValorIbs() { return valorIbs; }
    public void setValorIbs(BigDecimal valorIbs) { this.valorIbs = valorIbs; }

    public BigDecimal getValorImpostoSeletivo() { return valorImpostoSeletivo; }
    public void setValorImpostoSeletivo(BigDecimal valorImpostoSeletivo) { this.valorImpostoSeletivo = valorImpostoSeletivo; }

    public String getStatusPedido() { return statusPedido; }
    public void setStatusPedido(String statusPedido) { this.statusPedido = statusPedido; }

    public String getFormaPagamento() { return formaPagamento; }
    public void setFormaPagamento(String formaPagamento) { this.formaPagamento = formaPagamento; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
