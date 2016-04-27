package idp;

import idp.ui.*;
import idp.ui.myFrame;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;
import javax.swing.JFrame;
import javax.xml.stream.*;
// http://knowm.org/open-source/xchart/xchart-example-code/


// about power
// http://www.gpexe.com/en/blog/metabolic-power-really-understood.html
public class idp {

    public MyCanvas canvas;
    public static Match match;
    public visualField visField;
    public static visZones vis_zones;
    public Events events;
    public static FrameSet[] frameSet;

    public myFrame my_frame;
    public static MeanData[] dat;
    public static Position position;
    public static visSprints vis_sprints;
    public idp() {

        my_frame = new myFrame();

        //canvas = new MyCanvas();

        visField = new visualField();

        match = new Match("data/S_14_15_BRE_HSV/match.xml");
        position = new Position();
        //frameSet = position.fakeData();
        frameSet = position.readData();
        analyze();
        createTable();
        my_frame.config.updateData();
        my_frame.addView(visField, "Field");

        vis_zones = new visZones();
        my_frame.addView(vis_zones, "Zones");

        visSpeed vis_speed = new visSpeed();
        vis_speed.updateData(dat, frameSet);
        my_frame.addView(vis_speed, "speed");

        visMean vis_mean = new visMean();
        App.vis_mean = vis_mean;    // set static ref
        App.vis_speed = vis_speed;
        vis_mean.updateData(dat, frameSet);
        my_frame.addView(vis_mean, "mean");

        vis_sprints = new visSprints();
        App.vis_sprints = vis_sprints;
        vis_sprints.updateData(dat, frameSet);
        my_frame.addView(vis_sprints, "sprints");

        // events depend on an existing frameset when being instantiated
        events = new Events("data/S_14_15_BRE_HSV/events.xml");

        visField.updateData(position, match);
        vis_zones.repaint();


    }

    public static void main(String[] args) {
        new idp();

    }

    public void createTable() {

        // create a table view

        Object rowData[][] = new Object[frameSet.length][9];
        for (int i = 0; i < frameSet.length; i++) {

            rowData[i][0] = match.getPlayer(frameSet[i].Object) != null ?
                match.getPlayer(frameSet[i].Object).ShirtNumber :
                "-";
            rowData[i][1] = match.getPlayer(frameSet[i].Object) != null ?
                match.getPlayer(frameSet[i].Object).ShortName :
                frameSet[i].Object;
            rowData[i][2] = frameSet[i].firstHalf;
            rowData[i][3] = frameSet[i].getSum() / frameSet[i].getCount(0);
            rowData[i][4] =
                String.format("%.1f", (frameSet[i].getCount(-1) / 25.0 / 60)) + " | " +
                String.format("%.1f", (frameSet[i].getCount(0) / 25.0 / 60)) + " | " +
                String.format("%.1f", (frameSet[i].getCount(1) / 25.0 / 60));

            rowData[i][5] = frameSet[i].getSum() / 25.0 / 60 / 60;
            rowData[i][6] =
                leftPad("" + frameSet[i].getSprintCount(-1), 4, ' ') + " | " +
                leftPad("" + frameSet[i].getSprintCount(0), 4, ' ') + " | " +
                leftPad("" + frameSet[i].getSprintCount(1), 4, ' ');
            rowData[i][7] = match.getPlayer(frameSet[i].Object) != null ?
                match.getPlayer(frameSet[i].Object).Starting :
                "-";
            rowData[i][8] = match.getPlayer(frameSet[i].Object) != null ?
                match.getPlayer(frameSet[i].Object).PlayingPosition :
                "-";

            //System.out.println("psoition"+match.getPlayer(frameSet[i].Object).PlayingPosition);


        }
        Object columnNames[] = { "#", "Object", "firstHalf", "mean [km/h]", "duration [min]", "distance [km]", "#sprints (all, inter, active), per minute", "starting", "position"};

        my_frame.addView(new Table(rowData, columnNames), "Table");



        //  canvas.updateData(dat, frameSet);
    }

