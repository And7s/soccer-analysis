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

    public static Position position;
    public static visSprints vis_sprints;
    public static visMean vis_mean;

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
        vis_speed.updateData(frameSet);
        my_frame.addView(vis_speed, "speed");

        vis_mean = new visMean();
        App.vis_mean = vis_mean;    // set static ref
        App.vis_speed = vis_speed;

        my_frame.addView(vis_mean, "mean");

        vis_sprints = new visSprints();
        App.vis_sprints = vis_sprints;
        vis_sprints.updateData(frameSet);
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

        if (vis_zones != null) vis_zones.repaint();
        if (vis_sprints != null) vis_sprints.repaint();
        if (vis_mean != null) vis_mean.updateData();

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

