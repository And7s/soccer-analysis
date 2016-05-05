package idp;

import java.io.*;

import static idp.idp.position;

/**
 * Created by Andre on 02/05/2016.
 */
// a Game collects all information about a game, that is framesets, events and matchinformation
public class Game {
    int idx_ball_first_half = -1, idx_ball_second_half = -1;

    FrameSet[] frameSet;
    public Match match;
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
                    frameSet[i].frames_missing += (frames[j].N - last_n - 1);
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
            int ball_idx, diff; // diff is how much later the player joined this half

            if (frameSet[i].firstHalf) {
                ball_idx = idx_ball_first_half;
                diff = num - start_first_half;
            } else {
                ball_idx = idx_ball_second_half;
                diff = num - start_second_half;
            }

            // System.out.println("Frameset " + i +" starts at "+num + "diff "+diff + "in half "+ball_idx);
            for (int j = 0; j < frameSet[i].frames.length; j++) {
                // error detection
                if (frameSet[i].frames[j].N != frameSet[ball_idx].frames[j + diff].N) {     // this should not happen, but still, in the dataset are wholes

                    int var = frameSet[i].frames[j].N - frameSet[ball_idx].frames[j + diff].N;
                    System.out.println("abweichung " + var);

                    diff = frameSet[i].frames[j].N - frameSet[ball_idx].frames[j].N;    // realign
                    //System.exit(-1);

                }
                frameSet[i].frames[j].BallPossession = frameSet[ball_idx].frames[j + diff].BallPossession;
                frameSet[i].frames[j].BallStatus = frameSet[ball_idx].frames[j + diff].BallStatus;
            }

        }
        // precalculate numbers
        for (int i = 0; i < frameSet.length; i++) {
            frameSet[i].analyze(getBallFirstHalf(frameSet[i].firstHalf));  // per generate numbers
        }
        return  frameSet;
    }

    public void writeCSV() {
        if (match == null) return;  // dont knwo about the order, the seperate files are read
        if (frameSet == null) return;

        try
        {
            //FileWriter writer = new FileWriter(frameSet[0].Match + ".csv");

            OutputStream os = new FileOutputStream(frameSet[0].Match + ".csv");
            os.write(239);
            os.write(187);
            os.write(191);

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));



            writer.print(match.getDescr() + "\n");
            writer.print("Frameset," +
                "Sprint Count,Mean vel total,Mean vel-15,Mean vel-30,Mean vel-45," +
                "in total game, in paused game, in active game," +
                "speed minmax -15, speed minmax -30, speed minmax -45," +
                "framesmissing, first half, club"
            );
            writer.write('\n');

            for (int i = 0; i < frameSet.length; i++) {
                FrameSet fs = frameSet[i];
                writer.write(
                    match.getPlayer(fs.Object).ShortName + ", " +
                        fs.getSprintCount() + "," +
                        (fs.getSpeed() / fs.getCount()) + "," +
                        (fs.getSpeed(0,15) / fs.getCount(0,15)) + "," +
                        (fs.getSpeed(15,30) / fs.getCount(15,30)) + "," +
                        (fs.getSpeed(30,45) / fs.getCount(30,45)) + ","+
                        (fs.getCount(-1) / 25.0 / 60) + "," +
                        (fs.getCount(0) / 25.0 / 60) + "," +
                        (fs.getCount(1) / 25.0 / 60) + ","+
                        fs.getSpeedMin(0,15)  + " - " + fs.getSpeedMax(0,15) + "," +
                        fs.getSpeedMin(15,30)  + " - " + fs.getSpeedMax(15,30) + "," +
                        fs.getSpeedMin(30,45)  + " - " + fs.getSpeedMax(30,45) + "," +
                        fs.frames_missing + "," +
                        fs.firstHalf + "," +
                        match.getTeam(fs.Club).name + "," +
                        "\n"
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
