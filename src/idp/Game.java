package idp;

import java.io.FileWriter;
import java.io.IOException;

import static idp.idp.position;

/**
 * Created by Andre on 02/05/2016.
 */
// a Game collects all information about a game, that is framesets, events and matchinformation
public class Game {
    int idx_ball_first_half = -1, idx_ball_second_half = -1;
    int gap = 0;
    FrameSet[] frameSet;
    public Game() {

    }


    public void analyzeFrameSet(FrameSet[] frameSet) {
        this.frameSet = frameSet;
        for (int i = 0; i < frameSet.length; i++) {
            if (frameSet[i].isBall) {       // determine which frameset is the ball
                if (frameSet[i].firstHalf) {
                    idx_ball_first_half = i;
                } else {
                    idx_ball_second_half = i;
                }
            }
            Frame[] frames = frameSet[i].frames;
            int last_n = frames[0].N;
            System.out.println("a " + frames.length);
            for (int j = 1; j < frames.length; j++) {
                if (frames[j].N != last_n + 1) {
                    System.out.println("error" + (frames[j].N - last_n - 1));
                    gap += (frames[j].N - last_n - 1);
                }
                last_n = frames[j].N;
            }
        }
        spreadBallStatus();
    }

    public FrameSet[] spreadBallStatus() {
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
        for (int i = 0; i < frameSet.length; i++) {
            frameSet[i].analyze(getBallFirstHalf(frameSet[i].firstHalf));  // per generate numbers
        }
        return  frameSet;
    }

    public void writeCSV() {
        try
        {
            FileWriter writer = new FileWriter(frameSet[0].Match + ".csv");

            writer.append("Frameset,Sprint Count,Mean vel,vel2,ingame, in paused game, in active game");
            writer.append(',');
            writer.append("Age");
            writer.append('\n');

            for (int i = 0; i < frameSet.length; i++) {
                FrameSet fs = frameSet[i];
                writer.append(
                    fs.Object + ", " +
                        fs.getSprintCount() + ", " +
                        (fs.getSpeed() / fs.getCount()) + "," +
                        (fs.getSpeed(0,15) / fs.getCount(0,15)) + "," +
                        (fs.getSpeed(15,30) / fs.getCount(15,30)) + "," +
                        (fs.getSpeed(30,45) / fs.getCount(30,45)) + "."+
                        (fs.getCount(-1) / 25.0 / 60) + "," +
                        (fs.getCount(0) / 25.0 / 60) + "," +
                        (fs.getCount(1) / 25.0 / 60) + "\n"
                );
            }


            //generate whatever data you want

            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public FrameSet getBallFirstHalf(boolean firstHalf) {
        if (firstHalf) {
            return frameSet[idx_ball_first_half];
        } else {
            return frameSet[idx_ball_second_half];
        }
    }
}
