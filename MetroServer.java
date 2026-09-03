import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure-JDK web server for the Hyderabad Metro navigator -- no external
 * dependencies, no build tool. Uses com.sun.net.httpserver.HttpServer
 * (bundled with the JDK) to expose Graph_M / MetroData over a tiny JSON API,
 * and serves the static frontend (webapp/) that calls it.
 *
 * The route-finding itself runs in the same Graph_M.java used by the console
 * app (MetroApp.java) -- this is a second frontend for the same Java core,
 * not a reimplementation.
 */
public class MetroServer {

    private static final Path DEFAULT_WEB_ROOT = Paths.get("webapp");

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        Graph_M metro = MetroData.buildGraph();

        HttpServer server = createServer(metro, port, DEFAULT_WEB_ROOT);
        server.start();
        System.out.println("Hyderabad Metro web server running at http://localhost:" + port + "/");
    }

    /** Package-visible so tests can start a server on an ephemeral port (0) without touching main(). */
    static HttpServer createServer(Graph_M metro, int port, Path webRoot) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/stations", new StationsHandler(metro));
        server.createContext("/api/route", new RouteHandler(metro));
        server.createContext("/", new StaticFileHandler(webRoot));
        server.setExecutor(null); // default sequential executor -- fine for a small demo app
        return server;
    }

    // ---------------------------------------------------------------------
    // /api/stations
    // ---------------------------------------------------------------------

    static class StationsHandler implements HttpHandler {
        private final Graph_M metro;

        StationsHandler(Graph_M metro) {
            this.metro = metro;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("Method not allowed"));
                return;
            }
            List<String> stations = new ArrayList<>(metro.getAllStations());
            Collections.sort(stations);
            sendJson(exchange, 200, stations);
        }
    }

    // ---------------------------------------------------------------------
    // /api/route?from=...&to=...&metric=distance|time
    // ---------------------------------------------------------------------

    static class RouteHandler implements HttpHandler {
        private final Graph_M metro;

        RouteHandler(Graph_M metro) {
            this.metro = metro;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, error("Method not allowed"));
                return;
            }

            Map<String, String> params = parseQuery(exchange.getRequestURI());
            String fromInput = params.get("from");
            String toInput = params.get("to");
            String metric = params.getOrDefault("metric", "distance");

            if (fromInput == null || toInput == null) {
                sendJson(exchange, 400, error("Both 'from' and 'to' query parameters are required."));
                return;
            }

            String from = metro.resolveStationName(fromInput);
            if (from == null) {
                sendJson(exchange, 400, unknownStation(fromInput));
                return;
            }

            String to = metro.resolveStationName(toInput);
            if (to == null) {
                sendJson(exchange, 400, unknownStation(toInput));
                return;
            }

            boolean byTime = "time".equalsIgnoreCase(metric);
            Graph_M.PathResult result = byTime
                    ? metro.getShortestPathByTime(from, to)
                    : metro.getShortestPathByDistance(from, to);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("found", result.found());
            response.put("from", from);
            response.put("to", to);
            response.put("metric", byTime ? "time" : "distance");
            if (result.found()) {
                response.put("path", result.path);
                response.put("totalCost", result.totalCost / 10.0);
                response.put("unit", byTime ? "min" : "km");
            }
            sendJson(exchange, 200, response);
        }

        private Map<String, Object> unknownStation(String input) {
            Map<String, Object> body = error("Unknown station: \"" + input + "\"");
            body.put("suggestions", metro.suggestStations(input, 3));
            return body;
        }
    }

    // ---------------------------------------------------------------------
    // Static files (the webapp/ frontend)
    // ---------------------------------------------------------------------

    static class StaticFileHandler implements HttpHandler {
        private final Path webRoot;

        StaticFileHandler(Path webRoot) {
            this.webRoot = webRoot.toAbsolutePath().normalize();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = exchange.getRequestURI().getPath();
            if (requestPath.equals("/")) {
                requestPath = "/index.html";
            }

            // Resolve against webRoot and refuse anything that escapes it
            // (defends against a path-traversal request like /../MetroServer.java).
            Path resolved = webRoot.resolve("." + requestPath).normalize();
            if (!resolved.startsWith(webRoot) || !Files.exists(resolved) || Files.isDirectory(resolved)) {
                sendPlainText(exchange, 404, "Not found");
                return;
            }

            byte[] bytes = Files.readAllBytes(resolved);
            exchange.getResponseHeaders().set("Content-Type", contentTypeFor(resolved));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private String contentTypeFor(Path path) {
            String name = path.getFileName().toString();
            if (name.endsWith(".html")) return "text/html; charset=utf-8";
            if (name.endsWith(".css")) return "text/css; charset=utf-8";
            if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (name.endsWith(".json")) return "application/json; charset=utf-8";
            return "application/octet-stream";
        }
    }

    // ---------------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------------

    private static Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        return body;
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            String key = eq >= 0 ? pair.substring(0, eq) : pair;
            String value = eq >= 0 ? pair.substring(eq + 1) : "";
            params.put(
                    URLDecoder.decode(key, StandardCharsets.UTF_8),
                    URLDecoder.decode(value, StandardCharsets.UTF_8)
            );
        }
        return params;
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendPlainText(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
