package idp;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by Andre on 01/05/2016.
 */
public class Batch {
    Vector<String> files;
    public Batch() {
        files = new Vector<String>(10);
        final File folder = new File("D:\\dfl\\");
        listFilesForFolder(folder, "D:\\dfl\\");
        System.out.println(files.size());
        /*for (Enumeration it = files.elements(); it.hasMoreElements(); ) {

            String s = (String) it.nextElement();

            int ret = Position_new.checkType(s);
            if (ret >= 0) {
                System.out.println(s + " = " +ret);
            }
            Game game = new Game();
            if (ret == 3) {
                game.match = new Match(s);
                game.writeCSV();
            }

            if (ret == 1 || ret == 0) {



                try {
                    FrameSet[] frame_set = Position_new.readPosition(s, ret);
                    game.analyzeFrameSet(frame_set);
                    game.writeCSV();

                } catch (InvalidPositionDataSet e) {
                    System.out.println("could not load file " + s);
                }
                //game.analyzeFrameSet(frame_set);

            }


        }*/


        Game game = new Game();

        game.addMatch(new Match("D:\\dfl\\DFL-MAT-0002UH_MatchInformation.xml"));
        try {
            FrameSet[] frame_set = Position_new.readPosition("D:\\dfl\\DFL-MAT-0002UH_ObservedPositionalData.xml", 1);
            Position_new pos = new Position_new(frame_set);
            game.addPosition(pos);
            game.writeCSV();

        } catch (InvalidPositionDataSet e) {
            System.out.println("could not load file " );
        }

        /*
        String file = "H:\\dfl\\DFL-MAT-0002UG_ObservedPositionalData.xml";
        Position_new.checkType(file);
        FrameSet[] frame_set = Position_new.readPosition(file);
        Game.analyzeFrameSet(frame_set);*/

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
