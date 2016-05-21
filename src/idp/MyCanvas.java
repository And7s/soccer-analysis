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


public class MyCanvas extends JPanel {
    FrameSet[] frameSet;

    public void updateData(FrameSet[] frameSet) {
        this.frameSet = frameSet;
        repaint();

    }

    public void paintComponent(Graphics g) {
        //super.paintComponent(g);

        long startTime = System.nanoTime();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);

        Dimension size = getSize();
        int w = size.width ;
        int h = size.height;
        /*g2d.drawString(w + " - " + h, 50, 50);
        if (data != null) {
            g2d.drawString(data[0].toString(), 10, 10);

            for (int i = 0; i < data.length; i++) {
                int x = (int)((i + 0.5f) * w / data.length);
                int y = (int)(h - h * data[i].mean / 40);
                int delta = (int)(data[i].sd * h / 40);
                g2d.drawLine(x - 2, y, x + 2, y);
                g2d.drawLine(x, y - delta / 2, x, y + delta / 2);
            }
        }*/

        // plot speed
        int j = 3;

        Frame[] frames = frameSet[j].frames;
        double x1 = 0,
               y1 = frames[0].S,
              acc = 0;
        int offsety = 0;
        int dur_sprint = 0, count_sprint = 0;
        for (int i = 1; i < frames.length; i++) {
            if (frames[i].BallStatus == 1) {


                double vel_ms = frames[i].S / 3.6;
                if (vel_ms < 2) {
                    g2d.setColor(Color.lightGray);
                } else if (vel_ms < 4) {
                    g2d.setColor(Color.YELLOW);
                } else if (vel_ms < 5.5) {
                    g2d.setColor(Color.GREEN);
                } else if (vel_ms < 7) {
                    g2d.setColor(Color.ORANGE);
                } else {
                    g2d.setColor(Color.RED);
                }

                if (vel_ms >= 7) {
                    dur_sprint++;
                } else {
                    // no sprint, sprint over
                    if (dur_sprint >= 25) {
                        count_sprint++;
                        System.out.println("sprint took "+dur_sprint+"is sprint nr "+count_sprint);
                    }
                    dur_sprint = 0;
                }

                g2d.drawLine((int)(x1), (int) (y1 * 6.0f + offsety), (int)(x1 + 1), (int) (frames[i].S * 6.0f + offsety));


                // deltav is in unit deltav per frame => derive to m/s2 by dividing by time (0.04s)

                x1++;
                if (x1 > w) {
                    x1 = 0;
                    offsety += 200;
                }
                y1 = frames[i].S;
            }



        }

        //

        /*for (int i = 0; i <= 10000; i++) {
          Random r = new Random();
          int x1 = Math.abs(r.nextInt()) % w;
          int y1 = Math.abs(r.nextInt()) % h;
          int x2 = Math.abs(r.nextInt()) % w;
          int y2 = Math.abs(r.nextInt()) % h;
          g2d.drawLine(x1, y1, x2, y2);
        }*/
        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }

  
}