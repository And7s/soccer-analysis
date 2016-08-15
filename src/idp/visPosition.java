package idp;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

import static idp.idp.frameSet;
import static idp.idp.position;


public class visPosition extends JPanel {

    private static double scale = 1;

    public static void paint(Graphics g, int width, int height, FrameSet fs) {
        scale = Math.min(
            (float) width / 150,
            (float) height / 100
        );

        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.WHITE);

        g2d.fillRect(0, 0, width, height);

        drawBackground(g2d, width, height);
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.BLACK);


        for (int i = 1; i < fs.frames.length; i++) {

            if (fs.frames[i - 1].BallStatus == 1) {
                //g.setColor(new Color(0, 0, 0, 0.5f));

                // scale color according to velocity

                double val = Math.min(fs.frames[i - 1].OS / 10, 2.0);
                double red = val > 1 ? val - 1:0;
                double green = 1- Math.abs(1-val);
                double blue = val < 1 ? 1 - val :0;

                g.setColor(new Color((float)red, (float)green, (float)blue, 0.5f));

            } else {
                g.setColor(new Color(0, 0, 0, 0.05f));
            }
            g.drawLine(
                scaleX(fs.frames[i - 1].X),
                scaleY(fs.frames[i - 1].Y),
                scaleX(fs.frames[i].X),
                scaleY(fs.frames[i].Y)
            );

        }
    }

    private static void drawBackground(Graphics2D g, int width, int height) {
        g.setColor(new Color(77,158,58, 100));

        g.fillRect(0, 0, width, height);

        g.setColor(Color.white);
        g.setStroke(new BasicStroke(3));
        // outline
        int left = scaleX(-105f / 2.0f),
            top = scaleY(-68.0f / 2.0f),
            right = scaleX(105f / 2.0f),
            bottom = scaleY(68f / 2.0f);

        g.drawRect(left, top, (int)(105f * scale), (int)(68f * scale));
            g.drawLine(scaleX(0), top, scaleX(0), bottom);
        int scaledCirle = (int) (18.3 * scale);
        int dotSize = (int) (0.5f * scale);
        int meterSize = (int) (1.5f * scale);
        g.drawArc(scaleX(0) - scaledCirle / 2, scaleY(0) - scaledCirle / 2, scaledCirle, scaledCirle, 0, 360);
        g.drawArc(scaleX(0) - dotSize / 2, scaleY(0) - dotSize / 2, dotSize, dotSize, 0, 360);

        // goal room
        g.drawRect(scaleX(-105f / 2), scaleY(-12.8f / 2), (int)(5.5f * scale), (int)(12.8f * scale));
        g.drawRect(scaleX(105f / 2 - 5.5f), scaleY(-12.8f / 2), (int)(5.5f * scale), (int)(12.8f * scale));
        // penalty room
        g.drawRect(scaleX(-105f / 2), scaleY(-34.8f / 2), (int)(16.5f * scale), (int)(34.8f * scale));
        g.drawRect(scaleX(105f / 2 - 16.5f), scaleY(-34.8f / 2), (int)(16.5f * scale), (int)(34.8f * scale));


        // edges
        g.drawArc(right - meterSize / 2, bottom - meterSize / 2, meterSize, meterSize, 90, 90);
        g.drawArc(right - meterSize / 2, top - meterSize / 2, meterSize, meterSize, 180, 90);
        g.drawArc(left - meterSize / 2, top - meterSize / 2, meterSize, meterSize, 270, 90);
        g.drawArc(left - meterSize / 2, bottom - meterSize / 2, meterSize, meterSize, 0, 90);
    }


    private static int scaleX(double x) {
        return (int) ((x + 75) * scale) ;
    }

    private static int scaleY(double y) {
        return (int) ((y + 50) * scale);
    }

}