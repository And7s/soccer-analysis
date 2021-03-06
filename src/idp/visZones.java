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

    Graphics2D g2d;
    private int width, height;
    private MeanData[][] plotPoints;

    public void updateData(FrameSet[] frameSet) {
        System.out.println("set to "+frameSet.length);
        repaint();
    }

    // generate the data to be plotted
    private void analyze() {

        int steps = Config.steps_mean;
        plotPoints = new MeanData[steps * 2][5];

        int filter = Config.only_active? FILTER.ACTIVE : FILTER.ALL;

        for (int i = 0; i < steps * 2; i++) {
            int start = (int)(45.0 / steps * (i % steps));
            int end = (int)(45.0 / steps * ((i % steps) +1 ));
            for (int j = 0; j < 5; j++) {
                MeanData md = new MeanData();
                FrameSet[] sets = idp.frameSet;

                for (int k = 0; k < sets.length; k++) {
                    if (sets[k].isBall) continue;
                    if (Config.ignore_officials && sets[k].noTeam) continue;
                    Player player = idp.game.getPlayer(sets[k].Object);
                    boolean is_tw = player.PlayingPosition.equals("TW");
                    boolean is_starting = player.Starting;
                    if(Config.ignore_keeper && is_tw) continue;   // dont take keeper into the dataset
                    if(Config.ignore_exchange && !is_starting) continue;

                    if ((i < steps) == sets[k].firstHalf) { // account to the right half time
                        md.add(sets[k].getMD(VAR.SZ0 + j, start, end, filter));
                    }
                }
                plotPoints[i][j] = md;
            }
        }

    }


    private int getMeanStart(int i) {
        int steps = Config.steps_mean;
        return (int)(45.0 / steps * (i % steps));
    }
    private int getMeanEnd(int i) {
        int steps = Config.steps_mean;
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

        for (int i = 0; i < 150; i += 20) {   // horizontal lines
            int y = scaleY(i);
            g2d.drawString(i+" [m/min]", 10, y - 5);
            g2d.drawLine(0, y, width, y);
        }
        for (int i = 0; i < plotPoints.length; i++) {   // vertical lines
            g2d.drawLine(scaleX(i), 30, scaleX(i), height);
            g2d.drawString(getMeanStart(i) + "-" + getMeanEnd(i) + "min", scaleX(i) - 20, 15);
        }

        // connect dots
        g2d.setColor(Color.BLUE);
        for (int i = 1; i < plotPoints.length; i++) {
            for (int j = 0; j < plotPoints[i].length; j++) {    // loops the other way might be more effective?
                double val1 = plotPoints[i - 1][j].sum / plotPoints[i - 1][j].count / 3.6 * 60;
                double val2 = plotPoints[i][j].sum / plotPoints[i][j].count / 3.6 * 60;
                g2d.drawLine(scaleX(i - 1), scaleY(val1), scaleX(i), scaleY(val2));
            }
        }

        g2d.setColor(Color.BLACK);

        for (int i = 0; i < plotPoints.length; i++) {
            //Mean[] plot_data = idp.dat[i].speed_zones;
            MeanData[] md = plotPoints[i];
            for (int j = 0; j < md.length; j++) {

                int x = scaleX(i);

                double val = md[j].sum / md[j].count / 3.6 * 60; // m/min

                double sd = Math.sqrt(md[j].sq_sum / md[j].count / (3.6*60) / (3.6*60) - val * val); // sq_sum/count - mean^2 = var

                int y = scaleY(val); // 10 is the max of the height //
                int delta = (int)(height * sd / 10f);

                ((Graphics2D) g).setStroke(new BasicStroke(3));
                g2d.drawArc(x - 1, y -1, 2, 2, 0, 360);
                ((Graphics2D) g).setStroke(new BasicStroke(1));

                g2d.drawLine(x, y - delta / 2, x, y + delta / 2);
                g2d.drawLine(x - 2, y - delta / 2, x + 2, y - delta / 2);   // upper horizontal bar
                g2d.drawLine(x - 2, y + delta / 2, x + 2, y + delta / 2);   // lower

                g2d.drawString(String.format("%.2f sz:"+j, val), x + 10, y + 5);
                g2d.drawString(String.format("%4.0f", md[j].count), x - 10, y + delta / 2 + 15);
            }
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    private int scaleX(int i) {
        return (int)((i + 0.5) * width / plotPoints.length);
    }

    private int scaleY(double y) {
        return height - (int)(y * height / 200f);
    }

}