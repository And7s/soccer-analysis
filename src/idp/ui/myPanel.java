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

        JSlider slider1 = new JSlider(JSlider.HORIZONTAL,0,1000,0);//direction , min , max , current

        slider1.setMajorTickSpacing(250);
        slider1.setMinorTickSpacing(25);
        slider1.setOpaque(false);
        slider1.setPaintLabels(true);
        slider1.setPaintTicks(true);
        slider1.setPaintTrack(true);
        slider1.setAutoscrolls(true);
        slider1.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                System.out.println(slider1.getValue());
                c_lines = slider1.getValue() * 10;
            }
        });
        add(slider1);

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
        Font f = new Font("Segoe UI Light", Font.PLAIN, 20);
        Font fi = new Font("Segoe UI", Font.PLAIN, 50);
        FontMetrics fm = g.getFontMetrics(f);
        FontMetrics fim = g.getFontMetrics(fi);
        Graphics2D g2d = (Graphics2D) g;

        Dimension size = getSize();



        g.setFont(f);
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawString(c_paint+"Soccer Analysis, ", 20,10);
        g.setFont(fi);
        g.drawString("w "+size.getWidth() + ", he "+size.getHeight()  , 40,50);
        int width = (int) size.getWidth(),
            height = (int) size.getHeight();
        //System.out.println("He " + height);

        // some random Lines
        for (int i = 0; i < c_lines; i++) {
            g.drawLine((int) (Math.random() * width), (int) (Math.random() * height), (int) (Math.random() * width), (int) (Math.random() * height));
        }


    } //paintComponent
}
