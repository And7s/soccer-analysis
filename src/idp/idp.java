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

public class idp {

    public MyCanvas canvas;
    public Match match;
    public visualField visField;
    public Events events;
    public static FrameSet[] frameSet;

    public myFrame my_frame;
    public static MeanData[] dat;
    public static Position position;
    public idp() {

      my_frame = new myFrame();

        //canvas = new MyCanvas();

        visField = new visualField();

        match = new Match("data/S_14_15_BRE_HSV/match.xml");
        position = new Position();
        frameSet = position.fakeData();
        //frameSet = position.readData();
        analyze();
        createTable();
        my_frame.addView(visField, "Field");
        visSpeed vis_speed = new visSpeed();
        vis_speed.updateData(dat, frameSet);
        my_frame.addView(vis_speed, "speed");

        visMean vis_mean = new visMean();
        App.vis_mean = vis_mean;    // set static ref
        vis_mean.updateData(dat, frameSet);
        my_frame.addView(vis_mean, "mean");


        events = new Events("data/S_14_15_BRE_HSV/events.xml");

        visField.updateData(position, match);



    }

    public static void main(String[] args) {
        new idp();

    }

    public void createTable() {

        // create a table view

        Object rowData[][] = new Object[frameSet.length][7];
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
    System.out.println("create ");

        }
        Object columnNames[] = { "#", "Object", "firstHalf", "mean [km/h]", "duration [min]", "distance [km]", "#sprints (all, inter, active), per minute"};

        my_frame.addView(new Table(rowData, columnNames), "Table");



        //  canvas.updateData(dat, frameSet);
    }

    public static void analyze() {
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
        for (int i = 0; i < frameSet.length; i++) {
            if (frameSet[i].Club.equals("ball")) continue;
            System.out.println("analyze");
            System.out.println(frameSet[i].toString());
            Frame[] frames = frameSet[i].frames;    // quickref
            for (int j = 0; j < frames.length; j++) {
                // in which range?
                for (int k = border.length - 2; k >= 0; k--) {

                    if (frames[j].BallStatus == 1 || !App.only_active) {    // either ball statis is active, or its not requested
                        if (frames[j].N >= border[k]) {
                            dat[k].sum += frames[j].S;
                            dat[k].count++;
                            dat[k].sq_sum += frames[j].S * frames[j].S;
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < border.length - 1; i++) {
            dat[i].mean = dat[i].sum / dat[i].count;
            dat[i].var = dat[i].sq_sum / dat[i].count - dat[i].mean * dat[i].mean;
            dat[i].sd = (float)Math.sqrt(dat[i].var);
            System.out.println("mean "+dat[i]);
        }


    }
    public static String leftPad(String originalString, int length,
                                 char padCharacter) {
        StringBuilder sb = new StringBuilder();
        System.out.println("L "+sb.length() + " v "+originalString.length());
        while (sb.length() + originalString.length() < length) {
            sb.append(padCharacter);
            System.out.println("l "+sb.length() + " v "+originalString.length());
        }
        sb.append(originalString);

        return sb.toString();
    }
    
}

