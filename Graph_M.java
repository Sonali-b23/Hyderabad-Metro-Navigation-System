import java.util.*;

public class Graph_M {
    // Graph represented as adjacency list: station -> list of edges
    private HashMap<String, ArrayList<Edge>> metroMap;

    public Graph_M() {
        metroMap = new HashMap<>();
    }

    // Add station to the map
    public void addStation(String station) {
        metroMap.putIfAbsent(station, new ArrayList<>());
    }

    // Add bidirectional connection with cost
    public void addConnection(String from, String to, int cost) {
        metroMap.get(from).add(new Edge(to, cost));
        metroMap.get(to).add(new Edge(from, cost));
    }

    // Get shortest path using Dijkstra's algorithm
    public List<String> getShortestPath(String start, String end) {
        if (!metroMap.containsKey(start) || !metroMap.containsKey(end)) {
            System.out.println("Either start or end station does not exist.");
            return Collections.emptyList();
        }

        HashMap<String, Integer> distances = new HashMap<>();
        HashMap<String, String> previous = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.cost));

        for (String station : metroMap.keySet()) {
            distances.put(station, Integer.MAX_VALUE);
        }
        distances.put(start, 0);
        pq.add(new Node(start, 0));

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.station.equals(end)) break;  // early exit if destination reached

            if (current.cost > distances.get(current.station)) continue;

            for (Edge edge : metroMap.get(current.station)) {
                int newDist = distances.get(current.station) + edge.cost;
                if (newDist < distances.get(edge.to)) {
                    distances.put(edge.to, newDist);
                    previous.put(edge.to, current.station);
                    pq.add(new Node(edge.to, newDist));
                }
            }
        }

        // Reconstruct path
        LinkedList<String> path = new LinkedList<>();
        String at = end;
        while (at != null) {
            path.addFirst(at);
            at = previous.get(at);
        }

        if (!path.isEmpty() && path.getFirst().equals(start)) {
            System.out.println("Minimum Cost: " + distances.get(end));
            return path;
        } else {
            System.out.println("No path found from " + start + " to " + end);
            return Collections.emptyList();
        }
    }

    // Return all stations in the metro map
    public Set<String> getAllStations() {
        return metroMap.keySet();
    }

    // Print the metro map in text form
    public void printMetroMap() {
        for (String station : metroMap.keySet()) {
            System.out.print(station + " -> ");
            for (Edge e : metroMap.get(station)) {
                System.out.print(e.to + " (" + e.cost + ")  ");
            }
            System.out.println();
        }
    }

    // Inner class for edges
    private static class Edge {
        String to;
        int cost;

        public Edge(String to, int cost) {
            this.to = to;
            this.cost = cost;
        }
    }

    // Inner class for priority queue nodes
    private static class Node {
        String station;
        int cost;

        public Node(String station, int cost) {
            this.station = station;
            this.cost = cost;
        }
    }
}
