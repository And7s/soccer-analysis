package idp;
import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class Table extends JPanel {
    DefaultTableModel model;

    public Table(Object[][] rowData, Object[] columnNames) {
        model = new DefaultTableModel(rowData, columnNames);

        JTable table = new JTable(model) {
            public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
            }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        setLayout(new GridLayout(1,0));
    }

    public void update(Object[][] rows) {
        int numRem = model.getRowCount();
        for (int i = 0; i < numRem; i++) {
            model.removeRow(0);
        }
        for (int i = 0; i < rows.length; i++) {
            System.out.println("ins i "+i+"has "+model.getRowCount());
            model.addRow(rows[i]);
        }
    }
}
