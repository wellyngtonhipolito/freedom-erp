package org.freedom.ui.app.view.sale;

import org.freedom.core.domain.person.Pessoa;
import org.freedom.core.domain.product.Produto;
import org.freedom.core.domain.sale.ItemPedidoVenda;
import org.freedom.core.domain.sale.PedidoVenda;
import org.freedom.core.service.person.ClienteService;
import org.freedom.core.service.product.ProdutoService;
import org.freedom.core.service.sale.PedidoVendaService;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo Moderno de Abertura e Digitação de Pedido de Venda:
 * - Seleção de Cliente e Produtos
 * - Grade de Itens com quantidade e preço
 * - Totalização automática com CBS e IBS da Reforma 2026
 */
public class PedidoVendaFormDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(PedidoVendaFormDialog.class);

    private final PedidoVendaService pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;
    private final Runnable onPedidoSalvo;

    private final List<Pessoa> clientesCarregados = new ArrayList<>();
    private final List<Produto> produtosCarregados = new ArrayList<>();
    private final List<ItemPedidoVenda> itensPedido = new ArrayList<>();

    private final JComboBox<String> cbClientes = new JComboBox<>();
    private final JTextField txtNumeroPedido = new JTextField(10);
    private final JComboBox<String> cbFormaPagamento = new JComboBox<>(new String[]{"PIX", "BOLETO", "CARTAO_CREDITO", "DINHEIRO"});

    // Itens
    private final JComboBox<String> cbProdutos = new JComboBox<>();
    private final JTextField txtQtd = new JTextField("1.0", 5);
    private final JTextField txtPrecoUnit = new JTextField(8);
    private final JButton btnAdicionarItem = new JButton("➕ Adicionar Item");

    private final DefaultTableModel tableModelItens;
    private final JTable tableItens;

    // Totais
    private final JLabel lblTotalItens = new JLabel("R$ 0,00");
    private final JLabel lblTotalCbsIbs = new JLabel("CBS/IBS (1%): R$ 0,00");
    private final JLabel lblTotalGeral = new JLabel("R$ 0,00");

    private final JButton btnSalvar = new JButton("Salvar Pedido");
    private final JButton btnCancelar = new JButton("Cancelar");

    public PedidoVendaFormDialog(JFrame parent, PedidoVendaService pedidoService, Runnable onPedidoSalvo) {
        super(parent, "Novo Pedido de Venda / Orçamento", true);
        this.pedidoService = pedidoService;
        this.clienteService = new ClienteService();
        this.produtoService = new ProdutoService();
        this.onPedidoSalvo = onPedidoSalvo;

        txtNumeroPedido.setText("PED-" + System.currentTimeMillis() % 100000);

        tableModelItens = new DefaultTableModel(new String[]{"Item", "Produto", "Qtd", "Preço Unit (R$)", "Total (R$)"}, 0);
        tableItens = new JTable(tableModelItens);
        tableItens.setRowHeight(28);

        initComponents();
        carregarCombosAsync();

        pack();
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));

        // 1. Cabeçalho do Pedido
        JPanel headerPanel = new JPanel(new GridBagLayout());
        headerPanel.setBorder(BorderFactory.createTitledBorder("Dados do Pedido"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        headerPanel.add(new JLabel("Nº Pedido:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(txtNumeroPedido, gbc);

        gbc.gridx = 2;
        headerPanel.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 2;
        headerPanel.add(cbClientes, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        headerPanel.add(new JLabel("Pagamento:"), gbc);
        gbc.gridx = 1;
        headerPanel.add(cbFormaPagamento, gbc);

        add(headerPanel, BorderLayout.NORTH);

        // 2. Painel Central (Adicionar Itens + Tabela)
        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Itens do Pedido"));

        JPanel addItemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addItemPanel.add(new JLabel("Produto:"));
        cbProdutos.setPreferredSize(new Dimension(280, 28));
        addItemPanel.add(cbProdutos);
        addItemPanel.add(new JLabel("Qtd:"));
        addItemPanel.add(txtQtd);
        addItemPanel.add(new JLabel("Preço Unit:"));
        addItemPanel.add(txtPrecoUnit);
        addItemPanel.add(btnAdicionarItem);

        centerPanel.add(addItemPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(tableItens), BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Rodapé com Totais e Botões
        JPanel footerPanel = new JPanel(new BorderLayout(12, 12));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(8, 16, 12, 16));

        JPanel totalsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        totalsPanel.add(new JLabel("Total Itens:"));
        totalsPanel.add(lblTotalItens);
        totalsPanel.add(lblTotalCbsIbs);
        totalsPanel.add(new JLabel("TOTAL GERAL:"));
        lblTotalGeral.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalGeral.setForeground(new Color(34, 139, 34));
        totalsPanel.add(lblTotalGeral);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnPanel.add(btnCancelar);
        btnPanel.add(btnSalvar);

        footerPanel.add(totalsPanel, BorderLayout.WEST);
        footerPanel.add(btnPanel, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);

        // Eventos
        cbProdutos.addActionListener(e -> atualizarPrecoProdutoSelecionado());
        btnAdicionarItem.addActionListener(e -> adicionarItem());
        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarPedido());
    }

    private void carregarCombosAsync() {
        Thread.ofVirtual().start(() -> {
            clientesCarregados.addAll(clienteService.buscarClientesAsync("").join());
            produtosCarregados.addAll(produtoService.buscarProdutosAsync("").join());

            java.awt.EventQueue.invokeLater(() -> {
                cbClientes.removeAllItems();
                for (Pessoa c : clientesCarregados) {
                    cbClientes.addItem(c.getRazaoSocial() + " (" + c.getCpfCnpj() + ")");
                }
                cbProdutos.removeAllItems();
                for (Produto p : produtosCarregados) {
                    cbProdutos.addItem(p.getDescricao() + " [R$ " + p.getPrecoVenda() + "]");
                }
                atualizarPrecoProdutoSelecionado();
            });
        });
    }

    private void atualizarPrecoProdutoSelecionado() {
        int idx = cbProdutos.getSelectedIndex();
        if (idx >= 0 && idx < produtosCarregados.size()) {
            txtPrecoUnit.setText(produtosCarregados.get(idx).getPrecoVenda().toString());
        }
    }

    private void adicionarItem() {
        int prodIdx = cbProdutos.getSelectedIndex();
        if (prodIdx < 0) return;

        try {
            Produto p = produtosCarregados.get(prodIdx);
            BigDecimal qtd = new BigDecimal(txtQtd.getText().trim().replace(",", "."));
            BigDecimal preco = new BigDecimal(txtPrecoUnit.getText().trim().replace(",", "."));

            ItemPedidoVenda item = new ItemPedidoVenda(p.getId(), itensPedido.size() + 1, qtd, preco);
            itensPedido.add(item);

            tableModelItens.addRow(new Object[]{
                item.getNumeroItem(),
                p.getDescricao(),
                qtd,
                "R$ " + preco,
                "R$ " + item.getValorTotal()
            });

            recalcularTotais();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Valores inválidos para quantidade ou preço.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void recalcularTotais() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedidoVenda item : itensPedido) {
            total = total.add(item.getValorTotal());
        }
        BigDecimal cbsIbs = total.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP); // 1% transição 2026

        lblTotalItens.setText("R$ " + total);
        lblTotalCbsIbs.setText("CBS/IBS (1%): R$ " + cbsIbs);
        lblTotalGeral.setText("R$ " + total);
    }

    private void salvarPedido() {
        if (cbClientes.getSelectedIndex() < 0) {
            JOptionPane.showMessageDialog(this, "Selecione um cliente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (itensPedido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Adicione pelo menos um item ao pedido.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Pessoa cliente = clientesCarregados.get(cbClientes.getSelectedIndex());
            PedidoVenda pedido = new PedidoVenda(1L, cliente.getId(), txtNumeroPedido.getText().trim());
            pedido.setFormaPagamento((String) cbFormaPagamento.getSelectedItem());

            pedidoService.salvarPedido(pedido, itensPedido);

            JOptionPane.showMessageDialog(this, "Pedido de Venda salvo com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            if (onPedidoSalvo != null) onPedidoSalvo.run();
            dispose();
        } catch (Exception ex) {
            log.error("Erro ao salvar pedido: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
