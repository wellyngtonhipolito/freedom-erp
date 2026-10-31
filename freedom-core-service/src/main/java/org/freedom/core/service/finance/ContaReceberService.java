package org.freedom.core.service.finance;

import org.freedom.core.domain.finance.BaixaContaReceber;
import org.freedom.core.domain.finance.ContaReceber;
import org.freedom.infra.db.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Serviço completo de Contas a Receber:
 * - Emissão de novos títulos
 * - Baixa parcial ou total com juros, multa e abatimento de saldo
 * - Integração transacional no PostgreSQL via pool HikariCP
 */
public class ContaReceberService {

    private static final Logger log = LoggerFactory.getLogger(ContaReceberService.class);

    private final ContaReceberRepository repository;
    private final CalculadoraFinanceiraService calculadora;

    public ContaReceberService() {
        this.repository = new ContaReceberRepository();
        this.calculadora = new CalculadoraFinanceiraService();
    }

    public ContaReceberService(ContaReceberRepository repository, CalculadoraFinanceiraService calculadora) {
        this.repository = repository;
        this.calculadora = calculadora;
    }

    public ContaReceber emitirTitulo(ContaReceber conta) {
        if (conta.getValorNominal() == null || conta.getValorNominal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor nominal do título deve ser maior que zero.");
        }
        if (conta.getDataVencimento() == null || conta.getDataEmissao() == null) {
            throw new IllegalArgumentException("As datas de emissão e vencimento são obrigatórias.");
        }
        conta.setSaldoDevedor(conta.getValorNominal());
        conta.setStatusTitulo("PENDENTE");
        return repository.salvar(conta);
    }

    /**
     * Registra o recebimento (baixa parcial ou total) do título.
     * Atualiza o saldo devedor e o status para QUITADO ou QUITADO_PARCIAL.
     */
    public void registrarBaixa(Long contaReceberId, Long contaBancariaId, LocalDate dataBaixa,
                               BigDecimal valorPagoPrincipal, BigDecimal valorJuros,
                               BigDecimal valorMulta, BigDecimal valorDesconto,
                               String formaPagamento, String observacao) {

        String sqlBaixa = """
            INSERT INTO financeiro.baixa_conta_receber (
                conta_receber_id, conta_bancaria_id, data_baixa, valor_pago,
                valor_juros, valor_multa, valor_desconto, forma_pagamento, observacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        String sqlAtualizarTitulo = """
            UPDATE financeiro.conta_receber
            SET valor_pago = valor_pago + ?,
                valor_juros = valor_juros + ?,
                valor_multa = valor_multa + ?,
                valor_desconto = valor_desconto + ?,
                saldo_devedor = GREATEST(0.00, saldo_devedor - ?),
                status_titulo = CASE WHEN (saldo_devedor - ?) <= 0.00 THEN 'QUITADO' ELSE 'QUITADO_PARCIAL' END,
                data_liquidacao = CASE WHEN (saldo_devedor - ?) <= 0.00 THEN ? ELSE data_liquidacao END,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        String sqlMovimentoBancario = """
            INSERT INTO financeiro.movimento_bancario (
                conta_bancaria_id, data_movimento, tipo_movimento, valor, saldo_apos_movimento,
                origem_movimento, documento_referencia, historico
            )
            SELECT ?, ?, 'CREDITO', ?, (saldo_atual + ?), 'RECEBIMENTO_CLIENTE', ?, ?
            FROM financeiro.conta_bancaria WHERE id = ?
            """;

        String sqlAtualizarSaldoConta = """
            UPDATE financeiro.conta_bancaria
            SET saldo_atual = saldo_atual + ?,
                atualizado_em = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Inserir histórico de baixa
                try (PreparedStatement stmt = conn.prepareStatement(sqlBaixa)) {
                    stmt.setLong(1, contaReceberId);
                    if (contaBancariaId != null) stmt.setLong(2, contaBancariaId); else stmt.setNull(2, java.sql.Types.BIGINT);
                    stmt.setDate(3, Date.valueOf(dataBaixa));
                    stmt.setBigDecimal(4, valorPagoPrincipal);
                    stmt.setBigDecimal(5, valorJuros != null ? valorJuros : BigDecimal.ZERO);
                    stmt.setBigDecimal(6, valorMulta != null ? valorMulta : BigDecimal.ZERO);
                    stmt.setBigDecimal(7, valorDesconto != null ? valorDesconto : BigDecimal.ZERO);
                    stmt.setString(8, formaPagamento != null ? formaPagamento : "PIX");
                    stmt.setString(9, observacao);
                    stmt.executeUpdate();
                }

                // 2. Atualizar saldo do título
                try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizarTitulo)) {
                    stmt.setBigDecimal(1, valorPagoPrincipal);
                    stmt.setBigDecimal(2, valorJuros != null ? valorJuros : BigDecimal.ZERO);
                    stmt.setBigDecimal(3, valorMulta != null ? valorMulta : BigDecimal.ZERO);
                    stmt.setBigDecimal(4, valorDesconto != null ? valorDesconto : BigDecimal.ZERO);
                    stmt.setBigDecimal(5, valorPagoPrincipal);
                    stmt.setBigDecimal(6, valorPagoPrincipal);
                    stmt.setBigDecimal(7, valorPagoPrincipal);
                    stmt.setDate(8, Date.valueOf(dataBaixa));
                    stmt.setLong(9, contaReceberId);
                    stmt.executeUpdate();
                }

                // 3. Se informada conta bancária, creditar no extrato e saldo da conta
                if (contaBancariaId != null) {
                    BigDecimal totalCreditado = valorPagoPrincipal
                        .add(valorJuros != null ? valorJuros : BigDecimal.ZERO)
                        .add(valorMulta != null ? valorMulta : BigDecimal.ZERO)
                        .subtract(valorDesconto != null ? valorDesconto : BigDecimal.ZERO);

                    try (PreparedStatement stmt = conn.prepareStatement(sqlMovimentoBancario)) {
                        stmt.setLong(1, contaBancariaId);
                        stmt.setDate(2, Date.valueOf(dataBaixa));
                        stmt.setBigDecimal(3, totalCreditado);
                        stmt.setBigDecimal(4, totalCreditado);
                        stmt.setString(5, "TIT-" + contaReceberId);
                        stmt.setString(6, "Recebimento Título #" + contaReceberId + (observacao != null ? " - " + observacao : ""));
                        stmt.setLong(7, contaBancariaId);
                        stmt.executeUpdate();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizarSaldoConta)) {
                        stmt.setBigDecimal(1, totalCreditado);
                        stmt.setLong(2, contaBancariaId);
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
                log.info("Baixa de título #{} registrada com sucesso.", contaReceberId);
            } catch (SQLException e) {
                conn.rollback();
                log.error("Erro na transação de baixa do título #{}: {}", contaReceberId, e.getMessage(), e);
                throw new RuntimeException("Falha ao registrar baixa do título.", e);
            }
        } catch (SQLException e) {
            log.error("Erro de conexão ao baixar título: {}", e.getMessage(), e);
            throw new RuntimeException("Falha de conexão com o banco de dados.", e);
        }
    }
}

