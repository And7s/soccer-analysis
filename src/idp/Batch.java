package idp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static idp.idp.game;


class FilePair {
    String position, match;
    int posType;
}
/**
 * Created by Andre on 01/05/2016.
 */
public class Batch {
    Vector<String> files;


    public Batch() {
        files = new Vector<String>(10);

        Map<String, FilePair> pairs = new HashMap<String, FilePair>(10);

        final File folder = new File("D:\\dfl\\");
        listFilesForFolder(folder, "D:\\dfl\\");
        System.out.println(files.size());

        int count = 0;

        for (Enumeration it = files.elements(); it.hasMoreElements(); ) {
            if (count >= 30) break;
            count++;
            String s = (String) it.nextElement();
            if (s.contains("DFL-CLU")) continue;    // is a DFL club definition
            int ret = Position.checkType(s);


            if (s.contains("DFL-MAT")) {

                String matchId = s.substring(s.indexOf("DFL-MAT-") + 8, s.indexOf("_"));
                System.out.println("matchId is "+matchId);
                FilePair p;
                if (pairs.get(matchId) != null) {
                    p = pairs.get(matchId);
                } else {
                    p = new FilePair();
                    pairs.put(matchId, p);
                }

                if (ret == 3) {
                    p.match = s;
                } else if (ret == 0 || ret == 1) {
                    p.position = s;
                    p.posType = ret;
                }
            }
            if (ret >= 0) {
                System.out.println(s + " = " +ret);
            } else {
                System.out.println("cant handle file "+s);
            }

            if (ret == 3) {
                //game.addMatch(new Match(s));
            }

            /*if (ret == 1 || ret == 0) {
                try {
                    FrameSet[] frame_set = Position.readPosition(s, ret);
                    Position pos = new Position(frame_set);
                    game.addPosition(pos);


                } catch (InvalidPositionDataSet e) {
                    System.out.println("could not load file " + s);
                }

            }*/

            //game.writeCSV();


        }
        System.out.println("==Analysze==");
        for (Map.Entry<String, FilePair> entry : pairs.entrySet()) {

            // analyze this
            String pos = entry.getValue().position,
                match = entry.getValue().match;

            System.out.println("match "+entry.getKey() + " pos: "+ pos + " match "+ match);
            if (pos != null && match != null) {

                game.addMatch(new Match(match));
                try {
                    FrameSet[] frame_set = Position.readPosition(pos, entry.getValue().posType);
                    game.addPosition(new Position(frame_set));
                    game.writeCSV();
                } catch (InvalidPositionDataSet e) {
                    System.out.println("could not load file "+pos );
                }

            }

        }

// single game output
/*

        Match match = new Match("data/S_14_15_BRE_HSV/match.xml");
        game.addMatch(match);
        idp.match = match;
        try {
            FrameSet[] frame_set = Position.readPosition("data/S_14_15_BRE_HSV/position.xml", 1);
            idp.frameSet = frame_set;
            Position pos = new Position(frame_set);
            game.addPosition(pos);

            idp.position = pos;
        } catch (InvalidPositionDataSet e) {
            System.out.println("could not load file " );
        }
*/
        String[] matches = {"DFL-MAT-0002UQ", "DFL-MAT-0002UH"};
        //String[] matches = {"DFL-MAT-00031J", "DFL-MAT-0002UH", "DFL-MAT-0002UI", "DFL-MAT-0002UK", "DFL-MAT-0002UL"};    // with visuals one dataset less "DFL-MAT-0002UL"
        //String[] matches = {"DFL-MAT-0002UQ", "DFL-MAT-0002UP", "DFL-MAT-0002UO", "DFL-MAT-00031J", "DFL-MAT-0002UH", "DFL-MAT-0002UI", "DFL-MAT-0002UK", "DFL-MAT-0002UL"};
        //String[] matches = {};

        /*for (int i = 0; i < matches.length; i++) {
            Position.showMemory("before " + i);
            game.addMatch(new Match("D:\\dfl\\" + matches[i] + "_MatchInformation.xml"));
            try {
                FrameSet[] frame_set = Position.readPosition("D:\\dfl\\" + matches[i] + "_ObservedPositionalData.xml", 1);
                Position pos = new Position(frame_set);
                game.addPosition(pos);



            } catch (InvalidPositionDataSet e) {
                System.out.println("could not load file " );
            }

        }
        //idp.onGameLoaded();
        //game.writeCSV();
        Position.showMemory("at end ");
*/
    }

    public void listFilesForFolder(final File folder, String path) {
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry, path + fileEntry.getName() + "/" );
            } else {
                if (fileEntry.getName().endsWith(".xml")) {
                    files.add(path + fileEntry.getName());
                }
            }
        }
    }
}
