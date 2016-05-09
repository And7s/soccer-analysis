package idp;

class GameSection {
    double sum, sq_sum, count, min = Double.MAX_VALUE, max = Double.MIN_VALUE;
}
class MinuteData {
    public MinuteData() {
        all = new GameSection();
        active = new GameSection();
        paused = new GameSection();
    }
    GameSection all, active, paused;
}

class FILTER {
    public static final int ALL = 0, PAUSED = 1, ACTIVE = 2,
    length = 3;
}
class VAR {
    public static final int SPEED = 0, SPRINT = 1, ENERGY = 2, POWER = 3,
    length = 4;
}


public class FrameSet {
    public String Match, Club, Object;
    public boolean firstHalf;
    public boolean isBall = false;
    public Frame frames[];
    public int frames_missing = 0;
    private GameSection[][][] aggregate;

    private MinuteData[] agg_energy;
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
/*
    public double getSpeed() {
        return getSpeed(0, agg_speed.length);  // the entire game
    }
    public double getSpeed(int start, int end) { return getSpeed(start, end, -1); }

    public double getSpeed(int start, int end, int filter) {
        double sum = 0;

        for (int i = Math.max(start, 0); i < end && i < agg_speed.length; i++) {
            switch (filter) {
                case -1:    // all
                    sum += agg_speed[i].all.sum;
                    break;
                case 0:
                    sum += agg_speed[i].paused.sum;
                    break;
                case 1:
                    sum += agg_speed[i].active.sum;
                    break;
            }
        }

        return sum;
    }

    public double getSpeedSq() { return getSpeedSq(0, agg_speed.length); }  // the entire game
    public double getSpeedSq(int start, int end) { return getSpeedSq(start, end, -1); }

    public double getSpeedSq(int start, int end, int filter) {
        double sum = 0;

        for (int i = Math.max(start, 0); i < end && i < agg_speed.length; i++) {
            switch (filter) {
                case -1:    // all
                    sum += agg_speed[i].all.sq_sum;
                    break;
                case 0:
                    sum += agg_speed[i].paused.sq_sum;
                    break;
                case 1:
                    sum += agg_speed[i].active.sq_sum;
                    break;
            }
        }

        return sum;
    }


    // how many values are in every minute segment
    public int getCount() { return getCount(0, agg_speed.length); }
    public int getCount(int start, int end) { return getCount(start, end, -1); }

    public int getCount(int start, int end, int filter) {
        int count = 0;
        for (int i = Math.max(start, 0); i < end && i < agg_speed.length; i++) {
            switch (filter) {
                case -1:    // all
                    count += agg_speed[i].all.count;
                    break;
                case 0:
                    count += agg_speed[i].paused.count;
                    break;
                case 1:
                    count += agg_speed[i].active.count;
                    break;
            }
        }
        return count;
    }
*/


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



    // get minimum of speed
    /*
    public double getSpeedMin() { return getSpeedMin(0, agg_speed.length); }
    public double getSpeedMin(int start, int end) { return getSpeedMin(start, end, -1); }

    public double getSpeedMin(int start, int end, int filter) { // filter [-1 = all, 0 = interrupt, 1 = active]
        double min = Double.MAX_VALUE;
        for (int i = start; i < end && i < agg_speed.length; i++) {
            switch (filter) {
                case -1:    // all
                    min = Math.min(min, agg_speed[i].all.min);
                    break;
                case 0:
                    min = Math.min(min, agg_speed[i].paused.min);
                    break;
                case 1:
                    min = Math.min(min, agg_speed[i].active.min);
                    break;
            }

        }
        return min;
    }*/
/*
    public double getSpeedMax() { return getSpeedMax(0, agg_speed.length); }
    public double getSpeedMax(int start, int end) { return getSpeedMax(start, end, -1); }

    public double getSpeedMax(int start, int end, int filter) { // filter [-1 = all, 0 = interrupt, 1 = active]
        double max = Double.MIN_VALUE;
        for (int i = start; i < end && i < agg_speed.length; i++) {
            switch (filter) {
                case -1:    // all
                    max = Math.max(max, agg_speed[i].all.max);
                    break;
                case 0:
                    max = Math.max(max, agg_speed[i].paused.max);
                    break;
                case 1:
                    max = Math.max(max, agg_speed[i].active.max);
                    break;
            }

        }
        return max;
    }*/

    public double getEnergy() { return getEnergy(0, agg_energy.length); }
    public double getEnergy(int start, int end) { return getEnergy(start, end, -1); }

    public double getEnergy(int start, int end, int filter) { // filter [-1 = all, 0 = interrupt, 1 = active]

        double energy = 0;
        for (int i = start; i < end && i < agg_energy.length; i++) {
            switch (filter) {
                case -1:    // all
                    energy += agg_energy[i].all.sum;
                    break;
                case 0:
                    energy += agg_energy[i].paused.sum;
                    break;
                case 1:
                    energy += agg_energy[i].active.sum;
                    break;
            }
        }
        return energy;
    }



    // when a frameset is entirely loaded i can analze it to prepare results that might propably be reused
    public void analyze(FrameSet frameSet) {

        // preadjustements
        int ball_fs_offset = frameSet.frames[0].N;
        Frame[] ball_frames = frameSet.frames;


        int last_frame = frames[frames.length - 1].N;
        int duration_game_min = (int) Math.ceil((last_frame - ball_fs_offset) / 25.0 / 60);

        agg_energy = new MinuteData[duration_game_min];
        aggregate = new GameSection[VAR.length][FILTER.length][duration_game_min];

        for (int i = 0; i < VAR.length; i++) {

            for (int j = 0; j < FILTER.length; j++) {
                for (int k = 0; k < duration_game_min; k++) {
                    aggregate[i][j][k] = new GameSection();
                }
            }
        }

        for (int i = 0; i < duration_game_min; i++) {

            agg_energy[i] = new MinuteData();
        }
        int dur_sprint = 0;


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

            agg_energy[cur_minute].all.sum += EC;
            agg_energy[cur_minute].all.sq_sum += (EC * EC);        // TODO: write helper, write loops, simplyfy somehow
            agg_energy[cur_minute].all.count++;                     // TODO: add min/max



            // speed
            addValue(VAR.SPEED, ball_frames[i].BallStatus, cur_minute, frames[i].S);

            if (ball_frames[i].BallStatus == 1) {       //active

                agg_energy[cur_minute].active.sum += EC;
                agg_energy[cur_minute].active.sq_sum += (EC * EC);
                agg_energy[cur_minute].active.count++;
            } else {        // paused

                agg_energy[cur_minute].paused.sum += EC;
                agg_energy[cur_minute].paused.sq_sum += (EC * EC);
                agg_energy[cur_minute].paused.count++;
            }
        }
        System.out.println("analyze end");

    }

}
