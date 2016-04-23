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


public class visSpeed extends JPanel {
    MeanData[] data;
    FrameSet[] frameSet;

    private int width, height;
    private Graphics2D g2d;
    public void updateData(MeanData[] data, FrameSet[] frameSet) {
        this.data = data;
        this.frameSet = frameSet;
        repaint();

    }

    public void paintComponent(Graphics g) {
        //super.paintComponent(g);

        long startTime = System.nanoTime();
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);

        Dimension size = getSize();
        int width = size.width ;
        int height = size.height;


        // plot the whole game at once
        // TODO: merge couple of framesets


        plotSubset(0, frameSet[0].frames.length, 0);
        plotSubset(40, 200, 100);







/*











        // plot speed


        int j = 3;

        Frame[] frames = frameSet[j].frames;
        float x1 = 0,
            y1 = frames[0].S,
            acc = 0;
        int offsety = 0;
        int dur_sprint = 0, count_sprint = 0;
        for (int i = 1; i < frames.length; i++) {
            if (frames[i].BallStatus == 1) {


                float vel_ms = frames[i].S / 3.6f;
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
                if (x1 > width) {
                    x1 = 0;
                    offsety += 200;
                }
                y1 = frames[i].S;
            }



        }
*/
        //


        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    public void plotSubset(int start, int end, int offset_y) {
        int j = 0;  // which frameset
        Frame[] fs = frameSet[j].frames;
        float scale_y = (float) width / (end - start);

        for (int i = start + 1; i < end; i++ ) {

            g2d.drawLine(
                (int)((float)(i - start - 1) * scale_y),
                (int)(fs[i - 1].S) * 2 + offset_y,
                (int)((float)(i - start) * scale_y),
                (int)(fs[i].S) * 2 + offset_y
            );
        }
    }
}