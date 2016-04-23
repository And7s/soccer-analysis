package idp;

import java.awt.*;

/**
 * Created by Andre on 23/04/2016.
 */
public class Filter {
    float scale;
    public Filter(float scale) {
        this.scale = scale;
    }
    float Frames(Frame f) {
        return 0;
    }
    Color getColor(float f) {
        return Color.black;
    }
}
