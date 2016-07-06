package idp;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

class Team {
    String id, name;
}
public class Match {

    public Player[] players = new Player[50];
    public Team[] teams = new Team[2];
    private int c_player = 0;
    private int c_teams = 0;
    public String MatchId, AwayTeamId, HomeTeamId, Season, KickoffTime, Competition, GameTitle;
    public Match(String filename) {


        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in;
        long startTime = System.nanoTime();

        Player p2 = new Player();
        p2.ShortName = "Ball";
        p2.PersonId = "DFL-OBJ-0000XT";
        p2.PlayingPosition = "-";
        players[c_player++] = p2;

        try {
            in = new FileInputStream(filename);
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
            int next = streamReader.nextTag(); // Advance to "book" element

            while (streamReader.hasNext()) {
                if (streamReader.isStartElement()) {
                    String tag = streamReader.getLocalName();
                    if (tag.equals("Player")) {
                        Player p = new Player();
                        p.FirstName = streamReader.getAttributeValue(null, "FirstName");
                        p.LastName = streamReader.getAttributeValue(null, "LastName");
                        p.PersonId = streamReader.getAttributeValue(null, "PersonId");
                        p.PlayingPosition = streamReader.getAttributeValue(null, "PlayingPosition");
                        if (p.PlayingPosition == null) p.PlayingPosition = "";  // fallback
                        // System.out.println("player "+p.PersonId+" = "+p.PlayingPosition);
                        p.Starting = streamReader.getAttributeValue(null, "Starting").equals("true");
                        p.ShortName = streamReader.getAttributeValue(null, "Shortname");
                        p.ShirtNumber = Integer.parseInt(streamReader.getAttributeValue(null, "ShirtNumber"));
                        players[c_player++] = p;
                        // System.out.println(p.toString());
                    }
                    if (tag.equals("General")) {
                        MatchId = streamReader.getAttributeValue(null, "MatchId");
                        AwayTeamId = streamReader.getAttributeValue(null, "AwayTeamId");
                        HomeTeamId = streamReader.getAttributeValue(null, "HomeTeamId");
                        Season = streamReader.getAttributeValue(null, "Season");
                        KickoffTime = streamReader.getAttributeValue(null, "KickoffTime");
                        Competition = streamReader.getAttributeValue(null, "Competition");
                        GameTitle = streamReader.getAttributeValue(null, "GameTitle");


                    }
                    if (tag.equals("Team")) {
                        int count = streamReader.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            System.out.println(streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
                        }
                        teams[c_teams] = new Team();
                        teams[c_teams].id = streamReader.getAttributeValue(null, "TeamId");
                        teams[c_teams].name = streamReader.getAttributeValue(null, "TeamName");
                        c_teams++;


                    }
                    int count = streamReader.getAttributeCount();
                    for (int i = 0; i < count; i++) {
                        //System.out.println(streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
                    }
                }
                streamReader.next();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Player getPlayer(String id) {
        for (int i = 0; i < c_player; i++) {
            if (players[i].PersonId.equals(id)) {
                return players[i];
            }
        }
        return null;
    }

    public Team getTeam(String id) {
        for (int i = 0; i < c_teams; i++) {
            if (teams[i].id.equals(id)) {
                return teams[i];
            }
        }
        System.out.println("Cannot find team " + id);
        Team t = new Team();
        return t;
    }

    public String getDescr() {
        return "MatchId," + MatchId+ "\n" +
            "GameTitle," + GameTitle + "\n" +
            "Season," + Season + "\n" +
            "KickoffTime, " + KickoffTime + "\n" +
            "Competition," + Competition + "\n";
    }
}
