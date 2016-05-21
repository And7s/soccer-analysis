package idp;

import java.awt.*;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;




import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class visMean extends JPanel {

    private int width, height;
    private float scale;
    private Font font_big, font_small;
    private  GameSection[] plotPoints;
    public visMean() {
        font_big = new Font("Segoe UI Light", Font.PLAIN, 14);
        font_small = new Font("Segoe UI Light", Font.PLAIN, 10);


    }
    public void updateData() {

        repaint();

    }

    public void paintComponent(Graphics g) {
        int steps = App.steps_mean;
        plotPoints = new GameSection[steps * 2];

        int filter = App.only_active? FILTER.ACTIVE : FILTER.ALL;

        for (int i = 0; i < steps * 2; i++) {   // the time zones (15 mins) TODO: use condig

            int start = (int)(45.0 / steps * (i % steps));
            int end = (int)(45.0 / steps * ((i % steps) +1 ));

            GameSection gs = new GameSection();
            FrameSet[] sets = idp.frameSet;

            for (int k = 0; k < sets.length; k++) {
                if (sets[k].isBall) continue;
                Player player = idp.match.getPlayer(sets[k].Object);
                boolean is_tw = player.PlayingPosition.equals("TW");
                boolean is_starting = player.Starting;
                if(App.ignore_keeper && is_tw) continue;   // dont take keeper into the dataset
                if(App.ignore_exchange && !is_starting) continue;

                if ((i < 3) == sets[k].firstHalf) { // account to the right half time
                    gs.add(sets[k].getGS(VAR.SPEED, start, end, filter));
                }
            }
            plotPoints[i] = gs;
        }





        Dimension size = getSize();
        width = size.width ;
        height = size.height;
        scale = height / 30f;
        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, width, height);
        g.setFont(font_big);

        long startTime = System.nanoTime();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(150, 150, 150));


        // labels
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);

        g2d.setStroke(dashed);

        for (int i = 0; i < 20; i +=2) {
            g2d.drawString(i+"", 10, scaleY(i));
            g2d.drawLine(20, scaleY(i), width, scaleY(i));
        }

        for (int i = 0; i < plotPoints.length; i++) {
            g2d.drawLine(scaleX(i), 0, scaleX(i), height);
        }


        g2d.setColor(Color.black);

        for (int i = 0; i < plotPoints.length; i++) {
            int x = scaleX(i);
            double val = plotPoints[i].sum / plotPoints[i].count;
            int y = scaleY(val);
            double sd = Math.sqrt(plotPoints[i].sq_sum / plotPoints[i].count - Math.pow(val, 2));
            int delta = (int)(sd * scale);

            ((Graphics2D) g).setStroke(new BasicStroke(3));
            g2d.drawArc(x -1, y -1, 2, 2, 0, 360);
            ((Graphics2D) g).setStroke(new BasicStroke(1));

            g2d.drawLine(x, y - delta / 2, x, y + delta / 2);
            g2d.drawLine(x - 2, y - delta / 2, x + 2, y - delta / 2);   // upper horizontal bar
            g2d.drawLine(x - 2, y + delta / 2, x + 2, y + delta / 2);   // lower

            // value

            g2d.drawString(String.format("%.2f", plotPoints[i].sum / plotPoints[i].count), x + 10, y);
            g.setFont(font_small);
            g2d.drawString("c: " + String.format("%4.0f", plotPoints[i].count), x, y + delta / 2 + 20);
            g.setFont(font_big);
        }


        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    public int scaleX(double x) {
        return (int)((x + 0.5f) * width / plotPoints.length);
    }

    public int scaleY(double y) {
        return (int)(height - y * scale);
    }


}