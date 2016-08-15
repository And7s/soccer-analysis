package idp;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.File;
import java.util.Vector;

import javax.swing.JPanel;

import static idp.idp.*;


/**
 * Created by Andre on 16/04/2016.
 */
public class myPanel extends JPanel {
    int c_paint = 0;

    Vector<String> files;
    public myPanel() {

        Thread th = new Thread() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    repaint();
                    try {
                        sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        JButton button = new JButton("select Rroot dir");
        button.addActionListener(e -> {
            System.out.println("clicked open");
            openDialog();
        });
        add(button);
        th.start();
    }

    public void paintComponent(Graphics g) {
        c_paint++;
        super.paintComponent(g);
        Font f = new Font("Segoe UI Light", Font.PLAIN, 50);

        Graphics2D g2d = (Graphics2D) g;

        Dimension size = getSize();
        int width = (int) size.getWidth(),
            height =  (int) size.getHeight();

        g.setFont(f);
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawString("Soccer Analysis", width / 2 - 160, height / 3 );
        g.fillRect(0, height / 2 - 10, c_paint, 20);
    }

    private void openDialog() {

        File root_dir;
        JFileChooser fc = new JFileChooser("select Root dir");
        fc.setCurrentDirectory(new java.io.File(".")); // start at application current directory
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            root_dir = fc.getSelectedFile();

            vis_batch = new visBatch(root_dir.getAbsolutePath()+'\\');
            my_frame.addView(vis_batch, "batch");

        }
    }
}
