package idp;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static idp.idp.frameSet;
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
final String base = "D:\\dfl\\mat02\\";
        final File folder = new File(base);
        listFilesForFolder(folder, base);
        System.out.println(files.size());

        int count = 0;

        for (Enumeration it = files.elements(); it.hasMoreElements(); ) {
            if (count >= 10) break;
            count++;
            String s = (String) it.nextElement();

            if (count  > 0) {
                if (s.contains("DFL-CLU")) continue;    // is a DFL club definition
                int ret = Position.checkType(s);


                if (s.contains("DFL-MAT-")) {
                    System.out.println("extract from "+s);
                    String matchId = s.substring(s.indexOf("DFL-MAT-") + 8);
                    System.out.println("now after start "+matchId);
                    System.out.println("length "+matchId.length()+", starts "+matchId.indexOf("_")+ "; "+matchId.indexOf("."));
                    matchId = matchId.substring(0, 6);
                    System.out.println("matchId is " + matchId);
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
                    System.out.println(s + " = " + ret);
                } else {
                    System.out.println("cant handle file " + s);
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
                    Position position = new Position(frame_set);
                    game.addPosition(position);


                    // output plots taht depend on the position


                    // draw the position of a single player
/*
                    try {

                        BufferedImage image = new BufferedImage(1200, 900, BufferedImage.TYPE_INT_RGB);
                        for (int j = 0; j < frame_set.length; j++) {
                            Graphics2D cg = image.createGraphics();
                            visPosition.paint(cg, 1200, 900, frame_set[j]);
                            ImageIO.write(image, "png", new File("export/" + frame_set[j].Match + "_" + frame_set[j].Object + "vispos.png"));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // draw speed hcarts
                    try {
                        BufferedImage image = new BufferedImage(2000, 900, BufferedImage.TYPE_INT_RGB);
                        for (int j = 0; j < frame_set.length; j++) {
                            Graphics2D cg = image.createGraphics();
                            idp.position = position;
                            visSpeed.paint(cg, frame_set[j], 2000, 900);
                            ImageIO.write(image, "png", new File("export/" + frame_set[j].Match + "_" + frame_set[j].Object + "speed_zones.png"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }*/
                    // end output

                    position.freeMemory();
                    position = null;
                    frame_set = null;   // does this free the mem?
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
