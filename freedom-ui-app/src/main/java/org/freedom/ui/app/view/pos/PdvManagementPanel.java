package org.freedom.ui.app.view.pos;

import org.freedom.core.domain.pos.CaixaSessao;
import org.freedom.core.domain.pos.VendaPdv;
import org.freedom.core.domain.product.Produto;
import org.freedom.core.service.pos.PdvService;
import org.freedom.core.service.product.ProdutoService;
import org.freedom.ui.components.table.ModernDataTable;
import org.freedom.ui.components.table.ModernTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Interface Moderna do Ponto de Venda (PDV) / Frente de Caixa:
 * - Abertura e controle de sessão de caixa
 * - Venda rápida de balcão com leitor/combobox de produtos
 * - Cálculo automático de troco em tempo real
 * - Destaque dos impostos da Reforma 2026 (CBS 0.9% e IBS 0.1%)
 */
public class PdvManagementPanel extends JPanel {

    private static final Logger log = LoggerFactory.getLogger(PdvManagementPanel.class);

    private final PdvService pdvService;
    private final ProdutoService produtoService;
    private final JFrame parentFrame;

    private CaixaSessao caixaAtual;
    private final List<Produto> produtosCadastrados = new ArrayList<>();
    private final List<Produto> itensVendaAtual = new ArrayList<>();

    // Componentes de Topo (Status do Caixa)
    private final JLabel lblStatusCaixa = new JLabel("Status: VERIFICANDO...");
    private final JButton btnAbrirCaixa = new JButton("🔓 Abrir Caixa");

    // Componentes da Venda
    private final JComboBox<String> cbProdutos = new JComboBox<>();
    private final JTextField txtQtd = new JTextField("1.0", 5);
    private final JButton btnAdicionar = new JButton("➕ Bipar / Adicionar");

    private final DefaultTableModel tableModelVenda;
    private final JTable tableItensVenda;

    // Totais e Pagamento
    private final JLabel lblSubtotal = new JLabel("R$ 0,00");
    private final JLabel lblCbsIbs = new JLabel("CBS/IBS (1%): R$ 0,00");
    private final JLabel lblTotalPagar = new JLabel("R$ 0,00");

    private final JTextField txtValorRecebido = new JTextField(10);
    private final JComboBox<String> cbFormaPagto = new JComboBox<>(new String[]{"DINHEIRO", "PIX", "CARTAO_DEBITO", "CARTAO_CREDITO"});
    private final JLabel lblTroco = new JLabel("Troco: R$ 0,00");
    private final JButton btnFinalizarVenda = new JButton("💳 Finalizar Venda (Enter)");

    public PdvManagementPanel(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.pdvService = new PdvService();
        this.produtoService = new ProdutoService();

        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        tableModelVenda = new DefaultTableModel(new String[]{"Item", "Código/EAN", "Descrição", "Qtd", "Preço Unit.", "Total"}, 0);
        tableItensVenda = new JTable(tableModelVenda);
        tableItensVenda.setRowHeight(30);

        initComponents();
        carregarStatusCaixa();
        carregarProdutosAsync();
    }

