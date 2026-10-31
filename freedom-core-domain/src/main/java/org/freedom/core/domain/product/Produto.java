package org.freedom.core.domain.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

/**
 * Entidade que representa Produtos, Mercadorias e Serviços no Freedom ERP.
 * Mapeada para a tabela comercial.produto no PostgreSQL 16+.
 */
@Entity
@Table(name = "produto", schema = "comercial")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filial_id", nullable = false)
    private Long filialId = 1L;

    @Column(name = "codigo_referencia", length = 30)
    private String codigoReferencia;

    @Column(name = "codigo_barras_ean", length = 14)
    private String codigoBarrasEan;

    @Column(name = "descricao", length = 120, nullable = false)
    private String descricao;

    @Column(name = "unidade_medida", length = 6, nullable = false)
    private String unidadeMedida = "UN";

    @Column(name = "preco_custo", nullable = false, precision = 15, scale = 4)
    private BigDecimal precoCusto = BigDecimal.ZERO;

    @Column(name = "preco_venda", nullable = false, precision = 15, scale = 2)
    private BigDecimal precoVenda = BigDecimal.ZERO;

    @Column(name = "markup_percentual", nullable = false, precision = 7, scale = 2)
    private BigDecimal markupPercentual = BigDecimal.ZERO;

    // Regras Fiscais & Reforma Tributária 2026
    @Column(name = "ncm", length = 8, nullable = false)
    private String ncm;

    @Column(name = "cest", length = 7)
    private String cest;

    @Column(name = "origem_mercadoria", nullable = false)
    private int origemMercadoria = 0; // 0: Nacional

    @Column(name = "tipo_item_sped", length = 2, nullable = false)
    private String tipoItemSped = "00"; // 00: Revenda

    @Column(name = "sujeito_imposto_seletivo", nullable = false)
    private boolean sujeitoImpostoSeletivo = false;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public Produto() {
    }

    public Produto(Long filialId, String codigoReferencia, String descricao, String unidadeMedida, BigDecimal precoCusto, BigDecimal precoVenda, String ncm) {
        this.filialId = filialId != null ? filialId : 1L;
        this.codigoReferencia = codigoReferencia;
        this.descricao = descricao;
        this.unidadeMedida = unidadeMedida != null ? unidadeMedida : "UN";
        this.precoCusto = precoCusto != null ? precoCusto : BigDecimal.ZERO;
        this.precoVenda = precoVenda != null ? precoVenda : BigDecimal.ZERO;
        this.ncm = ncm;
        recalcularMarkup();
    }

    /**
     * Recalcula o markup a partir do preço de custo e venda:
     * Markup = ((Venda - Custo) / Custo) * 100
     */
    public void recalcularMarkup() {
        if (precoCusto != null && precoCusto.compareTo(BigDecimal.ZERO) > 0 && precoVenda != null) {
            BigDecimal lucro = precoVenda.subtract(precoCusto);
            this.markupPercentual = lucro.divide(precoCusto, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        } else {
            this.markupPercentual = BigDecimal.ZERO;
        }
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFilialId() { return filialId; }
    public void setFilialId(Long filialId) { this.filialId = filialId; }

    public String getCodigoReferencia() { return codigoReferencia; }
    public void setCodigoReferencia(String codigoReferencia) { this.codigoReferencia = codigoReferencia; }

    public String getCodigoBarrasEan() { return codigoBarrasEan; }
    public void setCodigoBarrasEan(String codigoBarrasEan) { this.codigoBarrasEan = codigoBarrasEan; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }

    public BigDecimal getPrecoCusto() { return precoCusto; }
    public void setPrecoCusto(BigDecimal precoCusto) {
        this.precoCusto = precoCusto;
        recalcularMarkup();
    }

    public BigDecimal getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
        recalcularMarkup();
    }

    public BigDecimal getMarkupPercentual() { return markupPercentual; }
    public void setMarkupPercentual(BigDecimal markupPercentual) { this.markupPercentual = markupPercentual; }

    public String getNcm() { return ncm; }
    public void setNcm(String ncm) { this.ncm = ncm; }

    public String getCest() { return cest; }
    public void setCest(String cest) { this.cest = cest; }

    public int getOrigemMercadoria() { return origemMercadoria; }
    public void setOrigemMercadoria(int origemMercadoria) { this.origemMercadoria = origemMercadoria; }

    public String getTipoItemSped() { return tipoItemSped; }
    public void setTipoItemSped(String tipoItemSped) { this.tipoItemSped = tipoItemSped; }

    public boolean isSujeitoImpostoSeletivo() { return sujeitoImpostoSeletivo; }
    public void setSujeitoImpostoSeletivo(boolean sujeitoImpostoSeletivo) { this.sujeitoImpostoSeletivo = sujeitoImpostoSeletivo; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}
