package idp;


import javax.swing.*;
import java.awt.*;


/**
 * Created by Andre on 19/04/2016.
 */
public class visualField extends JPanel {

    MeanData[] data;
    FrameSet[] frameSet;
    int width, height;
    float scale = 10f;
    public void updateData(MeanData[] data, FrameSet[] frameSet) {
        this.data = data;
        this.frameSet = frameSet;
        repaint();
    }

    public void paintComponent(Graphics g) {
        long startTime = System.nanoTime();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);

        Dimension size = getSize();
        width = size.width ;
        height = size.height;
        // update the scale
        scale = Math.min(
            (float) width / 150,
            (float) height / 100
        );


        // some random Lines
        for (int j = 0; j < frameSet.length; j++) {
            for (int i = 1; i < 1000; i++) {
                g.drawLine(
                    scaleX(frameSet[j].frames[i - 1].X),
                    scaleY(frameSet[j].frames[i - 1].Y),
                    scaleX(frameSet[j].frames[i].X),
                    scaleY(frameSet[j].frames[i].Y)
                );
            }
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    public int scaleX(float x) {
        return (int) ((x + 75) * scale) ;
    }

    public int scaleY(float y) {
        return (int) ((y+50) * scale);
    }


}
