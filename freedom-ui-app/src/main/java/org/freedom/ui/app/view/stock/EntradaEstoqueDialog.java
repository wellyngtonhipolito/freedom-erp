package org.freedom.ui.app.view.stock;

import org.freedom.core.domain.product.Produto;
import org.freedom.core.service.product.ProdutoService;
import org.freedom.core.service.stock.EstoqueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para Entrada Manual ou Compra de Mercadoria no Estoque.
 */
public class EntradaEstoqueDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(EntradaEstoqueDialog.class);

    private final EstoqueService estoqueService;
    private final ProdutoService produtoService;
    private final Runnable onEntradaConcluida;

    private final List<Produto> produtos = new ArrayList<>();
    private final JComboBox<String> cbProdutos = new JComboBox<>();
    private final JTextField txtQuantidade = new JTextField("1.0", 8);
    private final JTextField txtCustoUnitario = new JTextField(10);
    private final JTextField txtDocumento = new JTextField(12);
    private final JTextField txtObservacao = new JTextField(20);

    private final JButton btnConfirmar = new JButton("Confirmar Entrada");
    private final JButton btnCancelar = new JButton("Cancelar");

    public EntradaEstoqueDialog(JFrame parent, EstoqueService estoqueService, Runnable onEntradaConcluida) {
        super(parent, "Entrada de Mercadoria / Estoque", true);
        this.estoqueService = estoqueService;
        this.produtoService = new ProdutoService();
        this.onEntradaConcluida = onEntradaConcluida;

        initComponents();
        carregarProdutosAsync();

        pack();
        setMinimumSize(new Dimension(550, 320));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbProdutos, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtQuantidade, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Custo Unitário (R$):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtCustoUnitario, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Nº Nota / Doc Ref:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtDocumento, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtObservacao, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.add(btnCancelar);
        btnPanel.add(btnConfirmar);
        add(btnPanel, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnConfirmar.addActionListener(e -> salvarEntrada());
        cbProdutos.addActionListener(e -> atualizarCustoPadrao());
    }

    private void carregarProdutosAsync() {
        Thread.ofVirtual().start(() -> {
            produtos.addAll(produtoService.buscarProdutosAsync("").join());
            java.awt.EventQueue.invokeLater(() -> {
                cbProdutos.removeAllItems();
                for (Produto p : produtos) {
                    cbProdutos.addItem(p.getDescricao() + " (" + p.getUnidadeMedida() + ")");
                }
                atualizarCustoPadrao();
            });
        });
    }

    private void atualizarCustoPadrao() {
        int idx = cbProdutos.getSelectedIndex();
        if (idx >= 0 && idx < produtos.size()) {
            txtCustoUnitario.setText(produtos.get(idx).getPrecoCusto().toString());
        }
    }

    private void salvarEntrada() {
        int idx = cbProdutos.getSelectedIndex();
        if (idx < 0) return;

        try {
            Produto p = produtos.get(idx);
            BigDecimal qtd = new BigDecimal(txtQuantidade.getText().trim().replace(",", "."));
            BigDecimal custo = new BigDecimal(txtCustoUnitario.getText().trim().replace(",", "."));

            estoqueService.darEntradaEstoque(
                p.getId(),
                qtd,
                custo,
                txtDocumento.getText().trim(),
                txtObservacao.getText().trim()
            );

            JOptionPane.showMessageDialog(this, "Entrada de estoque e recálculo do custo médio concluídos!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            if (onEntradaConcluida != null) onEntradaConcluida.run();
            dispose();
        } catch (Exception ex) {
            log.error("Erro ao registrar entrada de estoque: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}

