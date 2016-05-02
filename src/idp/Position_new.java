package idp;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Random;

/**
 * Created by Andre on 20/04/2016.
 */

class InvalidPositionDataSet extends Exception {

}

public class Position_new {

    public static int checkType(String path) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in;
        try {
            in = new FileInputStream(path);
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
            if (streamReader.hasNext()) {
                streamReader.next();
                if (streamReader.isStartElement()) {
                    if (streamReader.getLocalName().equals("PutDataRequest")) {
                        System.out.println("could be a position dataset");
                        do streamReader.next(); while(!streamReader.isStartElement());

                        String tag = streamReader.getLocalName();
                        System.out.println(tag);
                        if (tag.equals("Positions")) {
                            System.out.println("position 14/15 season");
                            return 1;
                        } else if(tag.equals("Event")) {
                            System.out.println("Event");
                            return 2;
                        } else if (tag.equals("MatchInformation")) {
                            System.out.println("match");
                            return 3;
                        } else if (tag.equals("MetaData")) {
                            System.out.println("position 13/14");
                            return 0;
                        } else {
                            System.out.println("undefined " + tag);
                            return -1;
                        }
                    }
                    //System.out.println(streamReader.getLocalName());
                }
            }
            streamReader.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    public static FrameSet[] readPosition(String fileName, int FORMAT) throws InvalidPositionDataSet{
        int idx_ball_first_half = -1;
        int idx_ball_second_half = -1;  // define to be not in the fst index (0)
        FrameSet[] frameSet;

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in;
        long startTime = System.nanoTime();

        try {
            Frame[] tmp_frames = new Frame[100000];
            FrameSet[] tmp_framesets = new FrameSet[100];
            int tmp_c_framesets = 0;
            int tmp_c_nodes = 0;


            in = new FileInputStream(fileName);
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);

            while (streamReader.hasNext()) {
                if (streamReader.isStartElement()) {
                    String tag = streamReader.getLocalName();



                    /*System.out.println(streamReader.getLocalName());
                    int count = streamReader.getAttributeCount();
                    for (int i = 0; i < count; i++) {
                        System.out.println("\t" + streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
                    }*/



                    if (tag.equals("Frame")) {
                        Frame frame = new Frame();

                        frame.N = Integer.parseInt(streamReader.getAttributeValue(null, "N"));
                        if (tmp_c_framesets == idx_ball_first_half || tmp_c_framesets == idx_ball_second_half) {  // the team ball has the ballstatus
                            String ball_posession = streamReader.getAttributeValue(null, "BallPossession");

                            if (ball_posession == null) {
                                // OUTPUT NODE
                                System.out.println(streamReader.getLocalName());
                                int count = streamReader.getAttributeCount();
                                for (int i = 0; i < count; i++) {
                                    System.out.println("\t" + streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
                                }
                                // OUTPUT NODE END
                                throw new InvalidPositionDataSet();
                            }
                            frame.BallPossession = Integer.parseInt(ball_posession);
                            frame.BallStatus = Integer.parseInt(streamReader.getAttributeValue(null, "BallStatus"));

                        }

                        frame.X = Float.parseFloat(streamReader.getAttributeValue(null, "X"));
                        frame.Y = Float.parseFloat(streamReader.getAttributeValue(null, "Y"));
                        frame.S = Float.parseFloat(streamReader.getAttributeValue(null, "S"));  // km/h
                        tmp_frames[tmp_c_nodes++] = frame;

                    } else if(tag.equals("FrameSet")) {
                        // normalize different attribute naming across seasons
                        String[] ClubIdent = {"Club", "TeamId"},
                            MatchIdent = {"Match", "MatchId"},
                            ObjectIdent = {"Object", "PersonId"};
System.out.println("object is "+ ObjectIdent[FORMAT]);
                        FrameSet fr = new FrameSet();
                        fr.Club = streamReader.getAttributeValue(null, ClubIdent[FORMAT]);
                        fr.Match = streamReader.getAttributeValue(null, MatchIdent[FORMAT]);
                        fr.Object = streamReader.getAttributeValue(null, ObjectIdent[FORMAT]);
                        fr.firstHalf = streamReader.getAttributeValue(null, "GameSection").equals("firstHalf");


                        if (fr.Object.equals("DFL-OBJ-0000XT")) {
                            System.out.println("found ball" + tmp_c_framesets);
                            if (fr.firstHalf) {
                                idx_ball_first_half = tmp_c_framesets;
                            } else {
                                idx_ball_second_half = tmp_c_framesets;
                            }
                            fr.isBall = true;
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
                        tmp_framesets[tmp_c_framesets].frames = new Frame[tmp_c_nodes];
                        System.arraycopy(tmp_frames, 0,  tmp_framesets[tmp_c_framesets].frames, 0, tmp_c_nodes);
                        System.out.println(tmp_framesets[tmp_c_framesets].toString());
                        tmp_c_nodes = 0;
                        tmp_c_framesets++;
                    }
                }

                streamReader.next();
            }// end of xml
            streamReader.close();

            System.out.println("READ "+tmp_c_framesets);
            frameSet = new FrameSet[tmp_c_framesets];
            System.arraycopy(tmp_framesets, 0, frameSet, 0, tmp_c_framesets);
            long duration = System.nanoTime() - startTime;
            System.out.println("took: " + (duration / 1E9) +"s");

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new FrameSet[0];
        } catch (XMLStreamException e) {
            e.printStackTrace();
            return new FrameSet[0];
        }
        return frameSet;
    }
}
