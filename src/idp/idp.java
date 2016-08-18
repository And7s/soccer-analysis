package idp;


import java.io.OutputStream;
import java.io.PrintStream;

import static idp.Position.showMemory;
// http://knowm.org/open-source/xchart/xchart-example-code/

// to execute from shell go to bin/
// > java idp.idp, data must be available from the bin directory

// about power
// http://www.gpexe.com/en/blog/metabolic-power-really-understood.html


class Interceptor extends PrintStream {
    public Interceptor(OutputStream out) {
        super(out, true);
    }
    @Override
    public void print(String s) {//do what ever you like
        super.print(s);
    }
}


public class idp {

    public static Match match;
    public visualField visField;
    public static visZones vis_zones;
    public static FrameSet[] frameSet;
    public static Game game;

    public static myFrame my_frame;
    public static Config config;
    public static Position position;
    public static visBatch vis_batch;
    public static Table table;
    public static visMean vis_mean;
    public static visSpeed vis_speed;
    public static visSprints vis_sprints;


    private idp() {
        PrintStream origOut = System.out;
        PrintStream interceptor = new Interceptor(origOut);
        System.setOut(interceptor);// just add the interceptor
        vis_speed = new visSpeed();
        game = new Game();

        my_frame = new myFrame();

        visField = new visualField();

        showMemory("back in main");

        my_frame.addView(visField, "Field");



        vis_zones = new visZones();
        my_frame.addView(vis_zones, "Zones");

        my_frame.addView(vis_speed, "speed");

        vis_mean = new visMean();

        my_frame.addView(vis_mean, "mean");

        vis_sprints = new visSprints();

        vis_sprints.updateData();
        my_frame.addView(vis_sprints, "sprints");

        vis_zones.repaint();
    }

    public static void main(String[] args) {
        new idp();

    }

    public static void selectFrameSet(int idx) {
        if (idx < 0) {
            System.out.println("error why do youlselect -1");
            return;
        }
        System.out.println("slect match "+ idx);
        frameSet = game.positions.get(idx).frameSet;  // make this frameset accessible
        position = game.positions.get(idx);
        match = game.matchs.get(idx); // danger how to verify that mathc has been loaded
        Config.selectedMatchIdx = idx;

        updateTable();
        analyze();
    }
    public static void onGameLoaded() {
        //visField.updateData();
        System.out.println("a game has been loaded");
        selectFrameSet(0);
        config.updateData();
    }

    private static void updateTable() {

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
        }
        Object columnNames[] = { "#", "Object", "firstHalf", "mean [km/h]", "duration [min]", "distance [km]", "#sprints (all, inter, active), per minute", "starting", "position"};

        if (table != null) {
            table.update(rowData);
        } else {
            table = new Table(rowData, columnNames);
            my_frame.addView(table, "Table");
        }

    }

    public static void analyze() {
        long startTime = System.nanoTime();

        if (vis_zones != null) vis_zones.repaint();
        if (vis_sprints != null) vis_sprints.repaint();
        if (vis_mean != null) vis_mean.updateData();
        if (vis_speed != null) vis_speed.repaint();
        long duration = System.nanoTime() - startTime;
        System.out.println("analyze took" + (duration / 1E6)+ "ms");

    }
    private static String leftPad(String originalString, int length,
                                 char padCharacter) {
        StringBuilder sb = new StringBuilder();

        while (sb.length() + originalString.length() < length) {
            sb.append(padCharacter);
        }
        sb.append(originalString);

        return sb.toString();
    }
}

