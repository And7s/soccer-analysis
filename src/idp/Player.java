package idp;

public class Player {
    String FirstName, LastName, PlayingPosition, PersonId, ShortName;
    int ShirtNumber;
    boolean Starting;

    public String toString() {
        return "Player " + ShortName + " id: " + PersonId;
    }
}
