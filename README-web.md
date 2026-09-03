# Hyderabad Metro Navigation System (Java Web UI)

A browser-based UI for the Hyderabad Metro route-finder. This version is backed entirely by Java code -- no Python, no Node/React, no external libraries or build tool. `MetroServer.java` uses `com.sun.net.httpserver.HttpServer` (bundled with the JDK) to expose the core graph logic as a small JSON API, and serves a plain HTML/CSS/vanilla-JS frontend (`webapp/`).

---

## Features

* Display a complete list of metro stations in the network
* Get the shortest distance and time between any two stations
* Get the full shortest route by distance and by time, side-by-side with interchange stations flagged
* Case-insensitive station names, with "did you mean...?" suggestions on a typo
* A real Java web server built on nothing but the JDK's own `HttpServer`

---

## How It Works

The application represents the metro network as a graph:

* **Nodes** -> Metro stations
* **Edges** -> Direct connections between stations
* **Weights** -> Each edge carries *two* costs: distance (100m units) and time (6s units)

Distance and time are not proportional. A direct edge between two adjacent stations on the same line is fast per unit of distance; an interchange edge between two lines is short in distance but costs extra time for the platform walk. That's why "shortest by distance" and "shortest by time" can pick genuinely different routes -- both are computed with **Dijkstra's Algorithm**, once per metric. 

When you click "Find Route" in the browser, the webapp calls the Java API to execute the Dijkstra implementation.

---

## Technologies Used

* **Java** (Core Programming, JDK 11+) -- graph model and Dijkstra's algorithm
* **Web Server**: `com.sun.net.httpserver` (part of the JDK) -- no Spring, no external libraries
* Vanilla **HTML / CSS / JavaScript** for the web frontend

---

## How to Run

```bash
javac *.java
java MetroServer
```

Then open `http://localhost:8080/` in a browser. Pass a port number as an argument to use a different one, e.g. `java MetroServer 9090`. Pick a "From" and "To" station from the dropdowns and hit "Find Route".

---

## Running Tests

**Java (graph engine):**
```bash
javac Graph_M.java MetroData.java GraphMTest.java
java GraphMTest
```

**Java (JSON serialization):**
```bash
javac JsonUtil.java JsonUtilTest.java
java JsonUtilTest
```

**Java (web server, end-to-end):**
```bash
javac *.java
java MetroServerTest
```
Starts a real `MetroServer` on an ephemeral port and hits it over real HTTP with `java.net.http.HttpClient` (built into the JDK) -- checks `/api/stations`, `/api/route`, unknown-station handling, case-insensitive input, and that `index.html` is served.

---

## Project Structure

* `Graph_M.java`, `MetroData.java`, `GraphMTest.java` -- Core graph engine.
* `MetroServer.java` -- Pure-JDK HTTP server (`com.sun.net.httpserver`) exposing `Graph_M` as a JSON API and serving `webapp/`.
* `JsonUtil.java` -- Minimal dependency-free JSON writer.
* `JsonUtilTest.java` -- Tests for `JsonUtil`.
* `MetroServerTest.java` -- End-to-end HTTP tests for `MetroServer`.
* `webapp/index.html`, `webapp/style.css`, `webapp/app.js` -- The frontend: dropdowns, tabs, route cards with interchange badges.

---

## Author

**Sonali-b23**
