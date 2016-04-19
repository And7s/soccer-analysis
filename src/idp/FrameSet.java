package idp;

public class FrameSet {
    public String Match, Club, Object;
    public boolean firstHalf;
    public Frame frames[];

    private float sum = -1, sq_sum = -1;

    public String toString() {
        return "FrameSet Obj: " + Object + " Club: " + Club + " Match: " + Match + " firstHalf: " + firstHalf + " frames " + ((frames != null) ? frames.length : "null");
    }

    public float getSum() {
        if (sum == -1) {
            sum = 0;
            for (int i = 0; i < frames.length; i++) {
                sum += frames[i].S;
            }
        }
        return sum;
    }

    public float getSqSum() {
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
        int dur_sprint = 0, count_sprint = 0;
        for (int i = 0; i < frames.length; i++) {
            if (filter == -1 || (frames[i].BallStatus == filter)) {
                if (frames[i].S / 3.6 > 7) {  // convert to m/s
                    dur_sprint++;
                } else {
                    if (dur_sprint >= 25) {
                        count_sprint++;
                    }
                    dur_sprint = 0;
                }
            }
        }
        return count_sprint;
    }


}
