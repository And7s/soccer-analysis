package idp;
import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class Table extends JPanel {
    public Table(Object[][] rowData, Object[] columnNames) {
        //JFrame frame = new JFrame();
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


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
        add(scrollPane, BorderLayout.CENTER);
        setLayout(new GridLayout(1,0));
        //setSize(300, 150);
        //setVisible(true);
    }
}
