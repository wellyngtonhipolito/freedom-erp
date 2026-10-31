package org.freedom.core.service.product;

import org.freedom.core.domain.product.Produto;
import org.freedom.infra.db.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repositório desacoplado para a tabela comercial.produto.
 * Suporta busca textual acelerada via índice pg_trgm e código de barras.
 */
public class ProdutoRepository {

    private static final Logger log = LoggerFactory.getLogger(ProdutoRepository.class);

    public Produto salvar(Produto produto) {
        String sql = """
            INSERT INTO comercial.produto (
                filial_id, codigo_referencia, codigo_barras_ean, descricao, unidade_medida,
                preco_custo, preco_venda, markup_percentual, ncm, cest, origem_mercadoria,
                tipo_item_sped, sujeito_imposto_seletivo, ativo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, produto.getFilialId());
            stmt.setString(2, produto.getCodigoReferencia());
            stmt.setString(3, produto.getCodigoBarrasEan());
            stmt.setString(4, produto.getDescricao());
            stmt.setString(5, produto.getUnidadeMedida());
            stmt.setBigDecimal(6, produto.getPrecoCusto());
            stmt.setBigDecimal(7, produto.getPrecoVenda());
            stmt.setBigDecimal(8, produto.getMarkupPercentual());
            stmt.setString(9, produto.getNcm());
            stmt.setString(10, produto.getCest());
            stmt.setInt(11, produto.getOrigemMercadoria());
            stmt.setString(12, produto.getTipoItemSped());
            stmt.setBoolean(13, produto.isSujeitoImpostoSeletivo());
            stmt.setBoolean(14, produto.isAtivo());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    produto.setId(rs.getLong("id"));
                }
            }
            return produto;
        } catch (SQLException e) {
            log.error("Erro ao salvar produto: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao persistir produto no banco de dados.", e);
        }
    }

    public List<Produto> buscarPorTermo(String termo) {
        List<Produto> lista = new ArrayList<>();
        String sql = """
            SELECT id, filial_id, codigo_referencia, codigo_barras_ean, descricao, unidade_medida,
                   preco_custo, preco_venda, markup_percentual, ncm, cest, sujeito_imposto_seletivo, ativo
            FROM comercial.produto
            WHERE descricao ILIKE ? OR codigo_referencia ILIKE ? OR codigo_barras_ean ILIKE ?
            ORDER BY descricao ASC
            LIMIT 100
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + termo + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Produto p = new Produto();
                    p.setId(rs.getLong("id"));
                    p.setFilialId(rs.getLong("filial_id"));
                    p.setCodigoReferencia(rs.getString("codigo_referencia"));
                    p.setCodigoBarrasEan(rs.getString("codigo_barras_ean"));
                    p.setDescricao(rs.getString("descricao"));
                    p.setUnidadeMedida(rs.getString("unidade_medida"));
                    p.setPrecoCusto(rs.getBigDecimal("preco_custo"));
                    p.setPrecoVenda(rs.getBigDecimal("preco_venda"));
                    p.setMarkupPercentual(rs.getBigDecimal("markup_percentual"));
                    p.setNcm(rs.getString("ncm"));
                    p.setCest(rs.getString("cest"));
                    p.setSujeitoImpostoSeletivo(rs.getBoolean("sujeito_imposto_seletivo"));
                    p.setAtivo(rs.getBoolean("ativo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            log.error("Erro ao consultar produtos: {}", e.getMessage(), e);
        }
        return lista;
    }
}
