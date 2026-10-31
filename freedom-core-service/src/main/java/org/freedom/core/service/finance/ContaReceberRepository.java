package org.freedom.core.service.finance;

import org.freedom.core.domain.finance.ContaReceber;
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
 * Repositório desacoplado para operações na tabela fnc.conta_receber.
 */
public class ContaReceberRepository {

    private static final Logger log = LoggerFactory.getLogger(ContaReceberRepository.class);

    public ContaReceber salvar(ContaReceber conta) {
        String sql = """
            INSERT INTO fnc.conta_receber (
                filial_id, cliente_id, numero_documento, parcela, total_parcelas,
                data_emissao, data_vencimento, valor_nominal, valor_desconto,
                valor_juros, valor_multa, valor_pago, saldo_devedor, status_titulo,
                tipo_cobranca, chave_transacao_pix, historico
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, conta.getFilialId());
            stmt.setLong(2, conta.getClienteId());
            stmt.setString(3, conta.getNumeroDocumento());
            stmt.setInt(4, conta.getParcela());
            stmt.setInt(5, conta.getTotalParcelas());
            stmt.setDate(6, Date.valueOf(conta.getDataEmissao()));
            stmt.setDate(7, Date.valueOf(conta.getDataVencimento()));
            stmt.setBigDecimal(8, conta.getValorNominal());
            stmt.setBigDecimal(9, conta.getValorDesconto());
            stmt.setBigDecimal(10, conta.getValorJuros());
            stmt.setBigDecimal(11, conta.getValorMulta());
            stmt.setBigDecimal(12, conta.getValorPago());
            stmt.setBigDecimal(13, conta.getSaldoDevedor());
            stmt.setString(14, conta.getStatusTitulo());
            stmt.setString(15, conta.getTipoCobranca());
            stmt.setString(16, conta.getChaveTransacaoPix());
            stmt.setString(17, conta.getHistorico());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    conta.setId(rs.getLong("id"));
                }
            }
            return conta;
        } catch (SQLException e) {
            log.error("Erro ao salvar conta a receber: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao persistir conta a receber.", e);
        }
    }

    public List<ContaReceber> listarPendentes() {
        List<ContaReceber> resultado = new ArrayList<>();
        String sql = """
            SELECT id, filial_id, cliente_id, numero_documento, parcela, total_parcelas,
                   data_emissao, data_vencimento, valor_nominal, saldo_devedor, status_titulo, tipo_cobranca
            FROM fnc.conta_receber
            WHERE status_titulo = 'PENDENTE'
            ORDER BY data_vencimento ASC
            LIMIT 100
            """;

        try (Connection conn = DatabaseConnectionPool.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ContaReceber c = new ContaReceber();
                c.setId(rs.getLong("id"));
                c.setFilialId(rs.getLong("filial_id"));
                c.setClienteId(rs.getLong("cliente_id"));
                c.setNumeroDocumento(rs.getString("numero_documento"));
                c.setParcela(rs.getInt("parcela"));
                c.setTotalParcelas(rs.getInt("total_parcelas"));
                c.setDataEmissao(rs.getDate("data_emissao").toLocalDate());
                c.setDataVencimento(rs.getDate("data_vencimento").toLocalDate());
                c.setValorNominal(rs.getBigDecimal("valor_nominal"));
                c.setSaldoDevedor(rs.getBigDecimal("saldo_devedor"));
                c.setStatusTitulo(rs.getString("status_titulo"));
                c.setTipoCobranca(rs.getString("tipo_cobranca"));
                resultado.add(c);
            }
        } catch (SQLException e) {
            log.error("Erro ao listar contas a receber pendentes: {}", e.getMessage(), e);
        }
        return resultado;
    }
}

