package idp;

import javax.swing.*;
import java.awt.*;

import static idp.idp.game;
import static java.lang.Thread.sleep;


/**
 * Created by Andre on 19/04/2016.
 */
public class visualField extends JPanel {

    Graphics g;

    int width, height;
    float scale = 10f;
    int cur_pos = 0;
    boolean show_first_half = true;

    public visualField() {
        new Thread(() -> {
            System.out.println("Inner Thread");

            while (!Thread.currentThread().isInterrupted()) {
                update();
                try {
                    sleep(32);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private void update() {
        if (idp.frameSet == null ||idp.position == null) return;
        cur_pos += Config.playback_speed;
        int amount = idp.position.getBallFirstHalf(show_first_half).frames.length;  // how long is this half
        if (cur_pos >= amount) {
            cur_pos = 0;
            show_first_half = !show_first_half;
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
        FrameSet[] frameSet = idp.frameSet;
        Position position = idp.position;

        // System.out.println("update"+cur_pos);
        this.g = g;
        long startTime = System.nanoTime();
        Graphics2D g2d = (Graphics2D) g;
        int draw_frames_count = 100,
            steps_increase_alpha = 10;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.BLACK);

        Dimension size = getSize();
        width = size.width ;
        height = size.height;
        // update the scale
        scale = Math.min(
            (float) width / 150,
            (float) height / 100
        );
        drawBackground();
        ((Graphics2D) g).setStroke(new BasicStroke(1));


        // some random Lines
        FrameSet ball_frames = position.getBallFirstHalf(show_first_half);

        g2d.drawString("frame:" + cur_pos + ", "+(cur_pos / 25 / 60) + "min", 5, 30);
        g2d.drawString("Ballstatus" + ball_frames.frames[cur_pos].BallStatus, 5, 60);
        g2d.drawString("BallPosession" + ball_frames.frames[cur_pos].BallPossession, 5, 90);

        String first_club = "";

        int col_r, col_g, col_b;
        for (int j = 0; j < frameSet.length; j++) {
            g.setColor(new Color(0,0, 0, 0.05f));
            if (!frameSet[j].Club.equals(ball_frames.Club)) {
                if (first_club.equals("") || frameSet[j].Club.equals(first_club)) {
                    first_club = frameSet[j].Club;
                    col_r = 1; col_g = 0; col_b = 0;
                } else {
                    col_r = 0; col_g = 0; col_b = 1;
                }
            } else {
                col_r = 0; col_g = 0; col_b = 0;
            }
            if (frameSet[j].firstHalf == show_first_half) { // if this half is currently shown
                int offset = ball_frames.frames[0].N - frameSet[j].frames[0].N;  // offset betwen ball (match) and player activation
                int idx = 0;
                for (int i = 1; i < draw_frames_count; i++) { // todo synchronize the offset with the ball
                    idx = i + cur_pos + offset;

                    if (idx > 0 && idx < frameSet[j].frames.length) {  // inside the array (player is currently active)
                        if (i % steps_increase_alpha == 0) {
                            g.setColor(new Color(col_r, col_g, col_b, 0.1f * i / 10.0f));
                        }

                        g.drawLine(
                            scaleX(frameSet[j].frames[idx - 1].X),
                            scaleY(frameSet[j].frames[idx - 1].Y),
                            scaleX(frameSet[j].frames[idx].X),
                            scaleY(frameSet[j].frames[idx].Y)
                        );
                    }
                }
                if (idx > 0 && idx < frameSet[j].frames.length) {
                    int x = scaleX(frameSet[j].frames[idx].X),
                        y = scaleY(frameSet[j].frames[idx].Y);
                    g.fillArc(x - 1, y - 1, 3, 3, 0, 360);
                    g.setColor(Color.black);
                    g.drawString(
                        game.getPlayer(frameSet[j].Object).ShortName, x,y);
                }
            }
        }

        // long duration = System.nanoTime() - startTime;
        // System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    private void drawBackground() {
        g.setColor(new Color(182,215,174));
        g.fillRect(0, 0, width, height);

        g.setColor(Color.white);
        ((Graphics2D) g).setStroke(new BasicStroke(3));
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

    private int scaleX(double x) {
        return (int) ((x + 75) * scale) ;
    }

    private int scaleY(double y) {
        return (int) ((y + 50) * scale);
    }

}
