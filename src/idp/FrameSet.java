package idp;

class MinuteData {
    double sum, sq_sum, count;
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
            sum += agg_speed[i].sum;
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
            count += agg_speed[i].count;
        }
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
    public int getSprintCount(int filter) { // filter [-1 = all, 0 = interrupt, 1 = active]
        int count_sprint = 0;
        for (int i = 0; i < agg_sprints.length; i++) {
            count_sprint += agg_sprints[i].count;
        }
        return count_sprint;
    }



    // when a frameset is entirely loaded i can analze it to prepare results that might propably be reused
    public void analyze() {
        int duration_game_min = (int) Math.ceil(frames.length / 25.0 / 60);
        agg_sprints = new MinuteData[duration_game_min];  // will hold the aggregated information of the sprints per minute
        agg_speed = new MinuteData[duration_game_min];
        int dur_sprint = 0;
        int cur_minute = -1;
        System.out.println("have frames "+frames.length+" and minutes "+agg_sprints.length);

        for (int i = 0; i < frames.length; i++) {
            // check for new minute
            if (i % (25 * 60) == 0) {   // new minute in the game
                cur_minute++;
                agg_sprints[cur_minute] = new MinuteData();
                agg_speed[cur_minute] = new MinuteData();
            }
            // sprints
            if (frames[i].S / 3.6 > 7) {  // convert to m/s
                dur_sprint++;
                if (dur_sprint == 25) {
                    agg_sprints[cur_minute].sum++;
                }
            } else {
                dur_sprint = 0;
            }
            agg_sprints[cur_minute].count++;


            // speed
            agg_speed[cur_minute].count++;
            agg_speed[cur_minute].sum += frames[i].S;
            agg_speed[cur_minute].sq_sum += (frames[i].S * frames[i].S);
        }
        System.out.println("minute "+cur_minute);
    }

}
