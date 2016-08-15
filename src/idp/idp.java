package idp;

import javafx.geometry.Pos;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import static idp.App.vis_mean;
import static idp.App.vis_speed;
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

    public static myFrame my_frame;
    public static Config config;
    public static Position position;
    public static visSprints vis_sprints;
    public static visBatch vis_batch;
    public static visMean vis_mean;
    public static Table table;

    public static Batch batch;

    public void paint(Graphics g, int width, int height) {



     Graphics2D g2=(Graphics2D)g;

        g2.setBackground(Color.WHITE);
        g2.clearRect(0, 0, width, height);
        g2.setColor(Color.BLACK);
        g2.drawString("Draw a rectangle", 100,100);
        g2.drawRect(100,200,50,50);
    }


    public idp() {

        /*BufferedImage image=new BufferedImage(600, 100,BufferedImage.TYPE_INT_RGB);

        Graphics2D g2=(Graphics2D)image.getGraphics();


        paint(g2, 600, 100);
        try {
            ImageIO.write(image, "png", new File("mycanvas.png"));
        } catch (Exception e) {

        }*/
        App.vis_speed = new visSpeed();
        game = new Game();
        batch = new Batch();

        my_frame = new myFrame();


        visField = new visualField();

        showMemory("back in main");
       // analyze();



        //my_frame.config.updateData();
        my_frame.addView(visField, "Field");

        vis_batch = new visBatch();
        my_frame.addView(vis_batch, "batch");

        vis_zones = new visZones();
        my_frame.addView(vis_zones, "Zones");


        my_frame.addView(vis_speed, "speed");

        vis_mean = new visMean();
        App.vis_mean = vis_mean;    // set static ref

        my_frame.addView(vis_mean, "mean");

        vis_sprints = new visSprints();
        App.vis_sprints = vis_sprints;
        vis_sprints.updateData(frameSet);
        my_frame.addView(vis_sprints, "sprints");

        // events depend on an existing frameset when being instantiated
//        events = new Events("data/S_14_15_BRE_HSV/events.xml");

        // visField.updateData();
        vis_zones.repaint();
//        onGameLoaded();
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
        App.selectedMatchIdx = idx;

        updateTable();
        analyze();
    }
    public static void onGameLoaded() {
        //visField.updateData();
        System.out.println("a game has been loaded");
        selectFrameSet(0);
        config.updateData();

    }

    public static void updateTable() {

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

        if (table != null) {
            table.update(rowData);
        } else {
            table = new Table(rowData, columnNames);
            my_frame.addView(table, "Table");
        }


        //  canvas.updateData(dat, frameSet);
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

