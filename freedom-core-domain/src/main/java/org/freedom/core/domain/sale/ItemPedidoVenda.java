package org.freedom.core.domain.sale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidade que representa cada item componente de um Pedido de Venda.
 */
@Entity
@Table(name = "item_pedido_venda", schema = "comercial")
public class ItemPedidoVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pedido_venda_id", nullable = false)
    private Long pedidoVendaId;

    @Column(name = "produto_id", nullable = false)
    private Long produtoId;

    @Column(name = "numero_item", nullable = false)
    private int numeroItem = 1;

    @Column(name = "quantidade", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade = BigDecimal.ONE;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal precoUnitario = BigDecimal.ZERO;

    @Column(name = "valor_desconto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorDesconto = BigDecimal.ZERO;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    // Reforma Tributária 2026 (EC 132/23)
    @Column(name = "aliquota_icms", nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquotaIcms = new BigDecimal("18.00");

    @Column(name = "valor_icms", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorIcms = BigDecimal.ZERO;

    @Column(name = "aliquota_cbs", nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquotaCbs = new BigDecimal("0.90");

    @Column(name = "valor_cbs", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorCbs = BigDecimal.ZERO;

    @Column(name = "aliquota_ibs", nullable = false, precision = 5, scale = 2)
    private BigDecimal aliquotaIbs = new BigDecimal("0.10");

    @Column(name = "valor_ibs", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorIbs = BigDecimal.ZERO;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public ItemPedidoVenda() {
    }

    public ItemPedidoVenda(Long produtoId, int numeroItem, BigDecimal quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.numeroItem = numeroItem;
        this.quantidade = quantidade != null ? quantidade : BigDecimal.ONE;
        this.precoUnitario = precoUnitario != null ? precoUnitario : BigDecimal.ZERO;
        this.valorTotal = this.quantidade.multiply(this.precoUnitario);
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPedidoVendaId() { return pedidoVendaId; }
    public void setPedidoVendaId(Long pedidoVendaId) { this.pedidoVendaId = pedidoVendaId; }

    public Long getProdutoId() { return produtoId; }
    public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

    public int getNumeroItem() { return numeroItem; }
    public void setNumeroItem(int numeroItem) { this.numeroItem = numeroItem; }

    public BigDecimal getQuantidade() { return quantidade; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }

    public BigDecimal getPrecoUnitario() { return precoUnitario; }
    public void setPrecoUnitario(BigDecimal precoUnitario) { this.precoUnitario = precoUnitario; }

    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }

    public BigDecimal getAliquotaIcms() { return aliquotaIcms; }
    public void setAliquotaIcms(BigDecimal aliquotaIcms) { this.aliquotaIcms = aliquotaIcms; }

    public BigDecimal getValorIcms() { return valorIcms; }
    public void setValorIcms(BigDecimal valorIcms) { this.valorIcms = valorIcms; }

    public BigDecimal getAliquotaCbs() { return aliquotaCbs; }
    public void setAliquotaCbs(BigDecimal aliquotaCbs) { this.aliquotaCbs = aliquotaCbs; }

    public BigDecimal getValorCbs() { return valorCbs; }
    public void setValorCbs(BigDecimal valorCbs) { this.valorCbs = valorCbs; }

    public BigDecimal getAliquotaIbs() { return aliquotaIbs; }
    public void setAliquotaIbs(BigDecimal aliquotaIbs) { this.aliquotaIbs = aliquotaIbs; }

    public BigDecimal getValorIbs() { return valorIbs; }
    public void setValorIbs(BigDecimal valorIbs) { this.valorIbs = valorIbs; }

    public Instant getCriadoEm() { return criadoEm; }
}
