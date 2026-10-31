package org.freedom.ui.components.table;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

/**
 * Tabela moderna do Freedom ERP baseada em Swing + FlatLaf:
 * - Filtro de busca global em tempo real
 * - Linhas confortáveis com hover
 * - Barra de paginação/totalizador
 * - Desacoplamento completo de chamadas diretas a banco de dados
 *
 * @param <T> Tipo de objeto representado na linha
 */
public class ModernDataTable<T> extends JPanel {

    private final JTable table;
    private final ModernTableModel<T> model;
    private final TableRowSorter<ModernTableModel<T>> sorter;
    private final JTextField txtSearch;
    private final JLabel lblRecordCount;

    public ModernDataTable(ModernTableModel<T> model) {
        this.model = model;
        this.sorter = new TableRowSorter<>(model);

        setLayout(new BorderLayout(8, 8));

        // 1. Barra Superior de Pesquisa e Ações
        JPanel topBar = new JPanel(new BorderLayout(8, 8));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        
        searchPanel.add(new JLabel("🔍"));
        txtSearch = new JTextField(24);
        txtSearch.putClientProperty("JTextField.placeholderText", "Digite para filtrar registros em tempo real...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        searchPanel.add(txtSearch);
        topBar.add(searchPanel, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);

        // 2. Tabela Central com Estilo Moderno
        table = new JTable(model);
        table.setRowSorter(sorter);
        table.setRowHeight(32);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Barra Inferior de Totalização
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 4));
        lblRecordCount = new JLabel("Registros: 0");
        bottomBar.add(lblRecordCount);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void applyFilter() {
        String text = txtSearch.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
        updateRecordCount();
    }

    public void setData(List<T> items) {
        model.setItems(items);
        updateRecordCount();
    }

    public T getSelectedItem() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            return model.getItemAt(modelRow);
        }
        return null;
    }

    public JTable getTable() {
        return table;
    }

    private void updateRecordCount() {
        lblRecordCount.setText(String.format("Registros: %d de %d", table.getRowCount(), model.getRowCount()));
    }
}

