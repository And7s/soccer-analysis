package idp;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;


public class visZones extends JPanel {

    FrameSet[] frameSet;

    Graphics2D g2d;
    private int width, height;
    private GameSection[][] plotPoints;

    public void updateData(FrameSet[] frameSet) {
        System.out.println("set to "+frameSet.length);
        this.frameSet = frameSet;
        repaint();


    }

    // generate the data to be plotted
    public void analyze() {

        int steps = App.steps_mean;
        plotPoints = new GameSection[steps * 2][5];

        int filter = App.only_active? FILTER.ACTIVE : FILTER.ALL;

        for (int i = 0; i < steps * 2; i++) {   // the time zones (15 mins) TODO: use condig

            int start = (int)(45.0 / steps * (i % steps));
            int end = (int)(45.0 / steps * ((i % steps) +1 ));
            for (int j = 0; j < 5; j++) {
                GameSection gs = new GameSection();
                FrameSet[] sets = this.frameSet;

                for (int k = 0; k < sets.length; k++) {
                    if (sets[k].isBall) continue;
                    Player player = idp.game.getPlayer(sets[k].Object);
                    boolean is_tw = player.PlayingPosition.equals("TW");
                    boolean is_starting = player.Starting;
                    if(App.ignore_keeper && is_tw) continue;   // dont take keeper into the dataset
                    if(App.ignore_exchange && !is_starting) continue;

                    if ((i < steps) == sets[k].firstHalf) { // account to the right half time
                        gs.add(sets[k].getGS(VAR.SZ0 + j, start, end, filter));
                    }
                }
                plotPoints[i][j] = gs;
            }
        }

    }


    private int getMeanStart(int i) {
        int steps = App.steps_mean;
        return (int)(45.0 / steps * (i % steps));
    }
    private int getMeanEnd(int i) {
        int steps = App.steps_mean;
        return (int)(45.0 / steps * ((i % steps) +1 ));
    }

    public void paintComponent(Graphics g) {
        Dimension size = getSize();
        width = size.width ;
        height = size.height;
        paint(g, width, height);
    }

    public void paint(Graphics g, int width, int height) {
        this.width = width;
        this.height = height;
        analyze();
        //super.paintComponent(g);

        long startTime = System.nanoTime();
        g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);

        g2d.fillRect(0, 0, width, height);

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(width / 2, 0, width / 2, height);  // half time indicator

        // labels
        g2d.setColor(new Color(150, 150, 150));
        Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
        g2d.setStroke(dashed);

        for (int i = 0; i < 10; i += 2) {   // horizontal lines
            int y = height - (int)(i * height / 10f);
            g2d.drawString(i+"", 10, y);
            g2d.drawLine(20, y, width, y);
        }
        for (int i = 0; i < plotPoints.length; i++) {   // vertical lines
            g2d.drawLine(scaleX(i), 0, scaleX(i), height);
            g2d.drawString(getMeanStart(i) + "-" + getMeanEnd(i) + "min", scaleX(i) - 20, 30);
        }

        // connect dots
        g2d.setColor(Color.BLUE);
        for (int i = 1; i < plotPoints.length; i++) {
            for (int j = 0; j < plotPoints[i].length; j++) {    // loops the other way might be more effective?
                double val1 = plotPoints[i - 1][j].sum / plotPoints[i - 1][j].count / 3.6;
                double val2 = plotPoints[i][j].sum / plotPoints[i][j].count / 3.6;
                g2d.drawLine(scaleX(i - 1), scaleY(val1), scaleX(i), scaleY(val2));
            }
        }

        g2d.setColor(Color.BLACK);

        for (int i = 0; i < plotPoints.length; i++) {
            //Mean[] plot_data = idp.dat[i].speed_zones;
            GameSection[] gs = plotPoints[i];
            for (int j = 0; j < gs.length; j++) {

                int x = scaleX(i);

                double val = gs[j].sum / gs[j].count / 3.6; // which unit??

                double sd = Math.sqrt(gs[j].sq_sum / gs[j].count / 3.6 / 3.6 - val * val); // sq_sum/count - mean^2 = var

                int y = scaleY(val); // 10 is the max of the height //
                int delta = (int)(height * sd / 10f);

                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g2d.drawArc(x - 1, y -1, 2, 2, 0, 360);
                ((Graphics2D) g).setStroke(new BasicStroke(1));

                g2d.drawLine(x, y - delta / 2, x, y + delta / 2);
                g2d.drawLine(x - 2, y - delta / 2, x + 2, y - delta / 2);   // upper horizontal bar
                g2d.drawLine(x - 2, y + delta / 2, x + 2, y + delta / 2);   // lower

                g2d.drawString(String.format("%.2f", val), x + 10, y + 5);
                g2d.drawString(String.format("%4.0f", gs[j].count), x - 10, y + delta / 2 + 15);
            }
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    public int scaleX(int i) {
        return (int)((i + 0.5) * width / plotPoints.length);
    }

    public int scaleY(double y) {
        return height - (int)(y * height / 10f);
    }

}