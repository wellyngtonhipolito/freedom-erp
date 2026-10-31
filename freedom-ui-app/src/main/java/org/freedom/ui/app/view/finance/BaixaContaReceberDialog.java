package org.freedom.ui.app.view.finance;

import org.freedom.core.domain.finance.ContaReceber;
import org.freedom.core.service.finance.CalculadoraFinanceiraService;
import org.freedom.core.service.finance.ContaReceberService;
import org.freedom.core.service.finance.EncargosAtraso;
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
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Diálogo de Baixa / Liquidação de Título a Receber.
 * Calcula encargos em tempo real e permite quitação imediata.
 */
public class BaixaContaReceberDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(BaixaContaReceberDialog.class);

    private final ContaReceber titulo;
    private final ContaReceberService service;
    private final CalculadoraFinanceiraService calculadora;
    private final Runnable onBaixaConcluida;

    private final JLabel lblDocInfo = new JLabel();
    private final JTextField txtDataBaixa = new JTextField(10);
    private final JTextField txtValorPrincipal = new JTextField(12);
    private final JTextField txtValorJuros = new JTextField(10);
    private final JTextField txtValorMulta = new JTextField(10);
    private final JTextField txtValorDesconto = new JTextField(10);
    private final JLabel lblTotalRecebido = new JLabel("R$ 0,00");
    private final JComboBox<String> cbFormaPagamento = new JComboBox<>(new String[]{"PIX", "BOLETO", "TRANSFERENCIA", "DINHEIRO", "CARTAO"});
    private final JTextField txtObservacao = new JTextField(20);

    private final JButton btnConfirmar = new JButton("Confirmar Recebimento");
    private final JButton btnCancelar = new JButton("Cancelar");

    public BaixaContaReceberDialog(JFrame parent, ContaReceber titulo, ContaReceberService service, Runnable onBaixaConcluida) {
        super(parent, "Baixa de Título a Receber #" + titulo.getId(), true);
        this.titulo = titulo;
        this.service = service;
        this.calculadora = new CalculadoraFinanceiraService();
        this.onBaixaConcluida = onBaixaConcluida;

        initComponents();
        carregarDadosIniciais();

        pack();
        setMinimumSize(new Dimension(550, 420));
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

        // Cabeçalho com dados do documento
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        lblDocInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        formPanel.add(lblDocInfo, gbc);

        // Data da Baixa
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Data do Pagamento:"), gbc);
        gbc.gridx = 1;
        txtDataBaixa.setText(LocalDate.now().toString());
        formPanel.add(txtDataBaixa, gbc);

        // Valor Principal
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Valor do Saldo (R$):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtValorPrincipal, gbc);

        // Multa e Juros por atraso
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Multa por Atraso (R$):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtValorMulta, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Juros de Mora (R$):"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtValorJuros, gbc);

        // Desconto
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Desconto Pontualidade (R$):"), gbc);
        gbc.gridx = 1;
        txtValorDesconto.setText("0.00");
        formPanel.add(txtValorDesconto, gbc);

        // Forma de Pagamento
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Forma de Pagamento:"), gbc);
        gbc.gridx = 1;
        formPanel.add(cbFormaPagamento, gbc);

        // Total Recebido em destaque
        gbc.gridx = 0; gbc.gridy = 7;
        formPanel.add(new JLabel("Total a Liquidar:"), gbc);
        gbc.gridx = 1;
        lblTotalRecebido.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalRecebido.setForeground(new Color(34, 139, 34));
        formPanel.add(lblTotalRecebido, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botões de Ação
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnConfirmar);
        add(buttonPanel, BorderLayout.SOUTH);

        btnCancelar.addActionListener(e -> dispose());
        btnConfirmar.addActionListener(e -> processarBaixa());
    }

    private void carregarDadosIniciais() {
        lblDocInfo.setText(String.format("Doc: %s | Venc: %s | Saldo: R$ %s",
            titulo.getNumeroDocumento(), titulo.getDataVencimento(), titulo.getSaldoDevedor()));

        txtValorPrincipal.setText(titulo.getSaldoDevedor().toString());

        // Calcula encargos automáticos
        EncargosAtraso encargos = calculadora.calcularEncargosAtraso(
            titulo.getSaldoDevedor(),
            titulo.getDataVencimento(),
            LocalDate.now()
        );

        txtValorMulta.setText(encargos.valorMulta().toString());
        txtValorJuros.setText(encargos.valorJuros().toString());
        lblTotalRecebido.setText("R$ " + encargos.valorTotalAtualizado());
    }

    private void processarBaixa() {
        try {
            BigDecimal principal = new BigDecimal(txtValorPrincipal.getText().trim());
            BigDecimal multa = new BigDecimal(txtValorMulta.getText().trim());
            BigDecimal juros = new BigDecimal(txtValorJuros.getText().trim());
            BigDecimal desconto = new BigDecimal(txtValorDesconto.getText().trim());
            LocalDate dataBaixa = LocalDate.parse(txtDataBaixa.getText().trim());
            String formaPagamento = (String) cbFormaPagamento.getSelectedItem();

            service.registrarBaixa(
                titulo.getId(),
                null, // Caixa geral
                dataBaixa,
                principal,
                juros,
                multa,
                desconto,
                formaPagamento,
                txtObservacao.getText().trim()
            );

            JOptionPane.showMessageDialog(this, "Título liquidado com sucesso!", "Recebimento Concluído", JOptionPane.INFORMATION_MESSAGE);
            if (onBaixaConcluida != null) {
                onBaixaConcluida.run();
            }
            dispose();
        } catch (Exception ex) {
            log.error("Erro ao registrar baixa: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, "Falha ao registrar recebimento: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}
