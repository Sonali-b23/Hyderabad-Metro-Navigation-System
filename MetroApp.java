import java.util.*;

public class MetroApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Graph_M metro = new Graph_M();

        // --- Hyderabad Metro Stations ---

        // Corridor 1: Miyapur to LB Nagar
        String[] corridor1 = {
            "Miyapur", "JNTU College", "KPHB Colony", "Kukatpally", "Dr. B.R. Ambedkar Balanagar",
            "Moosapet", "Bharat Nagar", "Erragadda", "ESI Hospital", "S.R. Nagar", "Ameerpet",
            "Punjagutta", "Irrum Manzil", "Khairatabad", "Lakdikapul", "Assembly", "Nampally",
            "Gandhi Bhavan", "Osmania Medical College", "MG Bus Station", "New Market",
            "Musarambagh", "Dilsukhnagar", "Chaitanyapuri", "Victoria Memorial", "LB Nagar"
        };

        // Corridor 2: JBS Parade Ground to MG Bus Station
        String[] corridor2 = {
            "JBS Parade Ground", "Secunderabad West", "Gandhi Hospital", "Musheerabad",
            "RTC X Roads", "Chikkadpally", "Narayanguda", "Sultan Bazar", "MG Bus Station"
        };

        // Corridor 3: Nagole to Raidurg
        String[] corridor3 = {
            "Nagole", "Uppal", "NGRI", "Habsiguda", "Mettuguda", "Secunderabad East", "Parade Grounds",
            "Paradise", "Rasoolpura", "Prakash Nagar", "Begumpet", "Ameerpet", "Madhura Nagar",
            "Yusufguda", "Road No.5 Jubilee Hills", "Jubilee Hills Check Post", "Peddamma Gudi",
            "Madhapur", "Durgam Cheruvu", "HITEC City", "Raidurg"
        };

        // Add all stations
        for (String station : corridor1) metro.addStation(station);
        for (String station : corridor2) metro.addStation(station);
        for (String station : corridor3) metro.addStation(station);

        // Add connections with example costs (distance units)
        for (int i = 0; i < corridor1.length - 1; i++) {
            metro.addConnection(corridor1[i], corridor1[i + 1], 5);
        }
        for (int i = 0; i < corridor2.length - 1; i++) {
            metro.addConnection(corridor2[i], corridor2[i + 1], 5);
        }
        for (int i = 0; i < corridor3.length - 1; i++) {
            metro.addConnection(corridor3[i], corridor3[i + 1], 5);
        }

        // Interchange stations connections (shorter cost)
        metro.addConnection("MG Bus Station", "Ameerpet", 3);
        metro.addConnection("Parade Grounds", "JBS Parade Ground", 3);

        // Menu Loop
        while (true) {
            System.out.println("\n\t\t\t\t~~LIST OF ACTIONS~~\n");
            System.out.println("1. LIST ALL THE STATIONS IN THE MAP");
            System.out.println("2. SHOW THE METRO MAP");
            System.out.println("3. GET SHORTEST DISTANCE FROM A 'SOURCE' STATION TO 'DESTINATION' STATION");
            System.out.println("4. GET SHORTEST TIME TO REACH FROM A 'SOURCE' STATION TO 'DESTINATION' STATION");
            System.out.println("5. GET SHORTEST PATH (DISTANCE WISE) TO REACH FROM A 'SOURCE' STATION TO 'DESTINATION' STATION");
            System.out.println("6. GET SHORTEST PATH (TIME WISE) TO REACH FROM A 'SOURCE' STATION TO 'DESTINATION' STATION");
            System.out.println("7. EXIT THE MENU");
            System.out.print("\nENTER YOUR CHOICE FROM THE ABOVE LIST (1 to 7): ");

            int choice;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number from 1 to 7.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("\nList of all stations in the metro:");
                    for (String station : metro.getAllStations()) {
                        System.out.println("- " + station);
                    }
                    break;

                case 2:
                    System.out.println("\nMetro Map (station -> connected stations with costs):");
                    metro.printMetroMap();
                    break;

                case 3:
                case 4:
                case 5:
                case 6:
                    System.out.print("Enter SOURCE station: ");
                    String source = sc.nextLine().trim();
                    System.out.print("Enter DESTINATION station: ");
                    String destination = sc.nextLine().trim();

                    List<String> path = metro.getShortestPath(source, destination);
                    if (path.isEmpty()) {
                        System.out.println("No path found between given stations.");
                    } else {
                        System.out.println("Shortest Path from " + source + " to " + destination + ":");
                        System.out.println(String.join(" -> ", path));
                    }
                    break;

                case 7:
                    System.out.println("Exiting the application. Thank you!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please select from 1 to 7.");
            }
        }
    }
}
