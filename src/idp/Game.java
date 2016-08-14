package idp;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

import static idp.idp.*;


/**
 * Created by Andre on 02/05/2016.
 */
// a Game collects all information about a game, that is framesets, events and matchinformation
public class Game {

    ArrayList<Position> positions = new ArrayList<Position>();
    ArrayList<Match> matchs = new ArrayList<Match>();




    public Game() {

    }

    public void addPosition(Position pos) {
        positions.add(pos);
    }

    public void addMatch(Match match) {
        matchs.add(match);
    }


    public Player getPlayer(String id) {
        // if not found check all other matches
        for (int i = 0; i < matchs.size(); i++) {
            Player p = matchs.get(i).getPlayer(id);
            if (p != null) {
                return p;
            }
        }
        System.out.println("REAl didnt find palyer "+id);
        Player p = new Player();// return dummy palyer topo avoid nullpointerexception

        p.FirstName = id;
        p.LastName = id;
        p.PlayingPosition = "";
        p.PersonId = id;
        p.ShortName = id;
        p.ShirtNumber = -1;
        p.Starting = false;

        return p;
    }
    public void writeCSV() {
        if (matchs.size() != positions.size()) return;


        try
        {
            OutputStream os = new FileOutputStream("export/analyze.csv");
            os.write(239);
            os.write(187);
            os.write(191);

            PrintWriter wr = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

            for (int k = 0; k < positions.size(); k++) {

                FrameSet[] frameSet = positions.get(k).frameSet;
                Match match = matchs.get(k);
                System.out.println("write output "+match.MatchId);
                writeMatch(wr, frameSet, match);

                // write individual files
                OutputStream os2 = new FileOutputStream("export/" + match.MatchId + ".csv");
                os2.write(239);
                os2.write(187);
                os2.write(191);

                PrintWriter wr2 = new PrintWriter(new OutputStreamWriter(os2, "UTF-8"));
                writeMatch(wr2, frameSet, match);
                wr2.flush();
                wr2.close();

            }

            //generate whatever data you want

            wr.flush();
            wr.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    private void redoAnalyze() {
        for (int i = 0; i < game.positions.size(); i++) {
            game.positions.get(i).precalculateNumbers();
        }
        analyze();  // update all views
    }

    private void writeMatch(PrintWriter wr, FrameSet[] frameSet, Match match) {

        wr.append(
            "Match,GameTitle,Competition,KickoffTime," +
                "Player, " +
                "intervall," +
                "Sprint [sprint/min]," +
                "Speed [m/min]," +
                "duration [min]," +
                "acceleration [m/s2]," +
                "first half," +
                "club,energy[J/kg/m]," +
                "total distance[m],"+
                "mean abs acc [m/s2],");
        for (int l = VAR.SZ0; l <= VAR.SZ4; l++) {
            wr.append("speedzone "+(l-VAR.SZ0)+"duration [min], speedzone"+(l-VAR.SZ0)+" vel [m/min],");
        }
        wr.append("\n");

        for (int i = 0; i < frameSet.length; i++) {
            FrameSet fs = frameSet[i];

            if (fs.isBall) continue;
            if (App.ignore_officials && fs.noTeam) continue;
            Player player = game.getPlayer(fs.Object);
            boolean is_tw = player.PlayingPosition.equals("TW");
            boolean is_starting = player.Starting;
            if (App.ignore_keeper && is_tw) continue;   // dont take keeper into the dataset
            if (App.ignore_exchange && !is_starting) continue;

            for (int j = 0; j <= App.steps_mean; j++) {
                int filter = (j == 0) ? FILTER.ALL : FILTER.ACTIVE;
                int start = (j == 0) ? 0 : getMeanStart(j - 1);
                int end = (j == 0) ? 45 : getMeanEnd(j - 1);

                wr.append(
                    match.MatchId + "," +
                        match.GameTitle + "," +
                        match.Competition + "," +
                        match.KickoffTime + "," +
                        game.getPlayer(fs.Object).ShortName + ((j == 0) ? " all": " active") + "," +
                        start + "-" + end + "," +
                        (fs.getVar(VAR.SPRINT, start, end, filter) / fs.getVarCount(VAR.SPRINT, start, end, filter) * 25 * 60) + "," +
                        (fs.getVar(VAR.SPEED, start, end, filter) / fs.getVarCount(VAR.SPEED, start, end, filter) / 3.6 * 60) + "," +
                        fs.getVarCount(VAR.SPEED, start, end, filter) / 25.0 / 60 + "," +      // duration
                        (fs.getVarSq(VAR.ACC, start, end, filter) /      //m/s2 (but abs value sum)
                            fs.getVarCount(VAR.ACC, start, end, filter) ) + "," +   // need to divide by count
                        fs.firstHalf + "," +
                        match.getTeam(fs.Club).name + "," +
                        (fs.getVar(VAR.ENERGY, start, end, filter) / fs.getVarCount(VAR.ENERGY, start, end, filter)) + "," +
                        (fs.getVar(VAR.SPEED, start, end, filter) / 3.6 / 25.0) + "," +       // total run meter
                        Math.sqrt((fs.getVarSq(VAR.ACC, start, end, filter) / fs.getVarCount(VAR.ACC, start, end, filter))) * 25.0 / 3.6 + ","
                );

                for (int l = VAR.SZ0; l <= VAR.SZ4; l++) {
                    wr.append((fs.getVarCount(l, start, end, filter) / 25.0 / 60) + ",");     // duration in this speed zone
                    wr.append(fs.getVar(l, start, end, filter) / (fs.getVarCount(l, start, end, filter)) / 3.6 * 60 + ",");    // avg distance per minute in this zone
                }

                wr.append("\n");
            }

        }
    }
    private int getMeanStart(int i) {
        int steps = App.steps_mean;
        return (int)(45.0 / steps * (i % steps));
    }
    private int getMeanEnd(int i) {
        int steps = App.steps_mean;
        return (int)(45.0 / steps * ((i % steps) +1 ));
    }


    public void exportLoaded() {

        // exports all currently loaded datasets
        redoAnalyze();  // get correct numbers

        int all_fs_count = 0;
        for (int i = 0; i < game.positions.size(); i++) {
            all_fs_count += game.positions.get(i).frameSet.length;
        }
        FrameSet[] all_fs = new FrameSet[all_fs_count];
        int c = 0;
        for (int i = 0; i < game.positions.size(); i++) {
            FrameSet[] fs = game.positions.get(i).frameSet;
            for (int j = 0; j < fs.length; j++) {
                all_fs[c++] = fs[j];
            }
        }
        try {
            for (int i = 0; i < game.positions.size(); i++) {
                BufferedImage image = new BufferedImage(1000, 700,BufferedImage.TYPE_INT_RGB);
                vis_zones.updateData(game.positions.get(i).frameSet);

                Graphics2D cg = image.createGraphics();
                vis_zones.paint(cg, 1000, 700);
                ImageIO.write(image, "png", new File("export/" + getExportName(i) + "_speed_zones.png"));
            }


            BufferedImage image = new BufferedImage(1000, 700,BufferedImage.TYPE_INT_RGB);
            vis_zones.updateData(all_fs);
            Graphics2D cg = image.createGraphics();
            vis_zones.paint(cg, 1000, 700);
            ImageIO.write(image, "png", new File("export/all_speed_zones.png"));

        } catch (Exception e) {

        }

        // draw speed chart
        /*
        try {
            int all_fs_count = 0;
            for (int i = 0; i < game.positions.size(); i++) {
                BufferedImage image = new BufferedImage(2000, 900, BufferedImage.TYPE_INT_RGB);
                FrameSet[] fs = game.positions.get(i).frameSet;
                frameSet = fs;
                for (int j = 0; j < fs.length; j++) {
                    App.selctedFramesetIdx = j;
                    Graphics2D cg = image.createGraphics();
                    vis_speed.paint(cg, 2000, 900);
                    ImageIO.write(image, "png", new File("export/" + fs[j].Match + "_" + fs[j].Object + "speed_zones.png"));
                }
            }
            System.out.println("in total there are "+ all_fs_count);
        } catch (Exception e) {

        }*/

        try {

            for (int i = 0; i < game.positions.size(); i++) {
                BufferedImage image = new BufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB);
                FrameSet[] fs = game.positions.get(i).frameSet;
                match = game.matchs.get(i);
                frameSet = fs;

                Graphics2D cg = image.createGraphics();
                vis_mean.paint(cg, 1000, 500);
                ImageIO.write(image, "png", new File("export/" + getExportName(i) + "_mean.png"));

            }
            // all together
            BufferedImage image = new BufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB);

            frameSet = all_fs;

            Graphics2D cg = image.createGraphics();
            vis_mean.paint(cg, 1000, 500);
            ImageIO.write(image, "png", new File("export/all_mean.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // draw sprints#
        try {

            for (int i = 0; i < game.positions.size(); i++) {
                BufferedImage image = new BufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB);
                FrameSet[] fs = game.positions.get(i).frameSet;
                match = game.matchs.get(i);
                frameSet = fs;

                Graphics2D cg = image.createGraphics();
                vis_sprints.paint(cg, 1000, 500);
                ImageIO.write(image, "png", new File("export/" + getExportName(i) + "_sprints.png"));

            }
            // all together
            BufferedImage image = new BufferedImage(1000, 500, BufferedImage.TYPE_INT_RGB);

            frameSet = all_fs;

            Graphics2D cg = image.createGraphics();
            vis_sprints.paint(cg, 1000, 500);
            ImageIO.write(image, "png", new File("export/all_sprints.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        writeCSV();
        System.out.println("export finished");
    }

    private String getExportName(int i) {
        return game.matchs.get(i).MatchId + '_' + App.ignore_keeper + '_'+ App.only_active + '_'+ App.ignore_exchange + '_' + App.steps_mean + '_' + App.smooth_factor;
    }
}
