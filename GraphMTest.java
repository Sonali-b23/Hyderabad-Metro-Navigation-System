import java.util.*;

/**
 * Lightweight, dependency-free test harness for Graph_M (and MetroData's
 * graph-building). No JUnit/build tool is used anywhere else in this
 * project, so this avoids introducing one just for tests: compile and run
 * like any other class --
 *
 *   javac Graph_M.java MetroData.java GraphMTest.java
 *   java GraphMTest
 *
 * Exits with a non-zero status if any test fails, so it can be used as a CI
 * gate as-is.
 */
public class GraphMTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testDirectConnectionFound();
        testNoPathBetweenDisconnectedStations();
        testUnknownStationReturnsNotFound();
        testDistanceAndTimeCanDiffer();
        testShortestPathIsSymmetric();
        testCaseInsensitiveResolution();
        testUnresolvedNameReturnsNull();
        testSuggestionsRankExactSubstringFirst();
        testAddConnectionToUnknownStationThrows();
        testFullNetworkBuildsAndAllStationsReachable();

        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testDirectConnectionFound() {
        Graph_M g = new Graph_M();
        g.addStation("A");
        g.addStation("B");
        g.addConnection("A", "B", 10, 20);

        Graph_M.PathResult result = g.getShortestPathByDistance("A", "B");
        check("direct connection found", result.found() && result.totalCost == 10
                && result.path.equals(List.of("A", "B")));
    }

    private static void testNoPathBetweenDisconnectedStations() {
        Graph_M g = new Graph_M();
        g.addStation("A");
        g.addStation("B"); // no connection added

        Graph_M.PathResult result = g.getShortestPathByDistance("A", "B");
        check("disconnected stations report no path", !result.found() && result.path.isEmpty());
    }

    private static void testUnknownStationReturnsNotFound() {
        Graph_M g = new Graph_M();
        g.addStation("A");

        Graph_M.PathResult result = g.getShortestPathByDistance("A", "DoesNotExist");
        check("unknown destination reports no path", !result.found());
    }

    private static void testDistanceAndTimeCanDiffer() {
        // A->B is long in distance but a fast express edge; A->C->B is short
        // in distance but slow (e.g. an interchange walk) -- shortest-by-
        // distance and shortest-by-time should pick different routes.
        Graph_M g = new Graph_M();
        g.addStation("A");
        g.addStation("B");
        g.addStation("C");
        g.addConnection("A", "B", 100, 5);   // direct: far but fast
        g.addConnection("A", "C", 1, 50);    // short hop, slow
        g.addConnection("C", "B", 1, 50);    // short hop, slow

        Graph_M.PathResult byDistance = g.getShortestPathByDistance("A", "B");
        Graph_M.PathResult byTime = g.getShortestPathByTime("A", "B");

        check("shortest-by-distance picks the A-C-B route",
                byDistance.path.equals(List.of("A", "C", "B")) && byDistance.totalCost == 2);
        check("shortest-by-time picks the direct A-B route",
                byTime.path.equals(List.of("A", "B")) && byTime.totalCost == 5);
    }

    private static void testShortestPathIsSymmetric() {
        Graph_M metro = MetroData.buildGraph();
        Graph_M.PathResult forward = metro.getShortestPathByDistance("Raidurg", "Narayanguda");
        Graph_M.PathResult backward = metro.getShortestPathByDistance("Narayanguda", "Raidurg");

        check("shortest distance is symmetric", forward.totalCost == backward.totalCost);
    }

    private static void testCaseInsensitiveResolution() {
        Graph_M metro = MetroData.buildGraph();
        check("lowercase resolves to canonical name",
                "Raidurg".equals(metro.resolveStationName("raidurg")));
        check("uppercase resolves to canonical name",
                "Ameerpet".equals(metro.resolveStationName("AMEERPET")));
        check("padded whitespace is trimmed",
                "HITEC City".equals(metro.resolveStationName("  hitec city  ")));
    }

    private static void testUnresolvedNameReturnsNull() {
        Graph_M metro = MetroData.buildGraph();
        check("unknown name resolves to null", metro.resolveStationName("Not A Real Station") == null);
    }

    private static void testSuggestionsRankExactSubstringFirst() {
        Graph_M metro = MetroData.buildGraph();
        List<String> suggestions = metro.suggestStations("ameer", 3);
        check("substring match is suggested", !suggestions.isEmpty() && suggestions.get(0).equals("Ameerpet"));
    }

    private static void testAddConnectionToUnknownStationThrows() {
        Graph_M g = new Graph_M();
        g.addStation("A");
        boolean threw = false;
        try {
            g.addConnection("A", "NeverAdded", 1, 1);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        check("connecting to an unregistered station throws", threw);
    }

    private static void testFullNetworkBuildsAndAllStationsReachable() {
        Graph_M metro = MetroData.buildGraph();

        Set<String> expectedUnion = new HashSet<>();
        expectedUnion.addAll(List.of(MetroData.CORRIDOR_1));
        expectedUnion.addAll(List.of(MetroData.CORRIDOR_2));
        expectedUnion.addAll(List.of(MetroData.CORRIDOR_3));

        check("all corridor stations are present in the graph",
                metro.getAllStations().size() == expectedUnion.size()
                        && metro.getAllStations().equals(expectedUnion));

        // Every station should be reachable from Miyapur (one connected network).
        boolean allReachable = true;
        for (String station : metro.getAllStations()) {
            if (!metro.getShortestPathByDistance("Miyapur", station).found()) {
                allReachable = false;
                break;
            }
        }
        check("every station is reachable from Miyapur", allReachable);
    }

    private static void check(String description, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  [PASS] " + description);
        } else {
            failed++;
            System.out.println("  [FAIL] " + description);
        }
    }
}
