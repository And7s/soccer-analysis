package idp;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by Andre on 20/04/2016.
 */
public class Position {
    private int idx_ball_first_half, idx_ball_second_half;
    public FrameSet[] frameSet;

    public FrameSet[] fakeData() {
        Random rand = new Random();
        idx_ball_first_half = 0;
        idx_ball_second_half = 1;

        int cur_ball_status = 0;
        int cur_ball_posession = 1;

        frameSet = new FrameSet[58];
        for (int i = 0; i < 58; i++) {  // framesets
            frameSet[i] = new FrameSet();
            frameSet[i].Club = (i < 2) ? "Ball" : "A";
            frameSet[i].firstHalf = (i % 2) == 0;
            frameSet[i].Match = "asd";
            frameSet[i].Object = (i < 2) ? "DFL-OBJ-0000XT" : (Math.random() > 0.9 ? "DFL-OBJ-0000VY": "DFL-OBJ-00003M");

            frameSet[i].frames = new Frame[1000];
            float cur_pos_x = (float) Math.random() * 105f - 105f / 2.0f,
                cur_pos_y = (float) Math.random() * 68f - 68f / 2.0f,
                cur_goal_x = (float) Math.random() * 105f - 105f / 2.f,
                cur_goal_y = (float) Math.random() * 68f - 68f / 2.0f,
                recalc_in = (float) Math.abs(rand.nextGaussian() * 100);
            float recalc_at = recalc_in;
            System.out.println("goal "+cur_goal_x+" "+cur_goal_y);
            int first_half = i % 2;
            for (int j = 0; j < 1000; j++) {
                float perc = (recalc_at - j) / recalc_in;
                if (Math.random() > 0.99) {
                    cur_ball_status = Math.abs(cur_ball_status - 1);    // alters between 0 and 1
                }
                if (Math.random() > 0.99) {
                    cur_ball_posession = (cur_ball_posession % 2) + 1;   // alters 1 and 2
                }

                Frame frame = new Frame();

                frame.BallStatus =  cur_ball_status;
                frame.BallPossession = cur_ball_posession;
                frame.N = j + (i % 2) * 100000;
                frame.S = (j > 0) ?
                    (float) Math.abs(rand.nextGaussian() * 10) * 0.3f + frameSet[i].frames[j - 1].S * 0.7f:
                    (float) Math.abs(rand.nextGaussian() * 10);
                frame.A = (j > 0) ? (frame.S - frameSet[i].frames[j - 1].S) * 25 : 0;
                frame.X = cur_pos_x * (1.0f - perc) + cur_goal_x * perc ;
                frame.Y = cur_pos_y * (1.0f - perc) + cur_goal_y * perc ;
                frameSet[i].frames[j] = frame;
                if (j >= recalc_at) {
                    cur_goal_x = (float) Math.random() * 105f - 105f / 2.0f;
                    cur_goal_y = (float) Math.random() * 68f - 68f / 2.0f;
                    recalc_in = (float) Math.abs(rand.nextGaussian() * 100);
                    recalc_at = j + recalc_in;
                }
            }
        }

        return spreadBallStatus(frameSet);
    }

    public FrameSet[] readData() {

        idx_ball_first_half = -1;
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
                        frame.S = Float.parseFloat(streamReader.getAttributeValue(null, "S"));  // km/h
                        frame.A = 0;    // is set in analyze
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

            frameSet = spreadBallStatus(frameSet);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (int i = 0; i < frameSet.length; i++) {
 // ERROR           frameSet[i].analyze();
        }
        return frameSet;
    }
    public FrameSet[] spreadBallStatus(FrameSet[] frameSet) {
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
        return  frameSet;
    }

    public FrameSet getBallFirstHalf(boolean firsHalf) {
        if (firsHalf) {
            return frameSet[idx_ball_first_half];
        }else {
            return frameSet[idx_ball_second_half];
        }
    }



}
