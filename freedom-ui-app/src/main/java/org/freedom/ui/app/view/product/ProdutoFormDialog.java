package org.freedom.ui.app.view.product;

import org.freedom.core.domain.product.Produto;
import org.freedom.core.service.product.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

/**
 * Diálogo de Formulário para Cadastro de Produtos:
 * - Validação de NCM/CEST
 * - Cálculo automático de Markup em tempo real
 * - Flag para Imposto Seletivo (Reforma Tributária 2026)
 */
public class ProdutoFormDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(ProdutoFormDialog.class);

    private final ProdutoService service;
    private final Consumer<Produto> onProdutoSalvo;

    private final JTextField txtReferencia = new JTextField(12);
    private final JTextField txtEan = new JTextField(14);
    private final JTextField txtDescricao = new JTextField(25);
    private final JComboBox<String> cbUnidade = new JComboBox<>(new String[]{"UN", "CX", "KG", "MT", "PC", "LT"});
    private final JTextField txtPrecoCusto = new JTextField(10);
    private final JTextField txtPrecoVenda = new JTextField(10);
    private final JLabel lblMarkup = new JLabel("0.00 %");
    private final JTextField txtNcm = new JTextField(8);
    private final JTextField txtCest = new JTextField(7);
    private final JCheckBox chkImpostoSeletivo = new JCheckBox("Sujeito ao Imposto Seletivo (Reforma 2026)");

    private final JButton btnSalvar = new JButton("Salvar Produto");
    private final JButton btnCancelar = new JButton("Cancelar");

    public ProdutoFormDialog(JFrame parent, ProdutoService service, Consumer<Produto> onProdutoSalvo) {
        super(parent, "Novo Produto — Cadastro Comercial", true);
        this.service = service;
        this.onProdutoSalvo = onProdutoSalvo;

        initComponents();
        setupCalculoMarkup();

        pack();
        setMinimumSize(new Dimension(650, 420));
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

        // Linha 0: Ref e EAN
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Cód. Referência:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtReferencia, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Código de Barras EAN:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtEan, gbc);

        // Linha 1: Descrição e Unidade
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Descrição do Item:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtDescricao, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1;
        JPanel unPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        unPanel.add(new JLabel("UN:"));
        unPanel.add(cbUnidade);
        formPanel.add(unPanel, gbc);

        // Linha 2: Custos, Venda e Markup
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Preço de Custo (R$):"), gbc);
        gbc.gridx = 1;
        txtPrecoCusto.setText("0.00");
        formPanel.add(txtPrecoCusto, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Preço de Venda (R$):"), gbc);
        gbc.gridx = 3;
        txtPrecoVenda.setText("0.00");
        formPanel.add(txtPrecoVenda, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Margem / Markup:"), gbc);
        gbc.gridx = 1;
        lblMarkup.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 13));
        lblMarkup.setForeground(new java.awt.Color(34, 139, 34));
        formPanel.add(lblMarkup, gbc);

        // Linha 4: Fiscal (NCM e CEST)
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("NCM (8 dígitos):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNcm, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("CEST (opcional):"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtCest, gbc);

        // Linha 5: Reforma 2026
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 3;
        formPanel.add(chkImpostoSeletivo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botões
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalvar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarProduto());
    }

    private void setupCalculoMarkup() {
        DocumentListener listener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { atualizarMarkup(); }
            @Override public void removeUpdate(DocumentEvent e) { atualizarMarkup(); }
            @Override public void changedUpdate(DocumentEvent e) { atualizarMarkup(); }
        };
        txtPrecoCusto.getDocument().addDocumentListener(listener);
        txtPrecoVenda.getDocument().addDocumentListener(listener);
    }

    private void atualizarMarkup() {
        try {
            BigDecimal custo = new BigDecimal(txtPrecoCusto.getText().trim().replace(",", "."));
            BigDecimal venda = new BigDecimal(txtPrecoVenda.getText().trim().replace(",", "."));
            if (custo.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal markup = venda.subtract(custo).divide(custo, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                lblMarkup.setText(String.format("%.2f %%", markup));
            } else {
                lblMarkup.setText("0.00 %");
            }
        } catch (Exception ignored) {
            lblMarkup.setText("—");
        }
    }

    private void salvarProduto() {
        try {
            Produto p = new Produto();
            p.setFilialId(1L);
            p.setCodigoReferencia(txtReferencia.getText().trim());
            p.setCodigoBarrasEan(txtEan.getText().trim());
            p.setDescricao(txtDescricao.getText().trim());
            p.setUnidadeMedida((String) cbUnidade.getSelectedItem());
            p.setPrecoCusto(new BigDecimal(txtPrecoCusto.getText().trim().replace(",", ".")));
            p.setPrecoVenda(new BigDecimal(txtPrecoVenda.getText().trim().replace(",", ".")));
            p.setNcm(txtNcm.getText().trim().replaceAll("\\D", ""));
            p.setCest(txtCest.getText().trim().replaceAll("\\D", ""));
            p.setSujeitoImpostoSeletivo(chkImpostoSeletivo.isSelected());

            service.salvarProduto(p);

            JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            if (onProdutoSalvo != null) {
                onProdutoSalvo.accept(p);
            }
            dispose();
        } catch (Exception ex) {
            log.error("Erro ao salvar produto: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }
}

