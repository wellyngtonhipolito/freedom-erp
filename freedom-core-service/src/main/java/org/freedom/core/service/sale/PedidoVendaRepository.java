package org.freedom.core.service.sale;

import org.freedom.core.domain.sale.ItemPedidoVenda;
import org.freedom.core.domain.sale.PedidoVenda;
import org.freedom.infra.db.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositório desacoplado para a tabela comercial.pedido_venda.
 */
public class PedidoVendaRepository {

    private static final Logger log = LoggerFactory.getLogger(PedidoVendaRepository.class);

    public PedidoVenda salvarPedido(PedidoVenda pedido, List<ItemPedidoVenda> itens) {
        String sqlPedido = """
            INSERT INTO comercial.pedido_venda (
                filial_id, cliente_id, numero_pedido, data_emissao, data_previsao_entrega,
                valor_itens, valor_frete, valor_desconto, valor_total, valor_icms,
                valor_cbs, valor_ibs, valor_imposto_seletivo, status_pedido, forma_pagamento, observacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        String sqlItem = """
            INSERT INTO comercial.item_pedido_venda (
                pedido_venda_id, produto_id, numero_item, quantidade, preco_unitario,
                valor_desconto, valor_total, aliquota_icms, valor_icms, aliquota_cbs,
                valor_cbs, aliquota_ibs, valor_ibs
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Inserir Cabeçalho do Pedido
                try (PreparedStatement stmt = conn.prepareStatement(sqlPedido)) {
                    stmt.setLong(1, pedido.getFilialId());
                    stmt.setLong(2, pedido.getClienteId());
                    stmt.setString(3, pedido.getNumeroPedido());
                    stmt.setDate(4, Date.valueOf(pedido.getDataEmissao()));
                    if (pedido.getDataPrevisaoEntrega() != null) {
                        stmt.setDate(5, Date.valueOf(pedido.getDataPrevisaoEntrega()));
                    } else {
                        stmt.setNull(5, java.sql.Types.DATE);
                    }
                    stmt.setBigDecimal(6, pedido.getValorItens());
                    stmt.setBigDecimal(7, pedido.getValorFrete());
                    stmt.setBigDecimal(8, pedido.getValorDesconto());
                    stmt.setBigDecimal(9, pedido.getValorTotal());
                    stmt.setBigDecimal(10, pedido.getValorIcms());
                    stmt.setBigDecimal(11, pedido.getValorCbs());
                    stmt.setBigDecimal(12, pedido.getValorIbs());
                    stmt.setBigDecimal(13, pedido.getValorImpostoSeletivo());
                    stmt.setString(14, pedido.getStatusPedido());
                    stmt.setString(15, pedido.getFormaPagamento());
                    stmt.setString(16, pedido.getObservacao());

                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            pedido.setId(rs.getLong("id"));
                        }
                    }
                }

                // 2. Inserir Itens do Pedido
                if (itens != null && !itens.isEmpty()) {
                    try (PreparedStatement stmt = conn.prepareStatement(sqlItem)) {
                        for (ItemPedidoVenda item : itens) {
                            stmt.setLong(1, pedido.getId());
                            stmt.setLong(2, item.getProdutoId());
                            stmt.setInt(3, item.getNumeroItem());
                            stmt.setBigDecimal(4, item.getQuantidade());
                            stmt.setBigDecimal(5, item.getPrecoUnitario());
                            stmt.setBigDecimal(6, item.getValorDesconto());
                            stmt.setBigDecimal(7, item.getValorTotal());
                            stmt.setBigDecimal(8, item.getAliquotaIcms());
                            stmt.setBigDecimal(9, item.getValorIcms());
                            stmt.setBigDecimal(10, item.getAliquotaCbs());
                            stmt.setBigDecimal(11, item.getValorCbs());
                            stmt.setBigDecimal(12, item.getAliquotaIbs());
                            stmt.setBigDecimal(13, item.getValorIbs());
                            stmt.addBatch();
                        }
                        stmt.executeBatch();
                    }
                }

                conn.commit();
                log.info("Pedido de venda #{} salvo com sucesso.", pedido.getId());
                return pedido;
            } catch (SQLException e) {
                conn.rollback();
                log.error("Erro na transação de pedido: {}", e.getMessage(), e);
                throw new RuntimeException("Falha ao salvar pedido de venda.", e);
            }
        } catch (SQLException e) {
            log.error("Erro de conexão com banco de dados: {}", e.getMessage(), e);
            throw new RuntimeException("Falha de conexão.", e);
        }
    }

    public List<PedidoVenda> listarTodos() {
        List<PedidoVenda> lista = new ArrayList<>();
        String sql = """
            SELECT id, filial_id, cliente_id, numero_pedido, data_emissao,
                   valor_itens, valor_total, valor_cbs, valor_ibs, status_pedido, forma_pagamento
            FROM comercial.pedido_venda
            ORDER BY id DESC
            LIMIT 100
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                PedidoVenda p = new PedidoVenda();
                p.setId(rs.getLong("id"));
                p.setFilialId(rs.getLong("filial_id"));
                p.setClienteId(rs.getLong("cliente_id"));
                p.setNumeroPedido(rs.getString("numero_pedido"));
                p.setDataEmissao(rs.getDate("data_emissao").toLocalDate());
                p.setValorItens(rs.getBigDecimal("valor_itens"));
                p.setValorTotal(rs.getBigDecimal("valor_total"));
                p.setValorCbs(rs.getBigDecimal("valor_cbs"));
                p.setValorIbs(rs.getBigDecimal("valor_ibs"));
                p.setStatusPedido(rs.getString("status_pedido"));
                p.setFormaPagamento(rs.getString("forma_pagamento"));
                lista.add(p);
            }
        } catch (SQLException e) {
            log.error("Erro ao listar pedidos: {}", e.getMessage(), e);
        }
        return lista;
    }
}
