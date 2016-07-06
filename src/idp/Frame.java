package idp;

public class Frame {
    public int N; // which minute
    public byte BallStatus, BallPossession, M;
    public float S, A, OS, X, Y;
    public String toString() {
        return "Frame N: " + N + " OS: " + OS + " S: " + S + " Ballstatus: " + BallStatus + " BallPossession: " + BallPossession;
    }
}
