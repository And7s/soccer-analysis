package idp;

import java.util.Date;

/**
 * Created by Andre on 19/04/2016.
 */
public class Event {
    public String type, team, player;
    public Date date;
    public int T;

    public String toString() {
        return "Event: " + type + " player: " + player + " date " + date + "(frame: " + T + ")";
    }
}
