package idp;

import java.awt.*;

/**
 * Created by Andre on 23/04/2016.
 */
public class Filter {
    double scale;
    public Filter(float scale) {
        this.scale = scale;
    }
    double Frames(Frame f) {
        return 0;
    }
    Color getColor(double f) {
        return Color.black;
    }
}
