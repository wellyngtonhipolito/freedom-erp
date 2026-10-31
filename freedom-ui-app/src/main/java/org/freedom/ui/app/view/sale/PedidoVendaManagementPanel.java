package org.freedom.ui.app.view.sale;

import org.freedom.core.domain.sale.PedidoVenda;
import org.freedom.core.service.sale.PedidoVendaService;
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
 * Painel completo de Gestão de Pedidos de Venda:
 * - Listagem com ModernDataTable
 * - Botão de Novo Pedido com diálogo moderno
 * - Botão de Aprovação com emissão automática de títulos a receber no Financeiro
 */
public class PedidoVendaManagementPanel extends JPanel {

    private final PedidoVendaService service;
    private final ModernDataTable<PedidoVenda> dataTable;
    private final ModernTableModel<PedidoVenda> tableModel;
    private final JFrame parentFrame;

    public PedidoVendaManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.service = new PedidoVendaService();

        setLayout(new BorderLayout(12, 12));

        // 1. Barra de Ações
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnNovo = new JButton("🛒 Novo Pedido de Venda");
        JButton btnAprovar = new JButton("✅ Aprovar & Gerar Contas a Receber");
        JButton btnAtualizar = new JButton("🔄 Recarregar");

        actionPanel.add(btnNovo);
        actionPanel.add(btnAprovar);
        actionPanel.add(btnAtualizar);
        add(actionPanel, BorderLayout.NORTH);

        // 2. Tabela Moderna Fortemente Tipada
        tableModel = new ModernTableModel<>(
            List.of("ID", "Nº Pedido", "Data Emissão", "Valor Total (R$)", "CBS (0.9%)", "IBS (0.1%)", "Status", "Forma Pagamento"),
            List.of(
                p -> p.getId() != null ? p.getId().toString() : "",
                PedidoVenda::getNumeroPedido,
                p -> p.getDataEmissao() != null ? p.getDataEmissao().toString() : "",
                p -> "R$ " + p.getValorTotal(),
                p -> "R$ " + p.getValorCbs(),
                p -> "R$ " + p.getValorIbs(),
                PedidoVenda::getStatusPedido,
                PedidoVenda::getFormaPagamento
            )
        );

        dataTable = new ModernDataTable<>(tableModel);
        add(dataTable, BorderLayout.CENTER);

        // Eventos
        btnNovo.addActionListener(e -> {
            PedidoVendaFormDialog dialog = new PedidoVendaFormDialog(parentFrame, service, this::recarregarPedidos);
            dialog.setVisible(true);
        });

        btnAprovar.addActionListener(e -> aprovarPedidoSelecionado());
        btnAtualizar.addActionListener(e -> recarregarPedidos());

        recarregarPedidos();
    }

    private void aprovarPedidoSelecionado() {
        PedidoVenda selecionado = dataTable.getSelectedItem();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um pedido na tabela para aprovar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if ("APROVADO".equalsIgnoreCase(selecionado.getStatusPedido())) {
            JOptionPane.showMessageDialog(this, "Este pedido já foi aprovado e integrado ao financeiro.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int opc = JOptionPane.showConfirmDialog(this,
            "Confirma a aprovação do Pedido #" + selecionado.getNumeroPedido() + " no valor de R$ " + selecionado.getValorTotal() + "?\n" +
            "Um título de Contas a Receber será gerado automaticamente no Financeiro.",
            "Confirmar Aprovação", JOptionPane.YES_NO_OPTION);

        if (opc == JOptionPane.YES_OPTION) {
            try {
                service.aprovarPedido(selecionado);
                JOptionPane.showMessageDialog(this, "Pedido aprovado e título financeiro emitido com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                recarregarPedidos();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Falha ao aprovar pedido: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void recarregarPedidos() {
        service.listarPedidosAsync()
            .thenAccept(pedidos -> SwingUtilities.invokeLater(() -> dataTable.setData(pedidos)));
    }
}

