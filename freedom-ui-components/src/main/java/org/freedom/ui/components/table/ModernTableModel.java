package org.freedom.ui.components.table;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Model genérico e fortemente tipado para a ModernDataTable.
 *
 * @param <T> Tipo do objeto de dados da linha
 */
public class ModernTableModel<T> extends AbstractTableModel {

    private final List<String> columnNames;
    private final List<Function<T, Object>> columnExtractors;
    private List<T> items;

    public ModernTableModel(List<String> columnNames, List<Function<T, Object>> columnExtractors) {
        if (columnNames.size() != columnExtractors.size()) {
            throw new IllegalArgumentException("O número de nomes de colunas deve ser igual ao número de extratores.");
        }
        this.columnNames = new ArrayList<>(columnNames);
        this.columnExtractors = new ArrayList<>(columnExtractors);
        this.items = new ArrayList<>();
    }

    public void setItems(List<T> newItems) {
        this.items = (newItems != null) ? new ArrayList<>(newItems) : new ArrayList<>();
        fireTableDataChanged();
    }

    public T getItemAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < items.size()) {
            return items.get(rowIndex);
        }
        return null;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.size();
    }

    @Override
    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        T item = items.get(rowIndex);
        return columnExtractors.get(columnIndex).apply(item);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
