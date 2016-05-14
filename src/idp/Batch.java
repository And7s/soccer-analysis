package idp;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Andre on 01/05/2016.
 */
public class Batch {
    Vector<String> files;

    public Game game;
    public Batch() {
        files = new Vector<String>(10);
        final File folder = new File("D:\\dfl\\");
        listFilesForFolder(folder, "D:\\dfl\\");
        System.out.println(files.size());
        game = new Game();

        /*for (Enumeration it = files.elements(); it.hasMoreElements(); ) {

            String s = (String) it.nextElement();

            int ret = Position_new.checkType(s);
            if (ret >= 0) {
                System.out.println(s + " = " +ret);
            }

            if (ret == 3) {
                game.addMatch(new Match(s));
            }

            if (ret == 1 || ret == 0) {
                try {
                    FrameSet[] frame_set = Position_new.readPosition(s, ret);
                    Position_new pos = new Position_new(frame_set);
                    game.addPosition(pos);


                } catch (InvalidPositionDataSet e) {
                    System.out.println("could not load file " + s);
                }

            }

            game.writeCSV();
        }*/

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
        String[] matches = {"DFL-MAT-00031J", "DFL-MAT-0002UH"};
        //String[] matches = {"DFL-MAT-00031J", "DFL-MAT-0002UH", "DFL-MAT-0002UI", "DFL-MAT-0002UK", "DFL-MAT-0002UL"};    // with visuals one dataset less "DFL-MAT-0002UL"
        //String[] matches = {"DFL-MAT-0002UQ", "DFL-MAT-0002UP", "DFL-MAT-0002UO", "DFL-MAT-00031J", "DFL-MAT-0002UH", "DFL-MAT-0002UI", "DFL-MAT-0002UK", "DFL-MAT-0002UL"};
        //String[] matches = {};

        for (int i = 0; i < matches.length; i++) {
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
        game.writeCSV();
        Position.showMemory("at end ");

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
