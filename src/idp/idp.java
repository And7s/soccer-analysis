package idp;

import idp.ui.*;
import idp.ui.myFrame;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import javax.xml.stream.*;

import static idp.Position.showMemory;
// http://knowm.org/open-source/xchart/xchart-example-code/

// to execute from shell go to bin/
// > java idp.idp, data must be available from the bin directory

// about power
// http://www.gpexe.com/en/blog/metabolic-power-really-understood.html
public class idp {

    public MyCanvas canvas;
    public static Match match;
    public visualField visField;
    public static visZones vis_zones;
    public Events events;
    public static FrameSet[] frameSet;
    public static Game game;

    public myFrame my_frame;
    public static MeanData[] dat;
    public static Position position;
    public static visSprints vis_sprints;

    public static Batch batch;
    public idp() {

        game = new Game();
        batch = new Batch();

        my_frame = new myFrame();


        visField = new visualField();

        showMemory("back in main");
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

        //visField.updateData(position, match);
        vis_zones.repaint();


    }

    public static void main(String[] args) {
        new idp();

    }

    public static void onGameLoaded() {
        System.out.println("a game has been loaded");
        frameSet = game.positions.get(0).frameSet;  // make this frameset accessible
        position = game.positions.get(0);
        match = game.matchs.get(0); // danger how to verify that mathc has been loaded
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
            rowData[i][3] = frameSet[i].getVar(VAR.SPEED) / frameSet[i].getVarCount(VAR.SPEED);
            rowData[i][4] =
                String.format("%.1f", (frameSet[i].getVarCount(VAR.SPEED, FILTER.ALL) / 25.0 / 60)) + " | " +
                String.format("%.1f", (frameSet[i].getVarCount(VAR.SPEED, FILTER.PAUSED) / 25.0 / 60)) + " | " +
                String.format("%.1f", (frameSet[i].getVarCount(VAR.SPEED, FILTER.ACTIVE) / 25.0 / 60));

            rowData[i][5] = frameSet[i].getVar(VAR.SPEED) / 25.0 / 60 / 60;
            rowData[i][6] =
                leftPad("" + frameSet[i].getVar(VAR.SPRINT), 4, ' ') + " | " +
                leftPad("" + frameSet[i].getVar(VAR.SPRINT, FILTER.PAUSED), 4, ' ') + " | " +
                leftPad("" + frameSet[i].getVar(VAR.SPRINT, FILTER.ACTIVE), 4, ' ');
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
        for (int i = 0; i < dat.length; i++) dat[i] = new MeanData();   // initialize the objects


        for (int i = 0; i < frameSet.length; i++) {
            if (frameSet[i].Club.equals("ball")) continue;
            if (frameSet[i].Object.equals("DFL-OBJ-0000XT")) continue;  // different datasets declare the ball differently

            Frame[] frames = frameSet[i].frames;    // quickref

            Player player = match.getPlayer(frameSet[i].Object);
            boolean is_tw = player.PlayingPosition.equals("TW");
            boolean is_starting = player.Starting;

             if(App.ignore_keeper && is_tw) continue;   // dont take keeper into the dataset
             if(App.ignore_exchange && !is_starting) continue;

            // get information from minute data
            // parts say how many parts a half time should be split


            for (int k = 0; k < parts; k++) {   // create MeanData objects
                // sprints
                int from_min = (int)(50.0 / parts * k);
                int to_min = (int)(50.0 / parts * (k + 1));

                int filter = App.only_active ? FILTER.ACTIVE : FILTER.ALL;
                int account_chunk = frameSet[i].firstHalf ? k : k + parts;

                dat[account_chunk].sprints.sum += frameSet[i].getVar(VAR.SPRINT, from_min, to_min, filter);
                dat[account_chunk].sprints.count += frameSet[i].getVarCount(VAR.SPRINT, from_min, to_min, filter);

                // speed
                dat[account_chunk].sum += frameSet[i].getVar(VAR.SPEED, from_min, to_min, filter);
                dat[account_chunk].count += frameSet[i].getVarCount(VAR.SPEED, from_min, to_min, filter);
                dat[account_chunk].sq_sum += frameSet[i].getVarSq(VAR.SPEED, from_min, to_min, filter);
            }


            for (int j = 0; j < frames.length; j++) {

                // in which range?
                for (int k = border.length - 2; k >= 0; k--) {

                    if (!App.only_active || frames[j].BallStatus == 1) {     // either ball statis is active, or its not requested

                        if (frames[j].N >= border[k]) {

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

