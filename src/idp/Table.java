package idp;
import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.RowSorter;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class Table {
    public Table(Object[][] rowData, Object[] columnNames) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        TableModel model = new DefaultTableModel(rowData, columnNames) {
            public Class getColumnClass(int column) {
                if (column >= 0 && column <= getColumnCount())
                    return getValueAt(0, column).getClass();
                else
                    return Object.class;
            }
        };

        JTable table = new JTable(model) {
            public boolean isCellEditable(int rowIndex, int vColIndex) {
                return false;
            }
        };
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setSize(300, 150);
        frame.setVisible(true);
    }
}
