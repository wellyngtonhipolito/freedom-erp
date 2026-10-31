package org.freedom.ui.app;

import org.freedom.ui.components.card.ModernCard;
import org.freedom.ui.components.table.ModernDataTable;
import org.freedom.ui.components.table.ModernTableModel;
import org.freedom.ui.components.theme.ThemeManager;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

/**
 * Janela Executiva e Ponto de Entrada da Interface Gráfica Moderna do Freedom ERP.
 */
public class FreedomMainApp extends JFrame {

    public record TituloDemonstracao(String documento, String cliente, String vencimento, String valor, String status) {}

    public FreedomMainApp() {
        super("Freedom ERP — Enterprise Management (Modernizado Java 25 & FlatLaf)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(16, 16));

        // 1. Barra de Topo com Alternador de Tema Dark/Light
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel lblHeader = new JLabel("  Dashboard Corporativo & Gestão Integrada");
        lblHeader.putClientProperty("FlatLaf.styleClass", "h2");

        JButton btnToggleTheme = new JButton("🌓 Alternar Tema (Claro / Escuro)");
        btnToggleTheme.addActionListener(e -> ThemeManager.toggleTheme(this));

        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerActions.add(btnToggleTheme);

        headerPanel.add(lblHeader, BorderLayout.WEST);
        headerPanel.add(headerActions, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Painel Central (KPIs + ModernDataTable)
        JPanel centerPanel = new JPanel(new BorderLayout(16, 16));

        // 2.1 Cards de KPIs
        JPanel kpiGrid = new JPanel(new GridLayout(1, 3, 16, 16));
        kpiGrid.add(new ModernCard("Faturamento Mensal (Out/2026)", "R$ 482.350,00", "▲ +14,2% vs mês anterior", true));
        kpiGrid.add(new ModernCard("Contas a Receber (Vencidas)", "R$ 18.240,00", "▼ -4,8% de inadimplência", true));
        kpiGrid.add(new ModernCard("CBS / IBS Apurado (Transição)", "R$ 4.823,50", "Transição EC 132/23 (1%)", true));
        centerPanel.add(kpiGrid, BorderLayout.NORTH);

        // 2.2 Tabela de Títulos
        ModernTableModel<TituloDemonstracao> tableModel = new ModernTableModel<>(
            List.of("Nº Documento", "Cliente / Razão Social", "Data Vencimento", "Valor Nominal", "Status"),
            List.of(
                TituloDemonstracao::documento,
                TituloDemonstracao::cliente,
                TituloDemonstracao::vencimento,
                TituloDemonstracao::valor,
                TituloDemonstracao::status
            )
        );

        ModernDataTable<TituloDemonstracao> dataTable = new ModernDataTable<>(tableModel);
        dataTable.setData(List.of(
            new TituloDemonstracao("DOC-10291", "Distribuidora Nacional de Bebidas S/A", "31/10/2026", "R$ 12.450,00", "PENDENTE"),
            new TituloDemonstracao("DOC-10292", "Indústria Metalúrgica Alvorada Ltda", "02/11/2026", "R$ 8.920,00", "PENDENTE"),
            new TituloDemonstracao("DOC-10293", "Comércio e Logística Sudeste Eireli", "05/11/2026", "R$ 34.180,00", "QUITADO"),
            new TituloDemonstracao("DOC-10294", "Tecnologia e Automação Brasil S/A", "10/11/2026", "R$ 5.600,00", "PENDENTE")
        ));

        centerPanel.add(dataTable, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        // Inicializa o FlatLaf antes de instanciar componentes
        ThemeManager.setup();

        SwingUtilities.invokeLater(() -> {
            FreedomMainApp app = new FreedomMainApp();
            app.setVisible(true);
        });
    }
}

