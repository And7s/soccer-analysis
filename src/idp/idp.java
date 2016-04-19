package idp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.xml.stream.*;
// http://knowm.org/open-source/xchart/xchart-example-code/

public class idp {

    public static MyCanvas canvas;
    public static FrameSet[] frameSet;
    public static Match match;
    public static visualField visField;
    public static void main(String[] args) {


        //canvas = new MyCanvas();
        JFrame frame = new JFrame("Points");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        visField = new visualField();
        frame.add(visField);
        frame.setSize(250, 200);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        match = new Match("data/S_14_15_BRE_HSV/match.xml");
         readData();
        //fakeData();
        analyze();


    }


    public static void fakeData() {
        frameSet = new FrameSet[58];
        for (int i = 0; i < 58; i++) {  // framesets
            frameSet[i] = new FrameSet();
            frameSet[i].Club = (i < 2) ? "Ball" : "A";
            frameSet[i].firstHalf = (i % 2) == 0;
            frameSet[i].Match = "asd";
            frameSet[i].Object = (i < 2) ? "DFL-OBJ-0000XT" : "asd";

            frameSet[i].frames = new Frame[1000];
            int first_half = i % 2;
            for (int j = 0; j < 1000; j++) {
                Frame frame = new Frame();
                frame.BallStatus = 1;
                frame.BallPossession = 1;
                frame.N = j + (i % 2) * 100000;
                frame.S = (int) (Math.random() * 10);
                frame.X = (float) (Math.random() * 150 - 75);
                frame.Y = (float) (Math.random() * 100 - 50);
                frameSet[i].frames[j] = frame;
            }
        }
    }
    
