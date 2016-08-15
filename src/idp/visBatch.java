package idp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static idp.idp.game;

/**
 * Created by Andre on 31/07/2016.
 */

class LoadGameParallel {
    private JTextField panel;
    private Map.Entry<String, FilePair> entry;

    LoadGameParallel(Map.Entry<String, FilePair> entry, JTextField panel){
        this.entry = entry;
        this.panel = panel;
    }

    public void run() {

        game.addMatch(new Match(entry.getValue().match));
        panel.setText(entry.getKey() + " match complete, loading...");
        try {
            FrameSet[] frame_set = Position.readPosition(entry.getValue().position, entry.getValue().posType);
            idp.frameSet = frame_set;

            Position position = new Position(frame_set);
            idp.position = position;
            game.addPosition(position);
            panel.setText(entry.getKey() + " sucessfully loaded, current FS");
        } catch (Exception e2) {
            e2.printStackTrace();
            panel.setText(entry.getKey() + " error "+e2.getMessage());
        }

        System.out.println("Thread  exiting.");
        idp.onGameLoaded();
    }

}

class FileStruct {
    String name;
    int type;
    boolean inMem;
}
public class visBatch extends JPanel {
    Vector<String> files;
    Vector<FileStruct> files_struct;
    Map<String, FilePair> pairs;
    private static int LIMIT = 1000;
    public visBatch() {

        JPanel panel = new JPanel();

        JScrollPane scrollPane = new JScrollPane(panel);
        add(scrollPane, BorderLayout.CENTER);
        setLayout(new GridLayout(1,0));

        files = new Vector<String>(LIMIT);
        files_struct = new Vector<FileStruct>(LIMIT);

        pairs = new HashMap<String, FilePair>(LIMIT);
        final String base = "D:\\dfl\\";
        final File folder = new File(base);
        listFilesForFolder(folder, base);
        System.out.println(files.size());

        int count = 0;

        for (Enumeration it = files.elements(); it.hasMoreElements(); ) {

            String s = (String) it.nextElement();
            //files_struct.elementAt(count).name = s;
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
            }
            count++;
            if (count >= LIMIT) break;
        }


        int numPairs = 0;
        for (Map.Entry<String, FilePair> entry : pairs.entrySet()) {

            // analyze this
            String pos = entry.getValue().position,
                match = entry.getValue().match;

            System.out.println("match "+entry.getKey() + " pos: "+ pos + " match "+ match);
            JTextField name = new JTextField();
            name.setEditable(false);
            name.setText(entry.getKey());

            JTextPane cont = new JTextPane();
            cont.setEditable(false);
            cont.setContentType("text/html");
            String dispPos = pos.substring(pos.lastIndexOf("\\")+1);
            String dispMatch = match.substring(match.lastIndexOf("\\")+1);
            cont.setText(dispPos + "<br />" + dispMatch);

            JButton but = new JButton();
            but.setText("load");

            panel.add(name);
            panel.add(cont);
            panel.add(but);
            numPairs++;

            // adding event listener
            but.addActionListener(new ActionListener() {
                Map.Entry<String, FilePair> myEntry = entry;
                JTextField myTextField = name;
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("clicked" + entry.getKey());
                    LoadGameParallel lgp = new LoadGameParallel(myEntry, myTextField);  // could be replaced with loadgame, but doesnt ahve ffeeback to textfield yet
                    lgp.run();
                }
            });
        }
        GridLayout experimentLayout = new GridLayout(numPairs, 4);


        panel.setLayout(experimentLayout);
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

    private void loadGame(Map.Entry<String, FilePair> entry) {
        game.addMatch(new Match(entry.getValue().match));
        try {
            FrameSet[] frame_set = Position.readPosition(entry.getValue().position, entry.getValue().posType);
            idp.frameSet = frame_set;

            Position position = new Position(frame_set);
            idp.position = position;
            game.addPosition(position);
            position.freeMemory();
        } catch (Exception e2) {
            e2.printStackTrace();
        }


        System.out.println("Thread  exiting.");
        //idp.onGameLoaded();   // not necessary here, right?
    }

    public void exportAll() {
        System.out.println("EXPORT ALL");
        int c = 0;
        for (Map.Entry<String, FilePair> entry : pairs.entrySet()) {
            loadGame(entry);
            c++;
            if (c >= 128) break;
        }
        game.exportLegacy();
    }
}
