package idp;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class visSpeed extends JPanel implements MouseWheelListener {

    private static Graphics2D g2d;
    private static int width, height;
    static float zoom = 1;
    private static int height_plot = 150;

    visSpeed() {
        addMouseWheelListener(this);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        System.out.println(e.getWheelRotation());
        System.out.println(e.getPoint());

        if (e.getWheelRotation() > 0) {
            zoom /= 1.1;
        }
        if (e.getWheelRotation() < 0) {
            zoom *= 1.1;
        }
        zoom = Math.max(zoom, 1);

        // System.out.println("zoom "+zoom);
        repaint();
    }

    private static void plotSubset(Frame[] fs, int start, int end, int offset_y, VisFilter f) {

        double scale_x = (double) width / (end - start);
        double scale_y = f.scale;
        //System.out.println("plot " + (end-start)+" in "+width+" = "+scale_x);
        for (int i = start + 1; i < end; i++ ) {
            double val = f.Frames(fs[i]);
            g2d.setColor(f.getColor(val));
            g2d.drawLine(
                (int)((double)(i - start - 1) * scale_x),
                (int)(height_plot - f.Frames(fs[i - 1]) * scale_y + offset_y),
                (int)((double)(i - start) * scale_x),
                (int)(height_plot - val * scale_y + offset_y)
            );
        }
    }


    public void paintComponent(Graphics g) {
        Dimension size = getSize();
        width = size.width ;
        height = size.height;
        if (idp.frameSet.length > App.selctedFramesetIdx) {    // if there is a loaded frameSet
            paint(g, idp.frameSet[App.selctedFramesetIdx], width, height);
        }
    }
    public static void paint(Graphics g, FrameSet frameset, int width, int height) {

        visSpeed.width = width ;
        visSpeed.height = height;
        //super.paintComponent(g);

        long startTime = System.nanoTime();
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);

        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);

        // plot the whole game at once
        // TODO: merge couple of framesets

        VisFilter filter_speed = new VisFilter(10) {
            @Override
            double Frames(Frame f) {
                return (double)(f.S / 3.6);
            }

            @Override
            Color getColor(double f) {
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

        int length = frameset.frames.length;
        plotSubset(frameset.frames, 0, length, 0, filter_speed);


        plotSubset(frameset.frames, (int)((0.5 - 0.5 / zoom) * length), (int)((0.5 + 0.5 / zoom) * length), 1 * height_plot, filter_speed);

        VisFilter filter_acc =  new VisFilter(2.6f) {
            @Override
            double Frames(Frame f) {
                return f.A;
            }
            @Override
            Color getColor(double f) {
                if (Math.abs(f) < 2) {
                    return Color.lightGray;
                } else if(Math.abs(f) < 4) {
                    return Color.GREEN;
                } else {
                    return Color.RED;
                }
            }
        };

        plotSubset(frameset.frames, 0, frameset.frames.length, 2 * height_plot, filter_acc);

        plotSubset(frameset.frames, (int)((0.5 - 0.5 / zoom) * length), (int)((0.5 + 0.5 / zoom) * length), 3 * height_plot, filter_acc);



        // filter some more (calculate power consumption
        FrameSet cur_fs = frameset;
        cur_fs.analyze(idp.position.getBallFirstHalf(cur_fs.firstHalf));
        Frame[] fs = cur_fs.frames;

        int start = (int)((0.5 - 0.5 / zoom) * length);
        int end = (int)((0.5 + 0.5 / zoom) * length);
        int offset_y = 4* height_plot;
        float scale_x  = (float) width / (end - start);
        float scale_y = 8f;
        //System.out.println("plot " + (end-start)+" in "+width+" = "+scale_x);
        g2d.setColor(Color.black);
        double EC_before = 0;
        double EC_sum = 0;
        double P_sum = 0;
        double P_before = 0;
        double ec_min = 10000000;
        for (int i = start + 1; i < end; i++ ) {

            double ES = fs[i].A / 3.6 / 9.81;
            double EM = Math.sqrt(Math.pow(9.81, 2) + Math.pow(fs[i].A, 2)) / 9.81;

            double EC = (
                + 155.4 * Math.pow(ES, 5)
                - 30.4 * Math.pow(ES , 4)
                - 43.3 * Math.pow(ES, 3)
                + 46.3 * Math.pow(ES, 2)
                + 19.5 * ES
                + 3.6
                ) * EM; // unit J/kg/m
            // TODO: clairify: what about decelerations
            EC_sum += Math.abs(EC); // sum the absolute values

            double P = EC * fs[i].S / 3.6 / 25; // unit J/kg
            P_sum += P;

            /*if (EC < ec_min) {
                ec_min = EC;
                System.out.println("new min " + EC + "EC m"+EM+" slo"+ES);
            }*/

            //System.out.println("Energy cost "+EC+" EM "+EM+" POWER "+P+" ES:"+ES);

            g2d.drawLine(
                (int)((float)(i - start - 1) * scale_x),
                (int)(height_plot - EC_before * scale_y + offset_y),
                (int)((float)(i - start) * scale_x),
                (int)(height_plot - EC * scale_y + offset_y)
            );

            g2d.drawLine(
                (int)((float)(i - start - 1) * scale_x),
                (int)(height_plot - P_before * scale_y + offset_y + 100),
                (int)((float)(i - start) * scale_x),
                (int)(height_plot - P * scale_y + offset_y + 100)
            );
            EC_before = EC;
            P_before = P;
        }
        System.out.println("this player consumed ec: " + (EC_sum / 1000) + " and P: "+P_sum +" in "+(end - start) + "frams");

        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }
}