package org.freedom.ui.app.view.person;

import org.freedom.core.domain.person.Pessoa;
import org.freedom.core.service.person.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.function.Consumer;

/**
 * Diálogo de Formulário para Cadastro e Edição de Clientes.
 * Possui auto-resolução de endereço via BrasilAPI em Virtual Thread.
 */
public class ClienteFormDialog extends JDialog {

    private static final Logger log = LoggerFactory.getLogger(ClienteFormDialog.class);

    private final ClienteService clienteService;
    private final Consumer<Pessoa> onClienteSalvo;

    private final JTextField txtRazaoSocial = new JTextField(25);
    private final JTextField txtNomeFantasia = new JTextField(20);
    private final JTextField txtCpfCnpj = new JTextField(15);
    private final JTextField txtCep = new JTextField(9);
    private final JTextField txtLogradouro = new JTextField(25);
    private final JTextField txtNumero = new JTextField(6);
    private final JTextField txtBairro = new JTextField(15);
    private final JTextField txtCidade = new JTextField(15);
    private final JTextField txtUf = new JTextField(3);
    private final JTextField txtTelefone = new JTextField(12);
    private final JTextField txtEmail = new JTextField(20);

    private final JButton btnBuscarCep = new JButton("Buscar CEP");
    private final JButton btnSalvar = new JButton("Salvar Cliente");
    private final JButton btnCancelar = new JButton("Cancelar");

    public ClienteFormDialog(JFrame parent, ClienteService clienteService, Consumer<Pessoa> onClienteSalvo) {
        super(parent, "Novo Cliente — Cadastro Unificado", true);
        this.clienteService = clienteService;
        this.onClienteSalvo = onClienteSalvo;

        initComponents();
        setupEvents();

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

        // 1. Dados Principais
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Razão Social / Nome:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtRazaoSocial, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Nome Fantasia:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtNomeFantasia, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("CPF / CNPJ:"), gbc);
        gbc.gridx = 3;
        formPanel.add(txtCpfCnpj, gbc);

        // 2. Endereço e CEP
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("CEP:"), gbc);
        
        JPanel cepPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        cepPanel.add(txtCep);
        cepPanel.add(btnBuscarCep);
        gbc.gridx = 1;
        formPanel.add(cepPanel, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Logradouro (Rua/Av):"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(txtLogradouro, gbc);

        gbc.gridx = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Nº:"), gbc);
        gbc.gridx = 4;
        formPanel.add(txtNumero, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Bairro:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtBairro, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("Cidade / UF:"), gbc);
        JPanel cidadeUfPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        cidadeUfPanel.add(txtCidade);
        cidadeUfPanel.add(txtUf);
        gbc.gridx = 3; gbc.gridwidth = 2;
        formPanel.add(cidadeUfPanel, gbc);

        // 3. Contato
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Telefone / Whats:"), gbc);
        gbc.gridx = 1;
        formPanel.add(txtTelefone, gbc);

        gbc.gridx = 2;
        formPanel.add(new JLabel("E-mail:"), gbc);
        gbc.gridx = 3; gbc.gridwidth = 2;
        formPanel.add(txtEmail, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Botões de Ação
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.add(btnCancelar);
        buttonPanel.add(btnSalvar);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupEvents() {
        btnBuscarCep.addActionListener(e -> buscarCepAsync());
        btnCancelar.addActionListener(e -> dispose());
        btnSalvar.addActionListener(e -> salvarCliente());
    }

    private void buscarCepAsync() {
        String cep = txtCep.getText().trim();
        if (cep.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe um CEP para pesquisar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnBuscarCep.setEnabled(false);
        btnBuscarCep.setText("Consultando...");

        // Execução em Virtual Thread do Java 25 para não travar a UI
        Thread.ofVirtual().start(() -> {
            Pessoa temp = new Pessoa();
            clienteService.preencherEnderecoPorCep(temp, cep);

            SwingUtilities.invokeLater(() -> {
                btnBuscarCep.setEnabled(true);
                btnBuscarCep.setText("Buscar CEP");

                if (temp.getLogradouro() != null) {
                    txtLogradouro.setText(temp.getLogradouro());
                    txtBairro.setText(temp.getBairro());
                    txtCidade.setText(temp.getNomeMunicipio());
                    txtUf.setText(temp.getUf());
                } else {
                    JOptionPane.showMessageDialog(this, "CEP não encontrado na BrasilAPI.", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                }
            });
        });
    }

    private void salvarCliente() {
        try {
            Pessoa p = new Pessoa();
            p.setFilialId(1L); // Filial padrão matriz
            p.setTipoPessoa(txtCpfCnpj.getText().length() > 14 ? "PJ" : "PF");
            p.setRazaoSocial(txtRazaoSocial.getText().trim());
            p.setNomeFantasia(txtNomeFantasia.getText().trim());
            p.setCpfCnpj(txtCpfCnpj.getText().trim());
            p.setCep(txtCep.getText().trim());
            p.setLogradouro(txtLogradouro.getText().trim());
            p.setNumero(txtNumero.getText().trim());
            p.setBairro(txtBairro.getText().trim());
            p.setNomeMunicipio(txtCidade.getText().trim());
            p.setUf(txtUf.getText().trim());
            p.setTelefone(txtTelefone.getText().trim());
            p.setEmail(txtEmail.getText().trim());

            clienteService.salvarCliente(p);

            JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            if (onClienteSalvo != null) {
                onClienteSalvo.accept(p);
            }
            dispose();
        } catch (Exception ex) {
            log.error("Erro ao salvar cliente: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Validação", JOptionPane.ERROR_MESSAGE);
        }
    }
}
