import java.util.*;

public class MetroApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Graph_M metro = MetroData.buildGraph();

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
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number from 1 to 7.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.println("\nList of all stations in the metro:");
                    List<String> sorted = new ArrayList<>(metro.getAllStations());
                    Collections.sort(sorted);
                    for (String station : sorted) {
                        System.out.println("- " + station);
                    }
                    break;

                case 2:
                    System.out.println("\nMetro Map (station -> connected stations, distance in 100m units, time in 6s units):");
                    metro.printMetroMap();
                    break;

                case 3: {
                    StationPair pair = readStationPair(sc, metro);
                    if (pair == null) break;
                    Graph_M.PathResult result = metro.getShortestPathByDistance(pair.source, pair.destination);
                    if (!result.found()) {
                        System.out.println("No path found between given stations.");
                    } else {
                        System.out.printf("Shortest distance from %s to %s: %.1f km%n",
                                pair.source, pair.destination, result.totalCost / 10.0);
                    }
                    break;
                }

                case 4: {
                    StationPair pair = readStationPair(sc, metro);
                    if (pair == null) break;
                    Graph_M.PathResult result = metro.getShortestPathByTime(pair.source, pair.destination);
                    if (!result.found()) {
                        System.out.println("No path found between given stations.");
                    } else {
                        System.out.printf("Shortest time from %s to %s: %.1f min%n",
                                pair.source, pair.destination, result.totalCost / 10.0);
                    }
                    break;
                }

                case 5: {
                    StationPair pair = readStationPair(sc, metro);
                    if (pair == null) break;
                    Graph_M.PathResult result = metro.getShortestPathByDistance(pair.source, pair.destination);
                    printPathResult(pair, result, "distance", "%.1f km", 10.0);
                    break;
                }

                case 6: {
                    StationPair pair = readStationPair(sc, metro);
                    if (pair == null) break;
                    Graph_M.PathResult result = metro.getShortestPathByTime(pair.source, pair.destination);
                    printPathResult(pair, result, "time", "%.1f min", 10.0);
                    break;
                }

                case 7:
                    System.out.println("Exiting the application. Thank you!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice. Please select from 1 to 7.");
            }
        }
    }

    private static void printPathResult(StationPair pair, Graph_M.PathResult result, String metricName,
                                         String costFormat, double divisor) {
        if (!result.found()) {
            System.out.println("No path found between given stations.");
            return;
        }
        System.out.println("Shortest path (" + metricName + "-wise) from " + pair.source + " to " + pair.destination + ":");
        System.out.println(String.join(" -> ", result.path));
        System.out.printf("Total " + metricName + ": " + costFormat + "%n", result.totalCost / divisor);
    }

    /**
     * Prompts for source and destination stations, resolving each
     * case-insensitively. If either doesn't match a known station, prints
     * up to 3 close suggestions and returns null so the caller aborts the
     * operation instead of proceeding with an invalid name.
     */
    private static StationPair readStationPair(Scanner sc, Graph_M metro) {
        System.out.print("Enter SOURCE station: ");
        String sourceInput = sc.nextLine().trim();
        String source = metro.resolveStationName(sourceInput);
        if (source == null) {
            printUnknownStation(metro, sourceInput);
            return null;
        }

        System.out.print("Enter DESTINATION station: ");
        String destInput = sc.nextLine().trim();
        String destination = metro.resolveStationName(destInput);
        if (destination == null) {
            printUnknownStation(metro, destInput);
            return null;
        }

        return new StationPair(source, destination);
    }

    private static void printUnknownStation(Graph_M metro, String input) {
        System.out.println("Unknown station: \"" + input + "\"");
        List<String> suggestions = metro.suggestStations(input, 3);
        if (!suggestions.isEmpty()) {
            System.out.println("Did you mean: " + String.join(", ", suggestions) + "?");
        }
    }

    private static class StationPair {
        final String source;
        final String destination;

        StationPair(String source, String destination) {
            this.source = source;
            this.destination = destination;
        }
    }
}