    public static void analyze() {
        long startTime = System.nanoTime();
        int parts = App.steps_mean;  // in how many parts i divide a halftime
        int[] border = new int[parts * 2 + 1];
        FrameSet fs_ball = position.getBallFirstHalf(true);
        int c_border = 0;
        for (int i = 0; i < parts; i++) {
            border[c_border++] = (int) (fs_ball.frames[0].N + (double) fs_ball.frames.length / parts * i);
        }

        fs_ball = position.getBallFirstHalf(false);
        for (int i = 0; i < parts + 1; i++) {
            border[c_border++] = (int) (fs_ball.frames[0].N + (double) fs_ball.frames.length / parts * i);
        }


        System.out.println("border");


        dat = new MeanData[border.length - 1];
        for (int i = 0; i < border.length - 1; i++) {   // create Meandaata objects
            dat[i] = new MeanData();
        }
        int sprint_count = 0;
        for (int i = 0; i < frameSet.length; i++) {
            if (frameSet[i].Club.equals("ball")) continue;
            if (frameSet[i].Object.equals("DFL-OBJ-0000XT")) continue;  // different datasets declare the ball differently
            //System.out.println("analyze");
            //System.out.println(frameSet[i].toString());
            Frame[] frames = frameSet[i].frames;    // quickref

            Player player = match.getPlayer(frameSet[i].Object);
            boolean is_tw = player.PlayingPosition.equals("TW");
            boolean is_starting = player.Starting;

             if(App.ignore_keeper && is_tw) continue;   // dont take keeper into the dataset
             if(App.ignore_exchange && !is_starting) continue;

            for (int j = 0; j < frames.length; j++) {

                // in which range?
                for (int k = border.length - 2; k >= 0; k--) {

                    if (!App.only_active || frames[j].BallStatus == 1) {     // either ball statis is active, or its not requested

                        if (frames[j].N >= border[k]) {

                            // speed
                            dat[k].sum += frames[j].S;
                            dat[k].count++;
                            dat[k].sq_sum += frames[j].S * frames[j].S;

                            // speed zones
                            int speed_zone_idx;
                            // todo: reverse the selection for performance (smallest to the top)
                            if (frames[j].S / 3.6 > 7) {
                                speed_zone_idx = 4;
                            } else if (frames[j].S / 3.6 > 5.5) {
                                speed_zone_idx = 3;
                            } else if (frames[j].S / 3.6 > 4) {
                                speed_zone_idx = 2;
                            } else if (frames[j].S / 3.6 > 2) {
                                speed_zone_idx = 1;
                            } else {
                                speed_zone_idx = 0;
                            }
                            dat[k].speed_zones[speed_zone_idx].count++;
                            dat[k].speed_zones[speed_zone_idx].sum += (frames[j].S / 3.6f);
                            dat[k].speed_zones[speed_zone_idx].sq_sum += Math.pow(frames[j].S / 3.6, 2);

                            // sprint count at least 1 second faster than 7ms
                            if (frames[j].S / 3.6 > 7) {
                                //System.out.println("count "+sprint_count);
                                sprint_count++;
                                if (sprint_count == 25) {
                                    dat[k].sprints.sum++;
                                }
                            } else {
                                sprint_count = 0;
                            }
                            dat[k].sprints.count++; // to be able to measure sprint per time

                            break;
                        }
                    }
                }
            }
            if (vis_zones != null) vis_zones.repaint();
            if (vis_sprints != null) vis_sprints.repaint();
        }

        // to get the mean and the standard derivate we need to calculate (dived and stuff)
        for (int i = 0; i < border.length - 1; i++) {
            dat[i].mean = dat[i].sum / dat[i].count;
            dat[i].var = dat[i].sq_sum / dat[i].count - dat[i].mean * dat[i].mean;
            dat[i].sd = (float)Math.sqrt(dat[i].var);
            System.out.println("mean "+dat[i]);
            for (int j = 0; j < dat[i].speed_zones.length; j++) {
                dat[i].speed_zones[j].mean = dat[i].speed_zones[j].sum / dat[i].speed_zones[j].count;
                dat[i].speed_zones[j].var = dat[i].speed_zones[j].sq_sum / dat[i].speed_zones[j].count - dat[i].speed_zones[j].mean * dat[i].speed_zones[j].mean;
                dat[i].speed_zones[j].sd = (float)Math.sqrt(dat[i].speed_zones[j].var);
            }
            dat[i].sprints.mean = dat[i].sprints.sum / dat[i].sprints.count * 25 * 60;   // sprints per minute
            dat[i].sprints.var = 0;
            dat[i].sprints.sd = 0;
        }

        long duration = System.nanoTime() - startTime;
        System.out.println("analyze took" + (duration / 1E6)+ "ms");


    }
    public static String leftPad(String originalString, int length,
                                 char padCharacter) {
        StringBuilder sb = new StringBuilder();

        while (sb.length() + originalString.length() < length) {
            sb.append(padCharacter);
        }
        sb.append(originalString);

        return sb.toString();
    }
    
}

