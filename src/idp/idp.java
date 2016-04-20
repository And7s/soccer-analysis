package idp;

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
    public FrameSet[] frameSet;

    public idp() {

        //canvas = new MyCanvas();
        JFrame frame = new JFrame("Points");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        visField = new visualField();
        frame.add(visField);
        frame.setSize(250, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        match = new Match("data/S_14_15_BRE_HSV/match.xml");
        Position position = new Position();
        frameSet = position.fakeData();
        //frameSet = position.readData();
        analyze();

        events = new Events("data/S_14_15_BRE_HSV/events.xml");

    }

    public static void main(String[] args) {
        new idp();

    }




    public void analyze() {

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


        }
        Object columnNames[] = { "#", "Object", "firstHalf", "mean [km/h]", "duration [min]", "distance [km]", "#sprints (all, inter, active), per minute"};
        new Table(rowData, columnNames);



        int[] border = {10000, 33000, 56000, 100000, 123000, 146000, 200000};
        MeanData[] dat = new MeanData[border.length - 1];
        for (int i = 0; i < border.length - 1; i++) {   // create Meandaata objects
            dat[i] = new MeanData();
        }
        for (int i = 0; i < frameSet.length; i++) {
            if (frameSet[i].Club.equals("ball")) continue;
            System.out.println(frameSet[i].toString());
            Frame[] frames = frameSet[i].frames;    // quickref
            for (int j = 0; j < frames.length; j++) {
                // in which range?
                for (int k = border.length - 2; k >= 0; k--) {
                    if (frames[j].BallStatus == 0) {
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
        visField.updateData(dat, frameSet, match);

        //  canvas.updateData(dat, frameSet);
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

