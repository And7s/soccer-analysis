package idp;

public class MeanData {
    public float
        sum = 0,
        mean = 0,
        sq_sum = 0,
        count = 0,
        var = 0,
        sd = 0;

    public Mean[] speed_zones = new Mean[5];
    public Mean sprints;

    public MeanData() {
        for (int i = 0; i < speed_zones.length; i++) {
            speed_zones[i] = new Mean();
        }
        sprints = new Mean();
    }
    public String toString() {
        return "sum: " + sum + ", mean: " + mean + ", sd: " + sd + ", count: " + count;
    }
}
class Mean {
    public float mean= 0, sum = 0, count = 0, sq_sum = 0, var = 0, sd = 0;
}