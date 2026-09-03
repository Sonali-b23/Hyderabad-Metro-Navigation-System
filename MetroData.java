/**
 * Static data for the Hyderabad Metro network: stations, corridors, and
 * connection costs. Kept separate from MetroApp so the graph-building logic
 * can be reused (and unit-tested) without going through the interactive menu.
 *
 * Cost units:
 *   distance: 1 unit = 100 meters
 *   time:     1 unit = 6 seconds
 *
 * Per-hop values are averages for this network (~1.2km station spacing,
 * ~2.5 min running + dwell time between adjacent stations on the same line).
 * Interchange edges (moving between two lines at a shared/nearby station)
 * are short in distance but cost extra time for the platform walk --
 * that's the whole point of modeling distance and time separately.
 */
public class MetroData {

    private static final int NORMAL_HOP_DISTANCE = 12; // ~1.2km
    private static final int NORMAL_HOP_TIME = 25;      // ~2.5 min running + dwell

    private static final int INTERCHANGE_DISTANCE = 3;  // short walk, ~300m
    private static final int INTERCHANGE_TIME = 60;      // ~6 min: stairs/escalators + platform walk + wait

    // Corridor 1 (Red Line): Miyapur to LB Nagar
    public static final String[] CORRIDOR_1 = {
        "Miyapur", "JNTU College", "KPHB Colony", "Kukatpally", "Dr. B.R. Ambedkar Balanagar",
        "Moosapet", "Bharat Nagar", "Erragadda", "ESI Hospital", "S.R. Nagar", "Ameerpet",
        "Punjagutta", "Irrum Manzil", "Khairatabad", "Lakdikapul", "Assembly", "Nampally",
        "Gandhi Bhavan", "Osmania Medical College", "MG Bus Station", "New Market",
        "Musarambagh", "Dilsukhnagar", "Chaitanyapuri", "Victoria Memorial", "LB Nagar"
    };

    // Corridor 2 (Green Line): JBS Parade Ground to MG Bus Station
    public static final String[] CORRIDOR_2 = {
        "JBS Parade Ground", "Secunderabad West", "Gandhi Hospital", "Musheerabad",
        "RTC X Roads", "Chikkadpally", "Narayanguda", "Sultan Bazar", "MG Bus Station"
    };

    // Corridor 3 (Blue Line): Nagole to Raidurg
    public static final String[] CORRIDOR_3 = {
        "Nagole", "Uppal", "NGRI", "Habsiguda", "Mettuguda", "Secunderabad East", "Parade Grounds",
        "Paradise", "Rasoolpura", "Prakash Nagar", "Begumpet", "Ameerpet", "Madhura Nagar",
        "Yusufguda", "Road No.5 Jubilee Hills", "Jubilee Hills Check Post", "Peddamma Gudi",
        "Madhapur", "Durgam Cheruvu", "HITEC City", "Raidurg"
    };

    /** Builds and returns a fully-connected metro graph. */
    public static Graph_M buildGraph() {
        Graph_M metro = new Graph_M();

        for (String station : CORRIDOR_1) metro.addStation(station);
        for (String station : CORRIDOR_2) metro.addStation(station);
        for (String station : CORRIDOR_3) metro.addStation(station);

        addCorridorConnections(metro, CORRIDOR_1);
        addCorridorConnections(metro, CORRIDOR_2);
        addCorridorConnections(metro, CORRIDOR_3);

        // Interchange stations: Ameerpet (Red<->Blue) is the same shared
        // station name in both corridors so it's already one graph node.
        // MG Bus Station (Red<->Green) and Parade Grounds/JBS Parade Ground
        // (Blue<->Green) need an explicit extra edge for the platform walk.
        metro.addConnection("MG Bus Station", "Ameerpet", INTERCHANGE_DISTANCE, INTERCHANGE_TIME);
        metro.addConnection("Parade Grounds", "JBS Parade Ground", INTERCHANGE_DISTANCE, INTERCHANGE_TIME);

        return metro;
    }

    private static void addCorridorConnections(Graph_M metro, String[] corridor) {
        for (int i = 0; i < corridor.length - 1; i++) {
            metro.addConnection(corridor[i], corridor[i + 1], NORMAL_HOP_DISTANCE, NORMAL_HOP_TIME);
        }
    }

    private MetroData() {
        // static-only holder
    }
}
