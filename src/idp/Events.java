package idp;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.awt.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Created by Andre on 19/04/2016.
 */
public class Events {

    Event[] event;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd|HH:mm:ss.SSSX");
    public Events(String fileName) {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in;

        Event[] tmp_event = new Event[10000];
        int c_nodes = 0;
        try {
            in = new FileInputStream(fileName);
            XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);
            int next = streamReader.nextTag(); // Advance to "book" element

            while (streamReader.hasNext()) {
                if (streamReader.isStartElement()) {
                    String tag = streamReader.getLocalName();
                    if (tag.equals("Event")) {
                        // System.out.println(tag);
                        /*int count = streamReader.getAttributeCount();
                        for (int i = 0; i < count; i++) {
                            System.out.println("\t" + streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
                        }*/

                        Date date = sdf.parse(streamReader.getAttributeValue(null, "EventTime").replace('T', '|'));
                        Event ev = new Event();
                        ev.type = tag;
                        ev.date = date;
                        tmp_event[c_nodes++] = ev;

                        do {
                            // now inside the Event
                            streamReader.next();
                            if (streamReader.isStartElement()) {

                                /*System.out.println(streamReader.getLocalName());
                                count = streamReader.getAttributeCount();
                                for (int i = 0; i < count; i++) {
                                    System.out.println("\t\t" + streamReader.getAttributeLocalName(i) + ": " + streamReader.getAttributeValue(i));
                                }*/
                            }

                        } while(!streamReader.isEndElement() && streamReader.getLocalName().equals("Event"));   // wait for the closing Event tag

                    }
                }
                streamReader.next();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        event = new Event[c_nodes];
        for (int i = 0; i < c_nodes; i++) {
            event[i] = tmp_event[i];
        }


        Arrays.sort(event, new Comparator<Event>() {
            public int compare(Event e1, Event e2) {
                return (int) (e1.date.getTime() - e2.date.getTime());
            }
        });

        /*for (int i = 0; i < c_nodes; i++) {
            System.out.println("event "+event[i].date.getTime());
        }*/
        // reading over
        System.out.println("read " + c_nodes + " Events");

    }
}
