package idp;

public class Frame {
    public int N, BallStatus, BallPossession,
        M; // which minute
    public double X, Y, S, A;
    public String toString() {
        return "Frame N: " + N + " X: " + X + " Y: " + Y + " S: " + S + " Ballstatus: " + BallStatus + " BallPossession: " + BallPossession;
    }
}
