package idp.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Created by Andre on 16/04/2016.
 */
public class myPanel extends JPanel {
    int c_paint = 0;
    int c_lines = 0;
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

        th.start();
    }

    public void paintComponent(Graphics g) {
       // System.out.println("print "+c_paint);
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
}
