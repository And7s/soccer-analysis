package idp;

class GameSection {
    double sum = 0, sq_sum, count = 0, min = Double.MAX_VALUE, max = Double.MIN_VALUE;

    // merges two game section
    void add(GameSection gs) {
        sum += gs.sum;
        sq_sum += gs.sq_sum;
        count += gs.count;
        min = Math.min(min, gs.min);
        max = Math.max(max, gs.max);
    }
}

class FILTER {
    public static final int ALL = 0, PAUSED = 1, ACTIVE = 2,
    length = 3;
}
class VAR {
    public static final int SPEED = 0, SPRINT = 1, ENERGY = 2, POWER = 3, ACC = 4, SZ0 = 5, SZ1 = 6, SZ2 = 7, SZ3 = 8, SZ4 = 9 ,
    length = 10;
}


public class FrameSet {
    public String Match, Club, Object;
    public boolean firstHalf;
    public boolean isBall = false;
    public Frame frames[];
    public int frames_missing = 0;
    private GameSection[][][] aggregate;


    public String toString() {
        return "FrameSet Obj: " + Object + " Club: " + Club + " Match: " + Match + " firstHalf: " + firstHalf + " frames " + ((frames != null) ? frames.length : "null");
    }

    public double getVar(int var) { return getVar(var, 0, aggregate[var][FILTER.ALL].length, FILTER.ALL);}
    public double getVar(int var, int start, int end) { return getVar(var, start, end, FILTER.ALL); }
    public double getVar(int var, int filter) { return getVar(var, 0, aggregate[var][filter].length, filter); }

    public double getVar(int var, int start, int end, int filter) {
        GameSection[] dat = aggregate[var][filter];
        double val = 0;
        for (int i = start; i < end && i < dat.length; i++) {
            val += dat[i].sum;
        }
        return val;
    }

    // var filter time
    public GameSection getGS(int var, int start, int end, int filter) {
        GameSection gs = new GameSection(); // create new gs
        for (int i = start; i < end; i++) {
            gs.add(aggregate[var][filter][i]);
        }
        return gs;
    }

    public double getVarSq(int var) { return getVarSq(var, 0, aggregate[var][FILTER.ALL].length, FILTER.ALL);}
    public double getVarSq(int var, int start, int end) { return getVarSq(var, start, end, FILTER.ALL); }
    public double getVarSq(int var, int filter) { return getVarSq(var, 0, aggregate[var][filter].length, filter); }

    public double getVarSq(int var, int start, int end, int filter) {
        GameSection[] dat = aggregate[var][filter];
        double val = 0;
        for (int i = start; i < end && i < dat.length; i++) {
            val += dat[i].sq_sum;
        }
        return val;
    }

    public int getVarCount(int var) { return getVarCount(var, 0, aggregate[var][FILTER.ALL].length, FILTER.ALL);}
    public int getVarCount(int var, int start, int end) { return getVarCount(var, start, end, FILTER.ALL); }
    public int getVarCount(int var, int filter) { return getVarCount(var, 0, aggregate[var][filter].length, filter); }

    public int getVarCount(int var, int start, int end, int filter) {
        GameSection[] dat = aggregate[var][filter];
        int count = 0;
        for (int i = start; i < end && i < dat.length; i++) {
            count += dat[i].count;
        }
        return count;
    }

    public double getVarMin(int var, int start, int end, int filter) {
        GameSection[] dat = aggregate[var][filter];
        double min = Double.MAX_VALUE;
        for (int i = 0; i < dat.length; i++) {
            min = Math.min(min, dat[i].min);
        }
        return min;
    }
    public double getVarMax(int var, int start, int end, int filter) {
        GameSection[] dat = aggregate[var][filter];
        double max = Double.MIN_VALUE;
        for (int i = 0; i < dat.length; i++) {
            max = Math.max(max, dat[i].max);
        }
        return max;
    }


