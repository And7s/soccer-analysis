package idp;


import javax.swing.*;
import java.awt.*;


/**
 * Created by Andre on 19/04/2016.
 */
public class visualField extends JPanel {

    MeanData[] data;
    FrameSet[] frameSet;
    Match match;
    Graphics g;
    Position position;
    int width, height;
    float scale = 10f;
    int cur_pos = 0;
    boolean show_first_half = true;

    public void updateData(Position position, Match match) {
        this.position = position;
        this.frameSet = position.frameSet;
        this.match = match;
        repaint();

        Thread th = new Thread() {
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    update();
                    try {
                        sleep(32);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        th.run();
    }


    public void update() {

        cur_pos += App.playback_speed;
        int amount = position.getBallFirstHalf(show_first_half).frames.length;  // how long is this half
        if (cur_pos >= amount) {
            cur_pos = 0;
            show_first_half = !show_first_half;
        }
        repaint();
    }

    public void paintComponent(Graphics g) {
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


        for (int j = 0; j < frameSet.length; j++) {
            g.setColor(new Color(0,0, 0, 0.05f));

            if (frameSet[j].firstHalf == show_first_half) { // if this half is currently shown
                int offset = ball_frames.frames[0].N - frameSet[j].frames[0].N;  // offset betwen ball (match) and player activation
                int idx = 0;
                for (int i = 1; i < draw_frames_count; i++) { // todo synchronize the offset with the ball
                    idx = i + cur_pos + offset;

                    if (idx > 0 && idx < frameSet[j].frames.length) {  // inside the array (player is currently active)
                        if (i % steps_increase_alpha == 0) {
                            g.setColor(new Color(0, 0, 0, 0.1f * i / 10.0f));
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
                    ((Graphics2D) g).drawString(
                        match.getPlayer(frameSet[j].Object).ShortName, x,y);
                }
            }
        }

        // draw the Frames
        Event[] event = Events.event;

        int draw_event_count = 1 * 60 * 25;     // x minutes of events
        int end_frame = ball_frames.frames[0].N + cur_pos + draw_frames_count;
        int start_frame =  end_frame - draw_event_count;    // startframe

        int plot_event = 0;
        Color eventcolor = new Color(1, 1, 0, 0.05f);
        for (int  i = 0; i < event.length; i++) {
            if (event[i].T > end_frame) break;  // can end now, as frames are sorted
            if (event[i].T > start_frame) {
                g2d.setColor(Color.black);
                g2d.drawString(event[i].type, width - 150, 20 * plot_event);
                plot_event++;


                g2d.setColor(eventcolor);
                // where was the ball at this time?
                int ball_idx = event[i].T - ball_frames.frames[0].N;    // the offset from curent frameset to the first
                if (ball_idx >= 0 && ball_idx < ball_frames.frames.length) {    // check if not out of bounds
                    ///System.out.println("ball idx is " + ball_idx);
                    Frame event_frame = ball_frames.frames[ball_idx];    //TODO: take players position rather than the one of the ball

                    for (int j = 1; j < 10; j++) {
                        int s = j*j;
                        g2d.fillArc(scaleX(event_frame.X) - s / 2, scaleY(event_frame.Y) - s / 2, s, s, 0, 360);
                    }


                } else {
                    System.out.println("out of bounds " + ball_idx);    // should not happen with a regular dataset
                }
            }

            //System.out.println(event[i]);
        }
        // indicate ballstatus and ballposession


        long duration = System.nanoTime() - startTime;
        // System.out.println("duration" + (duration / 1E6)+ "ms");
    }

    public void drawBackground() {
        g.setColor(new Color(77,158,58));
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

    // draws a line, but takes arguments in METER
    public void drawLine(double x1, double y1, double x2, double y2) {
        g.drawLine(scaleX(x1), scaleY(y1), scaleX(x2), scaleY(y2));
    }

    public int scaleX(double x) {
        return (int) ((x + 75) * scale) ;
    }

    public int scaleY(double y) {
        return (int) ((y + 50) * scale);
    }


}
