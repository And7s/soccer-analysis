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
import java.util.concurrent.Callable;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class visSpeed extends JPanel {
    MeanData[] data;
    FrameSet[] frameSet;
    Graphics2D g2d;
    private int width, height;
    //private Graphics2D g2d;
    public void updateData(MeanData[] data, FrameSet[] frameSet) {
        this.data = data;
        this.frameSet = frameSet;
        repaint();

    }

    public void plotSubset(int start, int end, int offset_y, Filter f) {

        int j = 0;  // which frameset
        Frame[] fs = frameSet[0].frames;
        float scale_x = scale_x = (float) width / (end - start);
        float scale_y = f.scale;
        System.out.println("plot " + (end-start)+" in "+width+" = "+scale_x);
        for (int i = start + 1; i < end; i++ ) {
            float val = f.Frames(fs[i]);
            g2d.setColor(f.getColor(val));
            g2d.drawLine(
                (int)((float)(i - start - 1) * scale_x),
                (int)(f.Frames(fs[i - 1]) * scale_y + offset_y),
                (int)((float)(i - start) * scale_x),
                (int)(val * scale_y + offset_y)
            );
        }
    }
public static int foo() {
    return 1;
}
    public void paintComponent(Graphics g) {
        //super.paintComponent(g);

        long startTime = System.nanoTime();
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);

        Dimension size = getSize();
        width = size.width ;
        height = size.height;


        // plot the whole game at once
        // TODO: merge couple of framesets

        Filter filter_speed = new Filter(2) {
            @Override
            float Frames(Frame f) {
                return f.S;
            }

            @Override
            Color getColor(float f) {
                if (f < 2) {
                    return Color.lightGray;
                } else if (f < 4) {
                    return Color.YELLOW;
                } else if (f < 5.5) {
                     return Color.GREEN;
                } else if (f < 7) {
                    return Color.ORANGE;
                } else {
                    return Color.RED;
                }
            }
        };

        plotSubset(0, frameSet[0].frames.length, 0, filter_speed);

        plotSubset(40, 200, 100, filter_speed);

        Filter filter_acc =  new Filter(0.6f) {
            @Override
            float Frames(Frame f) {
                return f.A;
            }
            @Override
            Color getColor(float f) {
                if (Math.abs(f) < 2) {
                    return Color.lightGray;
                } else if(Math.abs(f) < 4) {
                    return Color.GREEN;
                } else {
                    return Color.RED;
                }
            }
        };

        plotSubset(0, frameSet[0].frames.length, 200, filter_acc);

        plotSubset(40, 200, 300, filter_acc);



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


}