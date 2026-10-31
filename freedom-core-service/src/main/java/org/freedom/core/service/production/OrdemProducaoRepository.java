package org.freedom.core.service.production;

import org.freedom.core.domain.production.OrdemProducao;
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
 * Repositório para ordens de produção na tabela producao.ordem_producao.
 */
public class OrdemProducaoRepository {

    private static final Logger log = LoggerFactory.getLogger(OrdemProducaoRepository.class);

    public record OrdemProducaoVisualizacaoDto(
        Long id,
        String numeroOp,
        Long produtoId,
        String descricaoProduto,
        java.math.BigDecimal quantidadePlanejada,
        java.math.BigDecimal quantidadeProduzida,
        java.time.LocalDate dataEmissao,
        String statusOp
    ) {}

    public OrdemProducao salvar(OrdemProducao op) {
        String sql = """
            INSERT INTO producao.ordem_producao (
                filial_id, numero_op, produto_id, almoxarifado_id, quantidade_planejada,
                data_emissao, data_previsao_fim, status_op, observacao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, op.getFilialId());
            stmt.setString(2, op.getNumeroOp());
            stmt.setLong(3, op.getProdutoId());
            stmt.setLong(4, op.getAlmoxarifadoId());
            stmt.setBigDecimal(5, op.getQuantidadePlanejada());
            stmt.setDate(6, Date.valueOf(op.getDataEmissao()));
            if (op.getDataPrevisaoFim() != null) {
                stmt.setDate(7, Date.valueOf(op.getDataPrevisaoFim()));
            } else {
                stmt.setNull(7, java.sql.Types.DATE);
            }
            stmt.setString(8, op.getStatusOp());
            stmt.setString(9, op.getObservacao());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    op.setId(rs.getLong("id"));
                }
            }
            return op;
        } catch (SQLException e) {
            log.error("Erro ao salvar ordem de produção: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao salvar Ordem de Produção.", e);
        }
    }

    public List<OrdemProducaoVisualizacaoDto> listarTodas() {
        List<OrdemProducaoVisualizacaoDto> lista = new ArrayList<>();
        String sql = """
            SELECT op.id, op.numero_op, op.produto_id, p.descricao AS descricao_produto,
                   op.quantidade_planejada, op.quantidade_produzida, op.data_emissao, op.status_op
            FROM producao.ordem_producao op
            INNER JOIN comercial.produto p ON p.id = op.produto_id
            ORDER BY op.id DESC
            LIMIT 100
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new OrdemProducaoVisualizacaoDto(
                    rs.getLong("id"),
                    rs.getString("numero_op"),
                    rs.getLong("produto_id"),
                    rs.getString("descricao_produto"),
                    rs.getBigDecimal("quantidade_planejada"),
                    rs.getBigDecimal("quantidade_produzida"),
                    rs.getDate("data_emissao").toLocalDate(),
                    rs.getString("status_op")
                ));
            }
        } catch (SQLException e) {
            log.error("Erro ao listar ordens de produção: {}", e.getMessage(), e);
        }
        return lista;
    }
}
