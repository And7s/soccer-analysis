package idp;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class visZones extends JPanel  {
    MeanData[] data;
    FrameSet[] frameSet;
    Graphics2D g2d;
    private int width, height;

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
        g2d.setColor(Color.WHITE);

        Dimension size = getSize();
        width = size.width ;
        height = size.height;
        g2d.fillRect(0, 0, width, height);



        // labels
        g2d.setColor(new Color(150, 150, 150));
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
        g2d.setStroke(dashed);

        for (int i = 0; i < 10; i += 2) {
            int y = height - (int)(i * height / 10f);
            g2d.drawString(i+"", 10, y);
            g2d.drawLine(20, y, width, y);
        }

        g2d.setColor(Color.BLACK);


        for (int i = 0; i < idp.dat.length; i++) {
            Mean[] plot_data = idp.dat[i].speed_zones;
            for (int j = 0; j < plot_data.length; j++) {

                int x = (int)(((i + (j + 0.5) / (plot_data.length + 2))) * width / idp.dat.length);
                int y = height - (int)(plot_data[j].mean * height / 10f);
                int delta = (int)(plot_data[j].sd * height / 10f);

                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g2d.drawArc(x - 1, y -1, 2, 2, 0, 360);
                ((Graphics2D) g).setStroke(new BasicStroke(1));

                g2d.drawLine(x, y - delta / 2, x, y + delta / 2);
                g2d.drawLine(x - 2, y - delta / 2, x + 2, y - delta / 2);   // upper horizontal bar
                g2d.drawLine(x - 2, y + delta / 2, x + 2, y + delta / 2);   // lower

                g2d.drawString(String.format("%.2f", plot_data[j].mean), x + 10, y + 5);
                g2d.drawString(String.format("%4.0f", plot_data[j].count), x - 10, y + delta / 2 + 15);
            }
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    public int scaleX(int i , int j) {
        return (int)((i + 0.5f) * width / idp.dat.length);
    }

    public int scaleY(float y) {
        return (int)(height - y * 1);
    }

}