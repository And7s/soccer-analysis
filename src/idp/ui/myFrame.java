package idp.ui;

import javax.swing.*;


import java.awt.*;
import java.awt.event.*;


/**
 * Created by Andre on 16/04/2016.
 */
public class myFrame extends JFrame {
    public int count;
    private JTabbedPane tabbedPane;
    public idp.Config config;
    public myFrame() {

        setTitle("DFL Soccer Analysis | IDP | Andreas Schmelz");
        setSize(700,500); // default size is 0,0
        setLocation(10,200); // default is 0,0 (top left corner)
        Font f = new Font("Segoe UI", Font.PLAIN, 30);

        Container contentPane = getContentPane();
        contentPane.setFont(f);
        contentPane.setLayout(new GridLayout(1,0));   // The content-pane sets its layout

        tabbedPane = new JTabbedPane();


        tabbedPane.addTab("Home", null, new myPanel());

        //contentPane.add(tabbedPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        config = new idp.Config();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            tabbedPane, config);
        splitPane.setResizeWeight(1);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(400);


        Dimension minimumSize = new Dimension(100, 50);
        tabbedPane.setMinimumSize(minimumSize);
        config.setMinimumSize(minimumSize);

        contentPane.add(splitPane);

        this.show();

    }

    public void addView(Component component, String name) {
        tabbedPane.addTab(name, null, component);
    }
}
