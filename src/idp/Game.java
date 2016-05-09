package idp;


import java.io.*;
import java.util.ArrayList;

import static idp.idp.match;
import static idp.idp.position;

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

        FrameSet[] frameSet = position.frameSet;
        try
        {
            OutputStream os = new FileOutputStream(frameSet[0].Match + ".csv");
            os.write(239);
            os.write(187);
            os.write(191);

            PrintWriter wr = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

            wr.append("MatchId," + match.MatchId + "\n" +
                "GameTitle," + match.GameTitle + "\n" +
                "KickoffTime" + match.KickoffTime + "\n" +
                "Competition" + "match.Competition" + "\n"
            );

            wr.append("Frameset, Sprint [sprint/active min]," +
                "vel total [m/min], Mean vel-15 [m/min], Mean vel-30 [m/min], Mean vel-45 [m/min]," +
                "in total game [min], in paused game, in active game," +
                "speed minmax -15[m/s], speed minmax -30[m/s], speed minmax -45[m/s]," +
                "framesmissing, first half, club,energy[J/kg/m]," +
                "total distance[m]\n");


            for (int i = 0; i < frameSet.length; i++) {
                FrameSet fs = frameSet[i];
                if (fs.Object.equals("DFL-OBJ-0000XT")) continue;
                wr.append(
                    match.getPlayer(fs.Object).ShortName + "," +
                        (fs.getVar(VAR.SPRINT, FILTER.ACTIVE) / fs.getVarCount(VAR.SPRINT, FILTER.ACTIVE) * 25 * 60) + "," +    // sprints per active minute

                    fs.getVar(VAR.SPEED) / fs.getVarCount(VAR.SPEED) / 3.6 * 60    + ",");
                for (int j = 0; j < 3; j++) {
                    wr.append(
                        (fs.getVar(VAR.SPEED, 15 * j, 15 * j + 15, FILTER.ACTIVE) / fs.getVarCount(VAR.SPEED, 15 * j, 15 * j + 15, FILTER.ACTIVE) / 3.6 * 60) + ","
                    );
                }
                wr.append(fs.getVarCount(VAR.SPEED, FILTER.ALL) / 25.0 / 60 + ",");
                wr.append(fs.getVarCount(VAR.SPEED, FILTER.PAUSED) / 25.0 / 60 + ",");
                wr.append(fs.getVarCount(VAR.SPEED, FILTER.ACTIVE) / 25.0 / 60 + ",");

                for (int j = 0; j < 3; j++) {
                    wr.append(
                        fs.getVarMin(VAR.SPEED, 15 * j, 15 * j + 15, FILTER.ACTIVE) / 3.6 + " - " +
                        fs.getVarMax(VAR.SPEED, 15 * j, 15 * j + 15, FILTER.ACTIVE) / 3.6 + ","
                    );
                }
                wr.append(fs.frames_missing + ",");
                wr.append(fs.firstHalf + ",");
                wr.append(match.getTeam(fs.Club).name + ",");
                wr.append((fs.getVar(VAR.ENERGY) / fs.getVarCount(VAR.ENERGY)) + ",");
                wr.append(
                    (fs.getVar(VAR.SPEED) / 3.6 / 25.0) + ","       // total run meter
                );
                wr.append("\n");
            }

            //generate whatever data you want

            wr.flush();
            wr.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
