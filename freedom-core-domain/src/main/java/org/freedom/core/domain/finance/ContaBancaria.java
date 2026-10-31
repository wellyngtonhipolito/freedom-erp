package org.freedom.core.domain.finance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidade que representa uma Conta Bancária ou Caixa Físico da empresa.
 */
@Entity
@Table(name = "conta_bancaria", schema = "financeiro")
public class ContaBancaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "filial_id", nullable = false)
    private Long filialId;

    @Column(name = "banco_id", nullable = false)
    private Long bancoId;

    @Column(name = "descricao", length = 60, nullable = false)
    private String descricao;

    @Column(name = "agencia", length = 10, nullable = false)
    private String agencia;

    @Column(name = "digito_agencia", length = 2)
    private String digitoAgencia;

    @Column(name = "numero_conta", length = 20, nullable = false)
    private String numeroConta;

    @Column(name = "digito_conta", length = 2)
    private String digitoConta;

    @Column(name = "tipo_conta", length = 20, nullable = false)
    private String tipoConta = "CORRENTE";

    @Column(name = "saldo_atual", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoAtual = BigDecimal.ZERO;

    @Column(name = "chave_pix", length = 100)
    private String chavePix;

    @Column(name = "tipo_chave_pix", length = 20)
    private String tipoChavePix;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    public ContaBancaria() {
    }

    public ContaBancaria(Long filialId, Long bancoId, String descricao, String agencia, String numeroConta, BigDecimal saldoInicial) {
        this.filialId = filialId;
        this.bancoId = bancoId;
        this.descricao = descricao;
        this.agencia = agencia;
        this.numeroConta = numeroConta;
        this.saldoAtual = saldoInicial != null ? saldoInicial : BigDecimal.ZERO;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFilialId() { return filialId; }
    public void setFilialId(Long filialId) { this.filialId = filialId; }

    public Long getBancoId() { return bancoId; }
    public void setBancoId(Long bancoId) { this.bancoId = bancoId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getAgencia() { return agencia; }
    public void setAgencia(String agencia) { this.agencia = agencia; }

    public String getDigitoAgencia() { return digitoAgencia; }
    public void setDigitoAgencia(String digitoAgencia) { this.digitoAgencia = digitoAgencia; }

    public String getNumeroConta() { return numeroConta; }
    public void setNumeroConta(String numeroConta) { this.numeroConta = numeroConta; }

    public String getDigitoConta() { return digitoConta; }
    public void setDigitoConta(String digitoConta) { this.digitoConta = digitoConta; }

    public String getTipoConta() { return tipoConta; }
    public void setTipoConta(String tipoConta) { this.tipoConta = tipoConta; }

    public BigDecimal getSaldoAtual() { return saldoAtual; }
    public void setSaldoAtual(BigDecimal saldoAtual) { this.saldoAtual = saldoAtual; }

    public String getChavePix() { return chavePix; }
    public void setChavePix(String chavePix) { this.chavePix = chavePix; }

    public String getTipoChavePix() { return tipoChavePix; }
    public void setTipoChavePix(String tipoChavePix) { this.tipoChavePix = tipoChavePix; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
}

