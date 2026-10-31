package org.freedom.core.service.pos;

import org.freedom.core.domain.pos.CaixaSessao;
import org.freedom.core.domain.pos.VendaPdv;
import org.freedom.infra.db.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório desacoplado para operações do Ponto de Venda no PostgreSQL 16+.
 */
public class PdvRepository {

    private static final Logger log = LoggerFactory.getLogger(PdvRepository.class);

    public CaixaSessao abrirCaixa(CaixaSessao sessao) {
        String sql = """
            INSERT INTO ponto_de_venda.caixa_sessao (
                filial_id, numero_terminal, operador_nome, valor_fundo_troco, status_caixa
            ) VALUES (?, ?, ?, ?, 'ABERTO')
            RETURNING id
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessao.getFilialId());
            stmt.setInt(2, sessao.getNumeroTerminal());
            stmt.setString(3, sessao.getOperadorNome());
            stmt.setBigDecimal(4, sessao.getValorFundoTroco());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sessao.setId(rs.getLong("id"));
                }
            }
            return sessao;
        } catch (SQLException e) {
            log.error("Erro ao abrir caixa no PDV: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao abrir caixa.", e);
        }
    }

    public Optional<CaixaSessao> buscarCaixaAberto(int terminal) {
        String sql = """
            SELECT id, filial_id, numero_terminal, operador_nome, data_abertura,
                   valor_fundo_troco, total_entradas, total_saidas_sangria, status_caixa
            FROM ponto_de_venda.caixa_sessao
            WHERE numero_terminal = ? AND status_caixa = 'ABERTO'
            ORDER BY id DESC
            LIMIT 1
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, terminal);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CaixaSessao s = new CaixaSessao();
                    s.setId(rs.getLong("id"));
                    s.setFilialId(rs.getLong("filial_id"));
                    s.setNumeroTerminal(rs.getInt("numero_terminal"));
                    s.setOperadorNome(rs.getString("operador_nome"));
                    s.setDataAbertura(rs.getTimestamp("data_abertura").toInstant());
                    s.setValorFundoTroco(rs.getBigDecimal("valor_fundo_troco"));
                    s.setTotalEntradas(rs.getBigDecimal("total_entradas"));
                    s.setTotalSaidasSangria(rs.getBigDecimal("total_saidas_sangria"));
                    s.setStatusCaixa(rs.getString("status_caixa"));
                    return Optional.of(s);
                }
            }
        } catch (SQLException e) {
            log.error("Erro ao verificar caixa aberto: {}", e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void registrarVenda(VendaPdv venda) {
        String sqlVenda = """
            INSERT INTO ponto_de_venda.venda_pdv (
                caixa_sessao_id, numero_cupom, valor_subtotal, valor_desconto,
                valor_total, valor_pago, valor_troco, forma_pagamento, valor_cbs, valor_ibs, status_venda
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'CONCLUIDA')
            RETURNING id
            """;

        String sqlAtualizarCaixa = """
            UPDATE ponto_de_venda.caixa_sessao
            SET total_entradas = total_entradas + ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(sqlVenda)) {
                    stmt.setLong(1, venda.getCaixaSessaoId());
                    stmt.setString(2, venda.getNumeroCupom());
                    stmt.setBigDecimal(3, venda.getValorSubtotal());
                    stmt.setBigDecimal(4, venda.getValorDesconto());
                    stmt.setBigDecimal(5, venda.getValorTotal());
                    stmt.setBigDecimal(6, venda.getValorPago());
                    stmt.setBigDecimal(7, venda.getValorTroco());
                    stmt.setString(8, venda.getFormaPagamento());
                    stmt.setBigDecimal(9, venda.getValorCbs());
                    stmt.setBigDecimal(10, venda.getValorIbs());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            venda.setId(rs.getLong("id"));
                        }
                    }
                }

                // Incrementa o total de entradas no caixa
                try (PreparedStatement stmt = conn.prepareStatement(sqlAtualizarCaixa)) {
                    stmt.setBigDecimal(1, venda.getValorTotal());
                    stmt.setLong(2, venda.getCaixaSessaoId());
                    stmt.executeUpdate();
                }

                conn.commit();
                log.info("Venda PDV #{} registrada com sucesso.", venda.getNumeroCupom());
            } catch (SQLException e) {
                conn.rollback();
                log.error("Erro na transação de venda PDV: {}", e.getMessage(), e);
                throw new RuntimeException("Falha ao registrar venda de balcão.", e);
            }
        } catch (SQLException e) {
            log.error("Erro de conexão ao registrar venda PDV: {}", e.getMessage(), e);
            throw new RuntimeException("Falha de conexão com banco de dados.", e);
        }
    }

    public List<VendaPdv> listarUltimasVendas(Long caixaSessaoId) {
        List<VendaPdv> lista = new ArrayList<>();
        String sql = """
            SELECT id, caixa_sessao_id, numero_cupom, data_venda, valor_total, forma_pagamento, status_venda
            FROM ponto_de_venda.venda_pdv
            WHERE caixa_sessao_id = ?
            ORDER BY id DESC
            LIMIT 50
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, caixaSessaoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    VendaPdv v = new VendaPdv();
                    v.setId(rs.getLong("id"));
                    v.setCaixaSessaoId(rs.getLong("caixa_sessao_id"));
                    v.setNumeroCupom(rs.getString("numero_cupom"));
                    v.setDataVenda(rs.getTimestamp("data_venda").toInstant());
                    v.setValorTotal(rs.getBigDecimal("valor_total"));
                    v.setFormaPagamento(rs.getString("forma_pagamento"));
                    v.setStatusVenda(rs.getString("status_venda"));
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            log.error("Erro ao listar vendas do PDV: {}", e.getMessage(), e);
        }
        return lista;
    }
}