    public void addValue(int var, int status, int min, double val) {
        int filter = (status == 0) ? FILTER.PAUSED : FILTER.ACTIVE;
        for (int i = 0; i <= filter; i += filter) { // does alternate all and the corresponding status
            aggregate[var][i][min].sum += val;
            aggregate[var][i][min].sq_sum += (val * val);
            aggregate[var][i][min].count ++;
            if (val < aggregate[var][i][min].min)
                aggregate[var][i][min].min = val;
            if (val > aggregate[var][i][min].max)
                aggregate[var][i][min].max = val;
        }
    }

    // when a frameset is entirely loaded i can analze it to prepare results that might propably be reused
    public void analyze(FrameSet frameSet) {

        // preadjustements
        int ball_fs_offset = frameSet.frames[0].N;
        Frame[] ball_frames = frameSet.frames;

        int last_frame = frameSet.frames[frameSet.frames.length - 1].N;
        int duration_game_min = (int) Math.ceil((last_frame - ball_fs_offset) / 25.0 / 60);

        aggregate = new GameSection[VAR.length][FILTER.length][duration_game_min];

        // initialize objects
        for (int i = 0; i < VAR.length; i++) {
            for (int j = 0; j < FILTER.length; j++) {
                for (int k = 0; k < duration_game_min; k++) {
                    aggregate[i][j][k] = new GameSection();
                }
            }
        }

        int dur_sprint = 0;

        double smooth_factor = 0.7;

        for (int i = 0; i < frames.length; i++) {
            // smooth the graph

            frames[i].S = (float)((i == 0) ? frames[i].OS : (frames[i - 1].S * smooth_factor + frames[i].OS * (1 - smooth_factor)));
            //frames[i].A = (i == 0) ? 0 : frames[i].S - frames[i - 1].S;

            frames[i].A = (float)((i == 0) ? 0 : (frames[i].S - frames[i - 1].S) / 3.6 * 25); // m/s^2
            int cur_minute = (frames[i].N - ball_fs_offset) / 25 / 60; // convert the frame number and offset to playing minute
            frames[i].M = (byte)cur_minute;

            // sprints
            if (frames[i].S / 3.6 > 7) {  // convert to m/s
                dur_sprint++;
                if (dur_sprint == 25) {
                    addValue(VAR.SPRINT, ball_frames[i].BallStatus, cur_minute, 1.0);
                } else {
                    addValue(VAR.SPRINT, ball_frames[i].BallStatus, cur_minute, 0.0);
                }
            } else {
                dur_sprint = 0;
                addValue(VAR.SPRINT, ball_frames[i].BallStatus, cur_minute, 0.0);
            }

            // energy
            double ES = frames[i].A / 9.81;
            double EM = Math.sqrt(Math.pow(9.81, 2) + Math.pow(frames[i].A, 2)) / 9.81;

            double EC = (
                + 155.4 * Math.pow(ES, 5)
                    - 30.4 * Math.pow(ES , 4)
                    - 43.3 * Math.pow(ES, 3)
                    + 46.3 * Math.pow(ES, 2)
                    + 19.5 * ES
                    + 3.6
            ) * EM; // unit J/kg/m
            // TODO: clairify: what about decelerations

            addValue(VAR.ENERGY, ball_frames[i].BallStatus, cur_minute, EC);

            // speed
            addValue(VAR.SPEED, ball_frames[i].BallStatus, cur_minute, frames[i].S);
            addValue(VAR.ACC, ball_frames[i].BallStatus, cur_minute, Math.abs(frames[i].A));

            // speed zones
            double[] zones = {7, 5.5, 4, 2, 0};
            for (int k = VAR.SZ0; k <= VAR.SZ4; k++) {
                if (frames[i].S / 3.6 >= zones[k - VAR.SZ0]) {
                    addValue(k, ball_frames[i].BallStatus, cur_minute, frames[i].S);
                    break;  // only account to the highes speed zone
                }
            }
        }
    }
}
