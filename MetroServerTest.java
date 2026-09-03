import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;

/**
 * Integration tests for MetroServer: starts a real server on an ephemeral
 * port and hits it with java.net.http.HttpClient (built into the JDK since
 * Java 11 -- no extra dependency). This is deliberately an end-to-end check
 * of the whole stack (Graph_M -> MetroServer -> JSON over HTTP), which is
 * exactly the layer where the original "distance and time return identical
 * results" bug could resurface without anyone noticing.
 */
public class MetroServerTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        Graph_M metro = MetroData.buildGraph();
        // Port 0 = let the OS pick a free ephemeral port.
        HttpServer server = MetroServer.createServer(metro, 0, Paths.get("webapp"));
        server.start();
        int port = server.getAddress().getPort();
        String base = "http://localhost:" + port;
        HttpClient client = HttpClient.newHttpClient();

        try {
            testStationsEndpointReturnsSortedList(client, base);
            testRouteEndpointReturnsDistanceAndTimeThatDiffer(client, base);
            testRouteEndpointRejectsUnknownStation(client, base);
            testRouteEndpointHandlesCaseInsensitiveInput(client, base);
            testIndexHtmlIsServed(client, base);
        } finally {
            server.stop(0);
        }

        System.out.println("\n" + passed + " passed, " + failed + " failed.");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testStationsEndpointReturnsSortedList(HttpClient client, String base) throws Exception {
        HttpResponse<String> res = get(client, base + "/api/stations");
        check("GET /api/stations returns 200", res.statusCode() == 200);
        check("stations list contains Ameerpet", res.body().contains("\"Ameerpet\""));
        check("stations list looks sorted (Ameerpet before Raidurg)",
                res.body().indexOf("Ameerpet") < res.body().indexOf("Raidurg"));
    }

    private static void testRouteEndpointReturnsDistanceAndTimeThatDiffer(HttpClient client, String base) throws Exception {
        HttpResponse<String> distanceRes = get(client, base + "/api/route?from=Raidurg&to=Narayanguda&metric=distance");
        HttpResponse<String> timeRes = get(client, base + "/api/route?from=Raidurg&to=Narayanguda&metric=time");

        check("distance route returns 200", distanceRes.statusCode() == 200);
        check("time route returns 200", timeRes.statusCode() == 200);

        // Regression guard: this is exactly the bug that shipped in the
        // original Java console app (options 3/4/5/6 all identical).
        check("distance totalCost is 13.5", distanceRes.body().contains("\"totalCost\":13.5"));
        check("time totalCost is 33.5", timeRes.body().contains("\"totalCost\":33.5"));
        check("distance response is not identical to time response", !distanceRes.body().equals(timeRes.body()));
    }

    private static void testRouteEndpointRejectsUnknownStation(HttpClient client, String base) throws Exception {
        HttpResponse<String> res = get(client, base + "/api/route?from=Raidrug&to=Narayanguda&metric=distance");
        check("unknown station returns 400", res.statusCode() == 400);
        check("unknown station response includes a suggestion", res.body().contains("Raidurg"));
    }

    private static void testRouteEndpointHandlesCaseInsensitiveInput(HttpClient client, String base) throws Exception {
        HttpResponse<String> res = get(client, base + "/api/route?from=raidurg&to=NARAYANGUDA&metric=distance");
        check("lowercase/uppercase input resolves and returns 200", res.statusCode() == 200);
        check("resolved response reports canonical casing", res.body().contains("\"from\":\"Raidurg\""));
    }

    private static void testIndexHtmlIsServed(HttpClient client, String base) throws Exception {
        HttpResponse<String> res = get(client, base + "/");
        check("GET / returns 200", res.statusCode() == 200);
        check("index.html contains the app title", res.body().contains("Hyderabad Metro Navigator"));
    }

    private static HttpResponse<String> get(HttpClient client, String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
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
