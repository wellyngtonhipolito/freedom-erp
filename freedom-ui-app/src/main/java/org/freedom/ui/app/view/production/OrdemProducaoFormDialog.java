package org.freedom.ui.app.view.production;

import org.freedom.core.domain.product.Produto;
import org.freedom.core.domain.production.OrdemProducao;
import org.freedom.core.service.product.ProdutoService;
import org.freedom.core.service.production.OrdemProducaoService;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo para Abertura de Nova Ordem de Produção (OP).
 */
public class OrdemProducaoFormDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(OrdemProducaoFormDialog.class);

    private final OrdemProducaoService opService;
    private final ProdutoService produtoService;
    private final Runnable onOpSalva;

    private final List<Produto> produtos = new ArrayList<>();
    private final JTextField txtNumeroOp = new JTextField(12);
    private final JComboBox<String> cbProdutos = new JComboBox<>();
    private final JTextField txtQtdPlanejada = new JTextField("10.0", 8);
    private final JTextField txtPrevisaoFim = new JTextField(10);
    private final JTextField txtObservacao = new JTextField(20);

    private final JButton btnSalvar = new JButton("Abrir Ordem");
    private final JButton btnCancelar = new JButton("Cancelar");

    public OrdemProducaoFormDialog(JFrame parent, OrdemProducaoService opService, Runnable onOpSalva) {
        super(parent, "Nova Ordem de Produção (PCP)", true);
        this.opService = opService;
        this.produtoService = new ProdutoService();
        this.onOpSalva = onOpSalva;

        txtNumeroOp.setText("OP-" + System.currentTimeMillis() % 100000);
        txtPrevisaoFim.setText(LocalDate.now().plusDays(7).toString());

        initComponents();
        carregarProdutosAsync();

        pack();
        setMinimumSize(new Dimension(550, 340));
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
        formPanel.add(new JLabel("Nº Ordem de Produção:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNumeroOp, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Produto a Fabricar:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(cbProdutos, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Qtd. Planejada:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtQtdPlanejada, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Previsão de Término:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtPrevisaoFim, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Observações:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtObservacao, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botões
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.add(btnCancelar);
        btnPanel.add(btnSalvar);
        add(btnPanel, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarOrdem());
    }

    private void carregarProdutosAsync() {
        Thread.ofVirtual().start(() -> {
            produtos.addAll(produtoService.buscarProdutosAsync("").join());
            java.awt.EventQueue.invokeLater(() -> {
                cbProdutos.removeAllItems();
                for (Produto p : produtos) {
                    cbProdutos.addItem(p.getDescricao() + " (" + p.getUnidadeMedida() + ")");
                }
            });
        });
    }

    private void salvarOrdem() {
        int idx = cbProdutos.getSelectedIndex();
        if (idx < 0) return;

        try {
            Produto p = produtos.get(idx);
            BigDecimal qtd = new BigDecimal(txtQtdPlanejada.getText().trim().replace(",", "."));
            LocalDate previsao = LocalDate.parse(txtPrevisaoFim.getText().trim());

            OrdemProducao op = new OrdemProducao(1L, txtNumeroOp.getText().trim(), p.getId(), 1L, qtd);
            op.setDataPrevisaoFim(previsao);
            op.setObservacao(txtObservacao.getText().trim());

            opService.abrirOrdemProducao(op);

            JOptionPane.showMessageDialog(this, "Ordem de Produção aberta com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            if (onOpSalva != null) onOpSalva.run();
            dispose();
        } catch (Exception ex) {
            log.error("Erro ao abrir OP: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
