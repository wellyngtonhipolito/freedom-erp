package org.freedom.core.service.stock;

import org.freedom.core.domain.stock.SaldoEstoque;
import org.freedom.infra.db.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositório e Gestor de Movimentações de Estoque no PostgreSQL 16+.
 */
public class EstoqueRepository {

    private static final Logger log = LoggerFactory.getLogger(EstoqueRepository.class);

    public record SaldoVisualizacaoDto(
        Long produtoId,
        String descricaoProduto,
        String unidadeMedida,
        BigDecimal quantidadeAtual,
        BigDecimal custoMedioPonderado,
        BigDecimal valorTotalEstoque
    ) {}

    public void registrarMovimento(Long almoxarifadoId, Long produtoId, String tipoMovimento,
                                  String origemMovimento, String documentoRef,
                                  BigDecimal quantidade, BigDecimal custoUnitario, String obs) {

        String sqlBuscaSaldo = """
            SELECT id, quantidade_atual, custo_medio_ponderado
            FROM estoque_compras.saldo_estoque
            WHERE almoxarifado_id = ? AND produto_id = ?
            FOR UPDATE
            """;

        String sqlInsereSaldo = """
            INSERT INTO estoque_compras.saldo_estoque (almoxarifado_id, produto_id, quantidade_atual, custo_medio_ponderado)
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

        String sqlAtualizaSaldo = """
            UPDATE estoque_compras.saldo_estoque
            SET quantidade_atual = ?, custo_medio_ponderado = ?, atualizado_em = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        String sqlMovimento = """
            INSERT INTO estoque_compras.movimento_estoque (
                almoxarifado_id, produto_id, tipo_movimento, origem_movimento, documento_referencia,
                data_movimento, quantidade, custo_unitario, valor_total,
                quantidade_apos_movimento, custo_medio_apos_movimento, observacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal qtdAtual = BigDecimal.ZERO;
                BigDecimal custoMedioAtual = BigDecimal.ZERO;
                Long saldoId = null;

                // 1. Obter ou inicializar saldo
                try (PreparedStatement stmt = conn.prepareStatement(sqlBuscaSaldo)) {
                    stmt.setLong(1, almoxarifadoId);
                    stmt.setLong(2, produtoId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            saldoId = rs.getLong("id");
                            qtdAtual = rs.getBigDecimal("quantidade_atual");
                            custoMedioAtual = rs.getBigDecimal("custo_medio_ponderado");
                        }
                    }
                }

                BigDecimal novaQtd;
                BigDecimal novoCustoMedio;

                if ("ENTRADA".equalsIgnoreCase(tipoMovimento)) {
                    novaQtd = qtdAtual.add(quantidade);
                    novoCustoMedio = CalculadoraCustoMedio.calcularNovoCustoMedio(qtdAtual, custoMedioAtual, quantidade, custoUnitario);
                } else { // SAÍDA
                    novaQtd = qtdAtual.subtract(quantidade);
                    novoCustoMedio = custoMedioAtual;
                }

                // 2. Atualizar ou inserir saldo
                if (saldoId == null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlInsereSaldo)) {
                        stmt.setLong(1, almoxarifadoId);
                        stmt.setLong(2, produtoId);
                        stmt.setBigDecimal(3, novaQtd);
                        stmt.setBigDecimal(4, novoCustoMedio);
                        stmt.execute();
                    }
                } else {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizaSaldo)) {
                        stmt.setBigDecimal(1, novaQtd);
                        stmt.setBigDecimal(2, novoCustoMedio);
                        stmt.setLong(3, saldoId);
                        stmt.executeUpdate();
                    }
                }

                // 3. Inserir registro no Kardex / Movimento
                try (PreparedStatement stmt = conn.prepareStatement(sqlMovimento)) {
                    stmt.setLong(1, almoxarifadoId);
                    stmt.setLong(2, produtoId);
                    stmt.setString(3, tipoMovimento);
                    stmt.setString(4, origemMovimento);
                    stmt.setString(5, documentoRef);
                    stmt.setDate(6, Date.valueOf(LocalDate.now()));
                    stmt.setBigDecimal(7, quantidade);
                    stmt.setBigDecimal(8, custoUnitario);
                    stmt.setBigDecimal(9, quantidade.multiply(custoUnitario));
                    stmt.setBigDecimal(10, novaQtd);
                    stmt.setBigDecimal(11, novoCustoMedio);
                    stmt.setString(12, obs);
                    stmt.executeUpdate();
                }

                conn.commit();
                log.info("Movimentação de {} para o produto #{} registrada com sucesso.", tipoMovimento, produtoId);
            } catch (SQLException e) {
                conn.rollback();
                log.error("Erro na transação de estoque: {}", e.getMessage(), e);
                throw new RuntimeException("Falha ao registrar movimentação de estoque.", e);
            }
        } catch (SQLException e) {
            log.error("Erro de conexão com o banco de dados: {}", e.getMessage(), e);
            throw new RuntimeException("Falha de conexão.", e);
        }
    }

    public List<SaldoVisualizacaoDto> listarSaldosGerais() {
        List<SaldoVisualizacaoDto> lista = new ArrayList<>();
        String sql = """
            SELECT p.id AS produto_id, p.descricao, p.unidade_medida,
                   COALESCE(s.quantidade_atual, 0.0000) AS qtd,
                   COALESCE(s.custo_medio_ponderado, p.preco_custo) AS custo_medio,
                   (COALESCE(s.quantidade_atual, 0.0000) * COALESCE(s.custo_medio_ponderado, p.preco_custo)) AS valor_total
            FROM comercial.produto p
            LEFT JOIN estoque_compras.saldo_estoque s ON s.produto_id = p.id
            WHERE p.ativo = TRUE
            ORDER BY p.descricao ASC
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new SaldoVisualizacaoDto(
                    rs.getLong("produto_id"),
                    rs.getString("descricao"),
                    rs.getString("unidade_medida"),
                    rs.getBigDecimal("qtd"),
                    rs.getBigDecimal("custo_medio"),
                    rs.getBigDecimal("valor_total")
                ));
            }
        } catch (SQLException e) {
            log.error("Erro ao listar saldos de estoque: {}", e.getMessage(), e);
        }
        return lista;
    }
}
