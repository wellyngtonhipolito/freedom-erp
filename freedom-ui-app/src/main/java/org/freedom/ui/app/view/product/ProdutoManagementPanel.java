package org.freedom.ui.app.view.product;

import org.freedom.core.domain.product.Produto;
import org.freedom.core.service.product.ProdutoService;
import org.freedom.ui.components.table.ModernDataTable;
import org.freedom.ui.components.table.ModernTableModel;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Painel completo de Gestão de Produtos:
 * - Listagem com ModernDataTable
 * - Busca assíncrona por Virtual Threads
 * - Cadastro com cálculo dinâmico de Markup e enquadramento fiscal 2026
 */
public class ProdutoManagementPanel extends JPanel {

    private final ProdutoService service;
    private final ModernDataTable<Produto> dataTable;
    private final ModernTableModel<Produto> tableModel;
    private final JFrame parentFrame;

    public ProdutoManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.service = new ProdutoService();

        setLayout(new BorderLayout(12, 12));

        // 1. Barra de Ações
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnNovo = new JButton("📦 Novo Produto");
        JButton btnAtualizar = new JButton("🔄 Recarregar");

        actionPanel.add(btnNovo);
        actionPanel.add(btnAtualizar);
        add(actionPanel, BorderLayout.NORTH);

        // 2. Tabela Moderna Fortemente Tipada
        tableModel = new ModernTableModel<>(
            List.of("ID", "Referência", "Descrição do Produto", "UN", "Preço Custo", "Preço Venda", "Markup (%)", "NCM", "Imp. Seletivo"),
            List.of(
                p -> p.getId() != null ? p.getId().toString() : "",
                p -> p.getCodigoReferencia() != null ? p.getCodigoReferencia() : "",
                Produto::getDescricao,
                Produto::getUnidadeMedida,
                p -> "R$ " + p.getPrecoCusto(),
                p -> "R$ " + p.getPrecoVenda(),
                p -> p.getMarkupPercentual() + " %",
                Produto::getNcm,
                p -> p.isSujeitoImpostoSeletivo() ? "SIM (2026)" : "NÃO"
            )
        );

        dataTable = new ModernDataTable<>(tableModel);
        add(dataTable, BorderLayout.CENTER);

        // Eventos
        btnNovo.addActionListener(e -> {
            ProdutoFormDialog dialog = new ProdutoFormDialog(parentFrame, service, p -> recarregarProdutos());
            dialog.setVisible(true);
        });

        btnAtualizar.addActionListener(e -> recarregarProdutos());

        recarregarProdutos();
    }

    public void recarregarProdutos() {
        service.buscarProdutosAsync("")
            .thenAccept(produtos -> SwingUtilities.invokeLater(() -> dataTable.setData(produtos)));
    }
}

