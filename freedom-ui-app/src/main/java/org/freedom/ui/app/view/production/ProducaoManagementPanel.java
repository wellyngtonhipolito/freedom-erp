package org.freedom.ui.app.view.production;

import org.freedom.core.service.production.OrdemProducaoRepository;
import org.freedom.core.service.production.OrdemProducaoService;
import org.freedom.ui.components.table.ModernDataTable;
import org.freedom.ui.components.table.ModernTableModel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.util.List;

/**
 * Painel completo de Gestão de Ordens de Produção (PCP):
 * - Listagem com ModernDataTable
 * - Abertura de OP
 * - Finalização com entrada física no estoque
 */
public class ProducaoManagementPanel extends JPanel {

    private final OrdemProducaoService service;
    private final ModernDataTable<OrdemProducaoRepository.OrdemProducaoVisualizacaoDto> dataTable;
    private final ModernTableModel<OrdemProducaoRepository.OrdemProducaoVisualizacaoDto> tableModel;
    private final JFrame parentFrame;

    public ProducaoManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.service = new OrdemProducaoService();

        setLayout(new BorderLayout(12, 12));

        // 1. Barra de Ações
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnNovaOp = new JButton("⚙️ Abrir Nova OP");
        JButton btnFinalizar = new JButton("🏁 Finalizar & Entrar em Estoque");
        JButton btnAtualizar = new JButton("🔄 Recarregar");

        actionPanel.add(btnNovaOp);
        actionPanel.add(btnFinalizar);
        actionPanel.add(btnAtualizar);
        add(actionPanel, BorderLayout.NORTH);

        // 2. Tabela Moderna Fortemente Tipada
        tableModel = new ModernTableModel<>(
            List.of("ID", "Nº OP", "Produto Acabado", "Qtd. Planejada", "Qtd. Produzida", "Data Emissão", "Status"),
            List.of(
                op -> op.id() != null ? op.id().toString() : "",
                OrdemProducaoRepository.OrdemProducaoVisualizacaoDto::numeroOp,
                OrdemProducaoRepository.OrdemProducaoVisualizacaoDto::descricaoProduto,
                op -> op.quantidadePlanejada() != null ? op.quantidadePlanejada().toString() : "0.0000",
                op -> op.quantidadeProduzida() != null ? op.quantidadeProduzida().toString() : "0.0000",
                op -> op.dataEmissao() != null ? op.dataEmissao().toString() : "",
                OrdemProducaoRepository.OrdemProducaoVisualizacaoDto::statusOp
            )
        );

        dataTable = new ModernDataTable<>(tableModel);
        add(dataTable, BorderLayout.CENTER);

        // Eventos
        btnNovaOp.addActionListener(e -> {
            OrdemProducaoFormDialog dialog = new OrdemProducaoFormDialog(parentFrame, service, this::recarregarOrdens);
            dialog.setVisible(true);
        });

        btnFinalizar.addActionListener(e -> finalizarOpSelecionada());
        btnAtualizar.addActionListener(e -> recarregarOrdens());

        recarregarOrdens();
    }

    private void finalizarOpSelecionada() {
        OrdemProducaoRepository.OrdemProducaoVisualizacaoDto selecionada = dataTable.getSelectedItem();
        if (selecionada == null) {
            JOptionPane.showMessageDialog(this, "Selecione uma Ordem de Produção na tabela.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("FINALIZADA".equalsIgnoreCase(selecionada.statusOp())) {
            JOptionPane.showMessageDialog(this, "Esta Ordem de Produção já está finalizada.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String qtdStr = JOptionPane.showInputDialog(this,
            "Informe a quantidade efetivamente fabricada para dar entrada no estoque:",
            selecionada.quantidadePlanejada().toString());

        if (qtdStr != null && !qtdStr.isBlank()) {
            try {
                BigDecimal qtd = new BigDecimal(qtdStr.trim().replace(",", "."));
                org.freedom.core.domain.production.OrdemProducao op = new org.freedom.core.domain.production.OrdemProducao();
                op.setId(selecionada.id());
                op.setNumeroOp(selecionada.numeroOp());
                op.setProdutoId(selecionada.produtoId());

                service.finalizarOrdemProducao(op, qtd, BigDecimal.ZERO);
                JOptionPane.showMessageDialog(this, "OP finalizada! Produto acabado entrou no estoque com sucesso.", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                recarregarOrdens();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Valor inválido ou erro ao finalizar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void recarregarOrdens() {
        service.listarOrdensAsync()
            .thenAccept(ordens -> SwingUtilities.invokeLater(() -> dataTable.setData(ordens)));
    }
}

