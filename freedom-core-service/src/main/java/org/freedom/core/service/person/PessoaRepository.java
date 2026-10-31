package org.freedom.core.service.person;

import org.freedom.core.domain.person.Pessoa;
import org.freedom.infra.db.DatabaseConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório desacoplado para operações na tabela std.pessoa.
 * Utiliza o pool HikariCP e queries otimizadas com o índice pg_trgm.
 */
public class PessoaRepository {

    private static final Logger log = LoggerFactory.getLogger(PessoaRepository.class);

    public Pessoa salvar(Pessoa pessoa) {
        String sql = """
            INSERT INTO std.pessoa (
                filial_id, tipo_pessoa, razao_social, nome_fantasia, cpf_cnpj,
                inscricao_estadual, inscricao_municipal, eh_cliente, eh_fornecedor,
                eh_transportadora, eh_vendedor, cep, logradouro, numero, complemento,
                bairro, municipio_ibge, nome_municipio, uf, telefone, celular_whatsapp, email, ativo
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pessoa.getFilialId());
            stmt.setString(2, pessoa.getTipoPessoa());
            stmt.setString(3, pessoa.getRazaoSocial());
            stmt.setString(4, pessoa.getNomeFantasia());
            stmt.setString(5, pessoa.getCpfCnpj());
            stmt.setString(6, pessoa.getInscricaoEstadual());
            stmt.setString(7, pessoa.getInscricaoMunicipal());
            stmt.setBoolean(8, pessoa.isEhCliente());
            stmt.setBoolean(9, pessoa.isEhFornecedor());
            stmt.setBoolean(10, pessoa.isEhTransportadora());
            stmt.setBoolean(11, pessoa.isEhVendedor());
            stmt.setString(12, pessoa.getCep());
            stmt.setString(13, pessoa.getLogradouro());
            stmt.setString(14, pessoa.getNumero());
            stmt.setString(15, pessoa.getComplemento());
            stmt.setString(16, pessoa.getBairro());
            stmt.setString(17, pessoa.getMunicipioIbge());
            stmt.setString(18, pessoa.getNomeMunicipio());
            stmt.setString(19, pessoa.getUf());
            stmt.setString(20, pessoa.getTelefone());
            stmt.setString(21, pessoa.getCelularWhatsapp());
            stmt.setString(22, pessoa.getEmail());
            stmt.setBoolean(23, pessoa.isAtivo());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pessoa.setId(rs.getLong("id"));
                }
            }
            return pessoa;
        } catch (SQLException e) {
            log.error("Erro ao salvar pessoa no PostgreSQL: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao persistir registro de pessoa.", e);
        }
    }

    public List<Pessoa> buscarPorTermo(String termo) {
        List<Pessoa> resultado = new ArrayList<>();
        String sql = """
            SELECT id, filial_id, tipo_pessoa, razao_social, nome_fantasia, cpf_cnpj,
                   eh_cliente, eh_fornecedor, email, celular_whatsapp, ativo
            FROM std.pessoa
            WHERE razao_social ILIKE ? OR cpf_cnpj ILIKE ? OR nome_fantasia ILIKE ?
            ORDER BY razao_social
            LIMIT 50
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String pattern = "%" + termo + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pessoa p = new Pessoa();
                    p.setId(rs.getLong("id"));
                    p.setFilialId(rs.getLong("filial_id"));
                    p.setTipoPessoa(rs.getString("tipo_pessoa"));
                    p.setRazaoSocial(rs.getString("razao_social"));
                    p.setNomeFantasia(rs.getString("nome_fantasia"));
                    p.setCpfCnpj(rs.getString("cpf_cnpj"));
                    p.setEhCliente(rs.getBoolean("eh_cliente"));
                    p.setEhFornecedor(rs.getBoolean("eh_fornecedor"));
                    p.setEmail(rs.getString("email"));
                    p.setCelularWhatsapp(rs.getString("celular_whatsapp"));
                    p.setAtivo(rs.getBoolean("ativo"));
                    resultado.add(p);
                }
            }
        } catch (SQLException e) {
            log.error("Erro ao consultar pessoas com termo '{}': {}", termo, e.getMessage(), e);
        }
        return resultado;
    }
}