    public static void readData() {
        int idx_ball_first_half = -1,
            idx_ball_second_half = -1;  // define to be not in the fst index (0)
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in;
        long startTime = System.nanoTime();

        try {
            Frame[] tmp_frames = new Frame[100000];
            FrameSet[] tmp_framesets = new FrameSet[100];
            int tmp_c_framesets = 0;
            int tmp_c_nodes = 0;

            //in = new FileInputStream("data/reduced_100_one_line.xml");
            in = new FileInputStream("data/S_14_15_BRE_HSV/position.xml");
            int FORMAT = 1;
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
            int next = streamReader.nextTag(); // Advance to "book" element
            //System.out.println(next);
            //streamReader.nextTag(); // Advance to "person" element

            while (streamReader.hasNext()) {
                if (streamReader.isStartElement()) {
                    String tag = streamReader.getLocalName();

                    if (tag.equals("Frame")) {

                        Frame frame = new Frame();

                        frame.N = Integer.parseInt(streamReader.getAttributeValue(null, "N"));
                        if (tmp_c_framesets == idx_ball_first_half || tmp_c_framesets == idx_ball_second_half) {  // the team ball has the ballstatus
                            frame.BallPossession = Integer.parseInt(streamReader.getAttributeValue(null, "BallPossession"));
                            frame.BallStatus = Integer.parseInt(streamReader.getAttributeValue(null, "BallStatus"));
                        }
                        //frame.T = 0;
                        frame.X = Float.parseFloat(streamReader.getAttributeValue(null, "X"));
                        frame.Y = Float.parseFloat(streamReader.getAttributeValue(null, "Y"));
                        frame.S = Float.parseFloat(streamReader.getAttributeValue(null, "S"));
                        //System.out.println("N"+N);
                        tmp_frames[tmp_c_nodes++] = frame;
                        
                    } else if(tag.equals("FrameSet")) {

                        // normalize different attribute naming across seasons
                        String[] ClubIdent = {"Club", "TeamId"},
                                 MatchIdent = {"Match", "MatchId"},
                                 ObjectIdent = {"Object", "PersonId"};

                        FrameSet fr = new FrameSet();
                        fr.Club = streamReader.getAttributeValue(null, ClubIdent[FORMAT]);
                        fr.Match = streamReader.getAttributeValue(null, MatchIdent[FORMAT]);
                        fr.Object = streamReader.getAttributeValue(null, ObjectIdent[FORMAT]);
                        fr.firstHalf = streamReader.getAttributeValue(null, "GameSection").equals("firstHalf");
                        System.out.println(fr.toString());

                        if (fr.Object.equals("DFL-OBJ-0000XT")) {
                            System.out.println("found ball" + tmp_c_framesets);
                            if (fr.firstHalf) {
                                idx_ball_first_half = tmp_c_framesets;
                            } else {
                                idx_ball_second_half = tmp_c_framesets;
                            }
                        }
                        tmp_framesets[tmp_c_framesets] = fr;
                        
                    }else {
                        /*System.out.println(streamReader.getLocalName());
                        int count = streamReader.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            System.out.println(streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
                        }*/
                    }

                } else if (streamReader.isEndElement()) {
                    String tag = streamReader.getLocalName();

                    if (tag.equals("FrameSet")) {   // frameset ended, now i know how many are there
                        // System.out.println("FrameSet Ends after "+ tmp_c_nodes);
                        tmp_framesets[tmp_c_framesets].frames = new Frame[tmp_c_nodes];
                        System.arraycopy(tmp_frames, 0,  tmp_framesets[tmp_c_framesets].frames, 0, tmp_c_nodes);
                        // System.out.println(tmp_framesets[tmp_c_framesets].toString());
                        tmp_c_nodes = 0;
                        tmp_c_framesets++;
                    }
                }
                
                streamReader.next();
            }// end of xml

            System.out.println("READ "+tmp_c_framesets);
            frameSet = new FrameSet[tmp_c_framesets];
            System.arraycopy(tmp_framesets, 0, frameSet, 0, tmp_c_framesets);
            long duration = System.nanoTime() - startTime;
            System.out.println("took: " + (duration / 1E9) +"s");

            // get ballposession and ballstatus for others
            int start_first_half = frameSet[idx_ball_first_half].frames[0].N,
                start_second_half = frameSet[idx_ball_second_half].frames[0].N;
            System.out.println("start at "+start_first_half+" and "+start_second_half);
            for (int i = 0; i < frameSet.length; i++) {
                if (i == idx_ball_first_half || i == idx_ball_second_half) continue;    // the ball has the ball status itself
                int num = frameSet[i].frames[0].N;
                int ball_idx = idx_ball_first_half, diff; // diff is how much later the player joined this half
                if (num >= start_second_half) {
                    ball_idx = idx_ball_second_half;
                    diff = num - start_second_half;
                } else {
                    diff = num - start_first_half;
                }
                // System.out.println("Frameset " + i +" starts at "+num + "diff "+diff + "in half "+ball_idx);
                for (int j = 0; j < frameSet[i].frames.length; j++) {
                    // error detection
                    if (frameSet[i].frames[j].N != frameSet[ball_idx].frames[j + diff].N) {     // this should not happen, but still, in the dataset are wholes
                        System.out.println("i"+i+" j"+j);
                        System.out.println(frameSet[i].frames[j].toString());
                        System.out.println(frameSet[ball_idx].frames[j + diff].toString());

                        int var = frameSet[i].frames[j].N - frameSet[ball_idx].frames[j + diff].N;
                        System.out.println("abweichung " + var);

                        diff = frameSet[i].frames[j].N - frameSet[ball_idx].frames[j].N;    // realign
                        //System.exit(-1);

                    }
                    frameSet[i].frames[j].BallPossession = frameSet[ball_idx].frames[j + diff].BallPossession;
                    frameSet[i].frames[j].BallStatus = frameSet[ball_idx].frames[j + diff].BallStatus;
                }
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public static String leftPad(String originalString, int length,
                                 char padCharacter) {
        StringBuilder sb = new StringBuilder();
        System.out.println("L "+sb.length() + " v "+originalString.length());
        while (sb.length() + originalString.length() < length) {
            sb.append(padCharacter);
            System.out.println("l "+sb.length() + " v "+originalString.length());
        }
        sb.append(originalString);

        return sb.toString();
    }


    public static void analyze() {

        // create a table view

        Object rowData[][] = new Object[frameSet.length][7];
        for (int i = 0; i < frameSet.length; i++) {

            rowData[i][0] = match.getPlayer(frameSet[i].Object) != null ?
                match.getPlayer(frameSet[i].Object).ShirtNumber :
                "-";
            rowData[i][1] = match.getPlayer(frameSet[i].Object) != null ?
                match.getPlayer(frameSet[i].Object).ShortName :
                frameSet[i].Object;
            rowData[i][2] = frameSet[i].firstHalf;
            rowData[i][3] = frameSet[i].getSum() / frameSet[i].getCount(0);
            rowData[i][4] =
                String.format("%.1f", (frameSet[i].getCount(-1) / 25.0 / 60)) + " | " +
                String.format("%.1f", (frameSet[i].getCount(0) / 25.0 / 60)) + " | " +
                String.format("%.1f", (frameSet[i].getCount(1) / 25.0 / 60));

            rowData[i][5] = frameSet[i].getSum() / 25.0 / 60 / 60;
            rowData[i][6] =
                leftPad("" + frameSet[i].getSprintCount(-1), 4, ' ') + " | " +
                leftPad("" + frameSet[i].getSprintCount(0), 4, ' ') + " | " +
                leftPad("" + frameSet[i].getSprintCount(1), 4, ' ');


        }
        Object columnNames[] = { "#", "Object", "firstHalf", "mean [km/h]", "duration [min]", "distance [km]", "#sprints (all, inter, active), per minute"};
        new Table(rowData, columnNames);



        int[] border = {10000, 33000, 56000, 100000, 123000, 146000, 200000};
        MeanData[] dat = new MeanData[border.length - 1];
        for (int i = 0; i < border.length - 1; i++) {   // create Meandaata objects
            dat[i] = new MeanData();
        }
        for (int i = 0; i < frameSet.length; i++) {
            if (frameSet[i].Club.equals("ball")) continue;
            System.out.println(frameSet[i].toString());
            Frame[] frames = frameSet[i].frames;    // quickref
            for (int j = 0; j < frames.length; j++) {
                // in which range?
                for (int k = border.length - 2; k >= 0; k--) {
                    if (frames[j].BallStatus == 0) {
                        if (frames[j].N >= border[k]) {
                            dat[k].sum += frames[j].S;
                            dat[k].count++;
                            dat[k].sq_sum += frames[j].S * frames[j].S;
                            break;
                        }
                    }
                }
            }
        }

        for (int i = 0; i < border.length - 1; i++) {
            dat[i].mean = dat[i].sum / dat[i].count;
            dat[i].var = dat[i].sq_sum / dat[i].count - dat[i].mean * dat[i].mean;
            dat[i].sd = (float)Math.sqrt(dat[i].var);
            System.out.println("mean "+dat[i]);
        }
        visField.updateData(dat, frameSet);

        //  canvas.updateData(dat, frameSet);
    }
    
    
}

