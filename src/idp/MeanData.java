package idp;

public class MeanData {
    public float
        sum = 0,
        mean = 0,
        sq_sum = 0,
        count = 0,
        var = 0,
        sd = 0;
    public String toString() {
        return "sum: " + sum + ", mean: " + mean + ", sd: " + sd + ", count: " + count;
    }
}
