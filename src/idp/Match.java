package idp;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class Match {

    public Player[] players = new Player[50];
    public int c_player = 0;

    public Match(String filename) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in;
        long startTime = System.nanoTime();

        int c_nodes = 0;
        Frame[] frames = new Frame[3600000];
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
                        p.ShortName = streamReader.getAttributeValue(null, "Shortname");
                        p.ShirtNumber = Integer.parseInt(streamReader.getAttributeValue(null, "ShirtNumber"));
                        players[c_player++] = p;
                        System.out.println(p.toString());
                    }
                    //System.out.println(streamReader.getLocalName());
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
        System.out.println("Cannot found player " + id);
        return null;
    }
}