    private void initComponents() {
        // 1. Barra de Topo com Sessão de Caixa
        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        lblStatusCaixa.setFont(new Font("Segoe UI", Font.BOLD, 15));
        topBar.add(lblStatusCaixa, BorderLayout.WEST);

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topActions.add(btnAbrirCaixa);
        topBar.add(topActions, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 2. Painel Central (Entrada de Itens + Grade de Venda)
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        
        JPanel itemInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        itemInputPanel.setBorder(BorderFactory.createTitledBorder("Adicionar Item à Venda"));
        itemInputPanel.add(new JLabel("Produto:"));
        cbProdutos.setPreferredSize(new Dimension(320, 28));
        itemInputPanel.add(cbProdutos);
        itemInputPanel.add(new JLabel("Qtd:"));
        itemInputPanel.add(txtQtd);
        itemInputPanel.add(btnAdicionar);

        centerPanel.add(itemInputPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(tableItensVenda), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Painel Lateral Direito (Totais, Pagamento e Troco)
        JPanel checkoutPanel = new JPanel(new GridBagLayout());
        checkoutPanel.setPreferredSize(new Dimension(340, 0));
        checkoutPanel.setBorder(BorderFactory.createTitledBorder("Fechamento do Cupom"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Subtotal
        gbc.gridx = 0; gbc.gridy = 0;
        checkoutPanel.add(new JLabel("Subtotal:"), gbc);
        gbc.gridx = 1;
        checkoutPanel.add(lblSubtotal, gbc);

        // Reforma 2026
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        lblCbsIbs.setForeground(new Color(100, 110, 120));
        checkoutPanel.add(lblCbsIbs, gbc);

        // Total a Pagar em Destaque
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        checkoutPanel.add(new JLabel("TOTAL A PAGAR:"), gbc);
        gbc.gridx = 1;
        lblTotalPagar.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalPagar.setForeground(new Color(34, 139, 34));
        checkoutPanel.add(lblTotalPagar, gbc);

        // Forma de Pagamento
        gbc.gridx = 0; gbc.gridy = 3;
        checkoutPanel.add(new JLabel("Forma de Pagto:"), gbc);
        gbc.gridx = 1;
        checkoutPanel.add(cbFormaPagto, gbc);

        // Valor Recebido
        gbc.gridx = 0; gbc.gridy = 4;
        checkoutPanel.add(new JLabel("Valor Recebido:"), gbc);
        gbc.gridx = 1;
        txtValorRecebido.setFont(new Font("Segoe UI", Font.BOLD, 15));
        checkoutPanel.add(txtValorRecebido, gbc);

        // Troco
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        lblTroco.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTroco.setForeground(new Color(0, 102, 204));
        checkoutPanel.add(lblTroco, gbc);

        // Botão Finalizar
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        btnFinalizarVenda.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFinalizarVenda.setPreferredSize(new Dimension(0, 42));
        checkoutPanel.add(btnFinalizarVenda, gbc);

        add(checkoutPanel, BorderLayout.EAST);

        // Eventos
        btnAbrirCaixa.addActionListener(e -> abrirNovoCaixa());
        btnAdicionar.addActionListener(e -> adicionarItemVenda());
        btnFinalizarVenda.addActionListener(e -> finalizarVenda());
        txtValorRecebido.addActionListener(e -> calcularTroco());
    }

    private void carregarStatusCaixa() {
        Thread.ofVirtual().start(() -> {
            Optional<CaixaSessao> aberto = pdvService.obterCaixaAberto(1);
            SwingUtilities.invokeLater(() -> {
                if (aberto.isPresent()) {
                    caixaAtual = aberto.get();
                    lblStatusCaixa.setText("🟢 Terminal #1: ABERTO | Operador: " + caixaAtual.getOperadorNome());
                    lblStatusCaixa.setForeground(new Color(34, 139, 34));
                    btnAbrirCaixa.setEnabled(false);
                    btnFinalizarVenda.setEnabled(true);
                } else {
                    caixaAtual = null;
                    lblStatusCaixa.setText("🔴 Terminal #1: CAIXA FECHADO");
                    lblStatusCaixa.setForeground(new Color(220, 20, 60));
                    btnAbrirCaixa.setEnabled(true);
                    btnFinalizarVenda.setEnabled(false);
                }
            });
        });
    }

    private void carregarProdutosAsync() {
        Thread.ofVirtual().start(() -> {
            produtosCadastrados.addAll(produtoService.buscarProdutosAsync("").join());
            SwingUtilities.invokeLater(() -> {
                cbProdutos.removeAllItems();
                for (Produto p : produtosCadastrados) {
                    cbProdutos.addItem(p.getDescricao() + " [R$ " + p.getPrecoVenda() + "]");
                }
            });
        });
    }

    private void abrirNovoCaixa() {
        String operador = JOptionPane.showInputDialog(parentFrame, "Nome do Operador de Caixa:", "Operador 01");
        if (operador != null && !operador.isBlank()) {
            String fundoStr = JOptionPane.showInputDialog(parentFrame, "Valor do Fundo de Troco (R$):", "100.00");
            try {
                BigDecimal fundo = new BigDecimal(fundoStr != null ? fundoStr.trim().replace(",", ".") : "0.00");
                pdvService.abrirCaixa(1, operador.trim(), fundo);
                JOptionPane.showMessageDialog(parentFrame, "Caixa aberto com sucesso!", "PDV Pronto", JOptionPane.INFORMATION_MESSAGE);
                carregarStatusCaixa();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(parentFrame, "Falha ao abrir caixa: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void adicionarItemVenda() {
        if (caixaAtual == null) {
            JOptionPane.showMessageDialog(this, "Abra o caixa antes de iniciar vendas.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idx = cbProdutos.getSelectedIndex();
        if (idx < 0) return;

        try {
            Produto p = produtosCadastrados.get(idx);
            BigDecimal qtd = new BigDecimal(txtQtd.getText().trim().replace(",", "."));
            BigDecimal totalItem = p.getPrecoVenda().multiply(qtd).setScale(2, RoundingMode.HALF_UP);

            itensVendaAtual.add(p);

            tableModelVenda.addRow(new Object[]{
                itensVendaAtual.size(),
                p.getCodigoBarrasEan() != null ? p.getCodigoBarrasEan() : p.getCodigoReferencia(),
                p.getDescricao(),
                qtd,
                "R$ " + p.getPrecoVenda(),
                "R$ " + totalItem
            });

            recalcularTotaisVenda();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recalcularTotaisVenda() {
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < tableModelVenda.getRowCount(); i++) {
            String valStr = tableModelVenda.getValueAt(i, 5).toString().replace("R$ ", "").replace(",", ".");
            total = total.add(new BigDecimal(valStr));
        }

        BigDecimal cbsIbs = total.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP); // 1% Reforma 2026

        lblSubtotal.setText("R$ " + total);
        lblCbsIbs.setText("CBS/IBS (1%): R$ " + cbsIbs);
        lblTotalPagar.setText("R$ " + total);
        txtValorRecebido.setText(total.toString());
        calcularTroco();
    }

    private void calcularTroco() {
        try {
            String totalStr = lblTotalPagar.getText().replace("R$ ", "").replace(",", ".");
            BigDecimal total = new BigDecimal(totalStr);
            BigDecimal recebido = new BigDecimal(txtValorRecebido.getText().trim().replace(",", "."));

            if (recebido.compareTo(total) >= 0) {
                BigDecimal troco = recebido.subtract(total);
                lblTroco.setText(String.format("Troco: R$ %.2f", troco));
                lblTroco.setForeground(new Color(0, 102, 204));
            } else {
                lblTroco.setText("Valor Insuficiente");
                lblTroco.setForeground(new Color(220, 20, 60));
            }
        } catch (Exception ignored) {
            lblTroco.setText("Troco: R$ 0,00");
        }
    }

    private void finalizarVenda() {
        if (itensVendaAtual.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhum item adicionado à venda.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String totalStr = lblTotalPagar.getText().replace("R$ ", "").replace(",", ".");
            BigDecimal total = new BigDecimal(totalStr);
            BigDecimal recebido = new BigDecimal(txtValorRecebido.getText().trim().replace(",", "."));
            String formaPagto = (String) cbFormaPagto.getSelectedItem();

            String numeroCupom = "CUPOM-" + System.currentTimeMillis() % 100000;
            List<Long> produtosIds = itensVendaAtual.stream().map(Produto::getId).toList();

            VendaPdv venda = pdvService.registrarVendaRapida(
                caixaAtual.getId(),
                numeroCupom,
                total,
                recebido,
                formaPagto,
                produtosIds
            );

            JOptionPane.showMessageDialog(this,
                String.format("Venda Concluída com Sucesso!\nNº Cupom: %s\nTotal: R$ %s\nTroco: R$ %s\nForma: %s",
                    numeroCupom, total, venda.getValorTroco(), formaPagto),
                "NFC-e Emitida", JOptionPane.INFORMATION_MESSAGE);

            // Limpa venda atual para a próxima
            itensVendaAtual.clear();
            tableModelVenda.setRowCount(0);
            recalcularTotaisVenda();
            txtValorRecebido.setText("0.00");
            lblTroco.setText("Troco: R$ 0,00");
        } catch (Exception ex) {
            log.error("Erro ao finalizar venda: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro no PDV", JOptionPane.ERROR_MESSAGE);
        }
    }
}

