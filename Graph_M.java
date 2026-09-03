import java.util.*;

/**
 * Weighted graph model of the metro network. Each edge carries TWO
 * independent weights -- distance (km x10, i.e. one unit = 100m) and time
 * (minutes x10, i.e. one unit = 6 seconds) -- so "shortest distance" and
 * "shortest time" can genuinely differ, e.g. an interchange edge is short in
 * distance but costs extra time for the platform walk.
 *
 * Station lookup is case-insensitive, and a lookup that matches nothing
 * suggests the closest known station names instead of just failing.
 */
public class Graph_M {

    /** Which edge weight to optimize for. */
    public enum Metric {
        DISTANCE,
        TIME
    }

    /** Path + total cost for a single shortest-path query. */
    public static class PathResult {
        public final List<String> path;
        public final int totalCost;

        PathResult(List<String> path, int totalCost) {
            this.path = path;
            this.totalCost = totalCost;
        }

        public boolean found() {
            return !path.isEmpty();
        }
    }

    private final HashMap<String, ArrayList<Edge>> metroMap = new HashMap<>();
    // Case-insensitive lookup: lowercase input -> canonical stored station name.
    private final HashMap<String, String> canonicalNameByLowercase = new HashMap<>();

    public Graph_M() {
    }

    public void addStation(String station) {
        metroMap.putIfAbsent(station, new ArrayList<>());
        canonicalNameByLowercase.putIfAbsent(station.toLowerCase(Locale.ROOT), station);
    }

    /**
     * Add a bidirectional connection with independent distance and time costs.
     *
     * @param distanceCost distance units (1 unit = 100m)
     * @param timeCost     time units (1 unit = 6 seconds)
     */
    public void addConnection(String from, String to, int distanceCost, int timeCost) {
        if (!metroMap.containsKey(from)) {
            throw new IllegalArgumentException("Unknown station '" + from + "' -- call addStation() first.");
        }
        if (!metroMap.containsKey(to)) {
            throw new IllegalArgumentException("Unknown station '" + to + "' -- call addStation() first.");
        }
        metroMap.get(from).add(new Edge(to, distanceCost, timeCost));
        metroMap.get(to).add(new Edge(from, distanceCost, timeCost));
    }

    /**
     * Resolve user-typed input to the canonical stored station name,
     * case-insensitively. Returns null if there's no exact (case-insensitive)
     * match -- callers should fall back to {@link #suggestStations}.
     */
    public String resolveStationName(String input) {
        if (input == null) {
            return null;
        }
        return canonicalNameByLowercase.get(input.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Suggest up to {@code limit} known station names closest to the given
     * (presumably mistyped) input, using Levenshtein edit distance. Also
     * matches names that simply *contain* the input as a substring (handles
     * a user typing a partial name), ranked ahead of pure edit-distance
     * matches.
     */
    public List<String> suggestStations(String input, int limit) {
        if (input == null || input.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String needle = input.trim().toLowerCase(Locale.ROOT);

        List<String> substringMatches = new ArrayList<>();
        List<Map.Entry<String, Integer>> distanceRanked = new ArrayList<>();

        for (String canonical : metroMap.keySet()) {
            String lower = canonical.toLowerCase(Locale.ROOT);
            if (lower.contains(needle)) {
                substringMatches.add(canonical);
            } else {
                distanceRanked.add(Map.entry(canonical, levenshtein(needle, lower)));
            }
        }

        substringMatches.sort(Comparator.naturalOrder());
        distanceRanked.sort(Comparator.comparingInt(Map.Entry::getValue));

        List<String> suggestions = new ArrayList<>(substringMatches);
        for (Map.Entry<String, Integer> entry : distanceRanked) {
            if (suggestions.size() >= limit) {
                break;
            }
            suggestions.add(entry.getKey());
        }

        if (suggestions.size() > limit) {
            return new ArrayList<>(suggestions.subList(0, limit));
        }
        return suggestions;
    }

    private static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[a.length()][b.length()];
    }

    /** Shortest path optimizing for real distance. */
    public PathResult getShortestPathByDistance(String start, String end) {
        return dijkstra(start, end, Metric.DISTANCE);
    }

    /** Shortest path optimizing for real travel time. */
    public PathResult getShortestPathByTime(String start, String end) {
        return dijkstra(start, end, Metric.TIME);
    }

    private PathResult dijkstra(String start, String end, Metric metric) {
        if (!metroMap.containsKey(start) || !metroMap.containsKey(end)) {
            return new PathResult(Collections.emptyList(), -1);
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

            if (current.station.equals(end)) break; // early exit once destination is settled

            if (current.cost > distances.get(current.station)) continue; // stale queue entry

            for (Edge edge : metroMap.get(current.station)) {
                int weight = metric == Metric.DISTANCE ? edge.distanceCost : edge.timeCost;
                int newDist = distances.get(current.station) + weight;
                if (newDist < distances.get(edge.to)) {
                    distances.put(edge.to, newDist);
                    previous.put(edge.to, current.station);
                    pq.add(new Node(edge.to, newDist));
                }
            }
        }

        LinkedList<String> path = new LinkedList<>();
        String at = end;
        while (at != null) {
            path.addFirst(at);
            at = previous.get(at);
        }

        if (!path.isEmpty() && path.getFirst().equals(start)) {
            return new PathResult(path, distances.get(end));
        }
        return new PathResult(Collections.emptyList(), -1);
    }

    public Set<String> getAllStations() {
        return metroMap.keySet();
    }

    public void printMetroMap() {
        for (String station : metroMap.keySet()) {
            System.out.print(station + " -> ");
            for (Edge e : metroMap.get(station)) {
                System.out.print(e.to + " (dist:" + e.distanceCost + ", time:" + e.timeCost + ")  ");
            }
            System.out.println();
        }
    }

    private static class Edge {
        final String to;
        final int distanceCost;
        final int timeCost;

        Edge(String to, int distanceCost, int timeCost) {
            this.to = to;
            this.distanceCost = distanceCost;
            this.timeCost = timeCost;
        }
    }

    private static class Node {
        final String station;
        final int cost;

        Node(String station, int cost) {
            this.station = station;
            this.cost = cost;
        }
    }
}
