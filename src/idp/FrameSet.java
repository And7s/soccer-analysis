package idp;

class GameSection {
    double sum, sq_sum, count;
}
class MinuteData {
    public MinuteData() {
        all = new GameSection();
        active = new GameSection();
        paused = new GameSection();
    }
    GameSection all, active, paused;
}

public class FrameSet {
    public String Match, Club, Object;
    public boolean firstHalf;
    public Frame frames[];

    private double sum = -1, sq_sum = -1;

    private MinuteData[] agg_sprints, agg_speed;
    public String toString() {
        return "FrameSet Obj: " + Object + " Club: " + Club + " Match: " + Match + " firstHalf: " + firstHalf + " frames " + ((frames != null) ? frames.length : "null");
    }

    public double getSpeed() {
        return getSpeed(0, agg_speed.length);  // the entire game
    }

    public double getSpeed(int start, int end) {
        double sum = 0;
        for (int i = Math.max(start, 0); i < end && i < agg_speed.length; i++) {
            sum += agg_speed[i].all.sum;
        }
        return sum;
    }

    // how many values are in every minute segment
    public int getCount() {
        return getCount(0, agg_speed.length);
    }

    public int getCount(int start, int end) {
        int count = 0;
        for (int i = Math.max(start, 0); i < end && i < agg_speed.length; i++) {
            count += agg_speed[i].all.count;
        }
        System.out.println("count called with "+start+" "+end+"= "+count);
        return count;
    }

    public double getSqSum() {
        if (sq_sum == -1) {
            sq_sum = 0;
            for (int i = 0; i < frames.length; i++) {
                sq_sum += frames[i].S * frames[i].S;
            }
        }
        return sq_sum;
    }

    public int getCount(int filter) { // filter [-1 = all, 0 = interrupt, 1 = active]
        if (filter == - 1) return frames.length;
        int count = 0;
        for (int i = 0; i < frames.length; i++) {
            if (frames[i].BallStatus == filter) {
                count++;
            }
        }

        return count;
    }

    // get number of sprints ( > 1s min 7m/s)
    public int getSprintCount() {
        return getSprintCount(0, agg_sprints.length);
    }

    public int getSprintCount(int start, int end) { // filter [-1 = all, 0 = interrupt, 1 = active]
        int count_sprint = 0;
        for (int i = start; i < end && i < agg_sprints.length; i++) {
            count_sprint += agg_sprints[i].all.sum;
        }
        System.out.println("sprintcount called with "+start+" "+end+"= "+count_sprint);
        return count_sprint;
    }



    // when a frameset is entirely loaded i can analze it to prepare results that might propably be reused
    public void analyze() {
        // preadjustements
        int ball_fs_offset = idp.position.getBallFirstHalf(firstHalf).frames[0].N;
        int last_frame = frames[frames.length - 1].N;
        int duration_game_min = (int) Math.ceil((last_frame - ball_fs_offset) / 25.0 / 60);
        agg_sprints = new MinuteData[duration_game_min];  // will hold the aggregated information of the sprints per minute
        agg_speed = new MinuteData[duration_game_min];
        for (int i = 0; i < duration_game_min; i++) {
            agg_sprints[i] = new MinuteData();
            agg_speed[i] = new MinuteData();
        }
        int dur_sprint = 0;

        System.out.println("have frames "+frames.length+" and minutes "+agg_sprints.length);
        double smooth_factor = 0.7;


        for (int i = 0; i < frames.length; i++) {
            // smooth the graph
            frames[i].S = (i == 0) ? frames[i].S : (frames[i - 1].S * smooth_factor + frames[i].S * (1 - smooth_factor));
            //frames[i].A = (i == 0) ? 0 : frames[i].S - frames[i - 1].S;

            frames[i].A = (i == 0) ? 0 : (frames[i].S - frames[i - 1].S) / 3.6 * 25; // m/s^2
            int cur_minute = (frames[i].N - ball_fs_offset) / 25 / 60; // convert the frame number and offset to playing minute
            frames[i].M = cur_minute;

            // sprints
            if (frames[i].S / 3.6 > 7) {  // convert to m/s
                dur_sprint++;
                if (dur_sprint == 25) {
                    agg_sprints[cur_minute].all.sum++;
                }
            } else {
                dur_sprint = 0;
            }
            agg_sprints[cur_minute].all.count++;

            // speed
            agg_speed[cur_minute].all.count++;
            agg_speed[cur_minute].all.sum += frames[i].S;
            agg_speed[cur_minute].all.sq_sum += (frames[i].S * frames[i].S);
        }
    }

}
