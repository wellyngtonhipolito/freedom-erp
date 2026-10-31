package org.freedom.ui.app.view.person;

import org.freedom.core.domain.person.Pessoa;
import org.freedom.core.service.person.ClienteService;
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
 * Painel completo de Gestão de Clientes:
 * - Listagem com ModernDataTable
 * - Busca assíncrona por Virtual Threads
 * - Botão de Cadastro Novo Cliente com BrasilAPI integrada
 */
public class ClienteManagementPanel extends JPanel {

    private final ClienteService clienteService;
    private final ModernDataTable<Pessoa> dataTable;
    private final ModernTableModel<Pessoa> tableModel;
    private final JFrame parentFrame;

    public ClienteManagementPanel(JFrame parentFrame, ClienteService clienteService) {
        this.parentFrame = parentFrame;
        this.clienteService = clienteService;

        setLayout(new BorderLayout(12, 12));

        // 1. Barra de Ações Superior
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        JButton btnNovoCliente = new JButton("➕ Novo Cliente");
        JButton btnAtualizar = new JButton("🔄 Recarregar");

        actionPanel.add(btnNovoCliente);
        actionPanel.add(btnAtualizar);
        add(actionPanel, BorderLayout.NORTH);

        // 2. Tabela Moderna Fortemente Tipada
        tableModel = new ModernTableModel<>(
            List.of("Código", "Razão Social / Nome", "Nome Fantasia", "CPF / CNPJ", "Cidade/UF", "Telefone", "E-mail"),
            List.of(
                p -> p.getId() != null ? p.getId().toString() : "",
                Pessoa::getRazaoSocial,
                p -> p.getNomeFantasia() != null ? p.getNomeFantasia() : "",
                Pessoa::getCpfCnpj,
                p -> (p.getNomeMunicipio() != null ? p.getNomeMunicipio() : "") + (p.getUf() != null ? "/" + p.getUf() : ""),
                p -> p.getCelularWhatsapp() != null ? p.getCelularWhatsapp() : (p.getTelefone() != null ? p.getTelefone() : ""),
                p -> p.getEmail() != null ? p.getEmail() : ""
            )
        );

        dataTable = new ModernDataTable<>(tableModel);
        add(dataTable, BorderLayout.CENTER);

        // Eventos
        btnNovoCliente.addActionListener(e -> {
            ClienteFormDialog dialog = new ClienteFormDialog(parentFrame, clienteService, p -> recarregarClientes());
            dialog.setVisible(true);
        });

        btnAtualizar.addActionListener(e -> recarregarClientes());

        // Carregamento inicial assíncrono
        recarregarClientes();
    }

    public void recarregarClientes() {
        clienteService.buscarClientesAsync("")
            .thenAccept(clientes -> SwingUtilities.invokeLater(() -> dataTable.setData(clientes)));
    }
}
