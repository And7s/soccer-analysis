package idp;


import java.io.*;
import java.util.ArrayList;

import static idp.idp.match;


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


    public void writeCSV() {
        if (matchs.size() != positions.size()) return;


        try
        {
            OutputStream os = new FileOutputStream("analyze.csv");
            os.write(239);
            os.write(187);
            os.write(191);

            PrintWriter wr = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

            for (int k = 0; k < positions.size(); k++) {
                FrameSet[] frameSet = positions.get(k).frameSet;
                Match match = matchs.get(k);
                writeMatch(wr, frameSet, match);

                // write individual files
                OutputStream os2 = new FileOutputStream(match.MatchId + ".csv");
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

    private void writeMatch(PrintWriter wr, FrameSet[] frameSet, Match match) {

        wr.append(
            "Match,MatchTitle,Competition,KickoffTime," +
                "Player, " +
                "intervall," +
                "Sprint [sprint/min]," +
                "Speed [m/min]," +
                "duration [min]," +
                "acceleration [m/s2]," +
                "first half, club,energy[J/kg/m]," +
                "total distance[m],"+
                "mean abs acc [m/s2],");
        for (int l = VAR.SZ0; l <= VAR.SZ4; l++) {
            wr.append("speedzone "+(l-VAR.SZ0)+"duration [min], speedzone"+(l-VAR.SZ0)+" distance [m/min],");
        }
        wr.append("\n");

        for (int i = 0; i < frameSet.length; i++) {
            FrameSet fs = frameSet[i];

            if (fs.isBall) continue;
            Player player = match.getPlayer(fs.Object);
            boolean is_tw = player.PlayingPosition.equals("TW");
            boolean is_starting = player.Starting;
            if(App.ignore_keeper && is_tw) continue;   // dont take keeper into the dataset
            if(App.ignore_exchange && !is_starting) continue;

            for (int j = 0; j <= App.steps_mean; j++) {
                int filter = (j == 0) ? FILTER.ALL : FILTER.ACTIVE;
                int start = (j == 0) ? 0 : getMeanStart(j - 1);
                int end = (j == 0) ? 45 : getMeanEnd(j - 1);

                wr.append(
                    match.MatchId + "," +
                        match.GameTitle + "," +
                        match.KickoffTime + "," +
                        match.Competition + "," +
                        match.getPlayer(fs.Object).ShortName + ((j == 0) ? " all": " active") + "," +
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
}
