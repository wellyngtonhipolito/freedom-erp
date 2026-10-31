package org.freedom.ui.app.view.finance;

import org.freedom.core.domain.finance.ContaReceber;
import org.freedom.core.service.finance.ContaReceberRepository;
import org.freedom.core.service.finance.ContaReceberService;
import org.freedom.ui.components.table.ModernDataTable;
import org.freedom.ui.components.table.ModernTableModel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Painel completo de Gestão de Contas a Receber:
 * - Listagem com ModernDataTable
 * - Quitação com cálculo automático de multa e juros
 * - Integração com banco PostgreSQL em tempo real
 */
public class FinanceiroReceberPanel extends JPanel {

    private final ContaReceberService service;
    private final ContaReceberRepository repository;
    private final ModernDataTable<ContaReceber> dataTable;
    private final ModernTableModel<ContaReceber> tableModel;
    private final JFrame parentFrame;

    public FinanceiroReceberPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.service = new ContaReceberService();
        this.repository = new ContaReceberRepository();

        setLayout(new BorderLayout(12, 12));

        // 1. Barra de Ferramentas
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnLiquidar = new JButton("💳 Baixar / Receber Título");
        JButton btnAtualizar = new JButton("🔄 Recarregar");

        actionPanel.add(btnLiquidar);
        actionPanel.add(btnAtualizar);
        add(actionPanel, BorderLayout.NORTH);

        // 2. Tabela Moderna Fortemente Tipada
        tableModel = new ModernTableModel<>(
            List.of("ID", "Nº Documento", "Parcela", "Vencimento", "Valor Nominal", "Saldo Devedor", "Status", "Cobrança"),
            List.of(
                c -> c.getId() != null ? c.getId().toString() : "",
                ContaReceber::getNumeroDocumento,
                c -> c.getParcela() + "/" + c.getTotalParcelas(),
                c -> c.getDataVencimento() != null ? c.getDataVencimento().toString() : "",
                c -> c.getValorNominal() != null ? "R$ " + c.getValorNominal() : "",
                c -> c.getSaldoDevedor() != null ? "R$ " + c.getSaldoDevedor() : "",
                ContaReceber::getStatusTitulo,
                ContaReceber::getTipoCobranca
            )
        );

        dataTable = new ModernDataTable<>(tableModel);
        add(dataTable, BorderLayout.CENTER);

        // Eventos
        btnLiquidar.addActionListener(e -> abrirDialogoBaixa());
        btnAtualizar.addActionListener(e -> recarregarTitulos());

        recarregarTitulos();
    }

    private void abrirDialogoBaixa() {
        ContaReceber selecionado = dataTable.getSelectedItem();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um título na tabela para registrar o recebimento.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BaixaContaReceberDialog dialog = new BaixaContaReceberDialog(parentFrame, selecionado, service, this::recarregarTitulos);
        dialog.setVisible(true);
    }

    public void recarregarTitulos() {
        Thread.ofVirtual().start(() -> {
            List<ContaReceber> lista = repository.listarPendentes();
            SwingUtilities.invokeLater(() -> dataTable.setData(lista));
        });
    }
}
