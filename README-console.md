# Hyderabad Metro Navigation System (Console App)

A metro route-finder for the Hyderabad Metro rail network. This version provides a command-line interface (console menu) for querying routes. The system models metro stations and connections as a **weighted graph** with two independent weights per connection -- real distance and real travel time -- so shortest-by-distance and shortest-by-time routes can genuinely differ.

---

## Features

* Display a complete list of metro stations in the network
* Visualize station connectivity along with distance and time costs
* Get the shortest **distance** between any two stations (a number, in km)
* Get the shortest **time** between any two stations (a number, in minutes)
* Get the full shortest **route** by distance
* Get the full shortest **route** by time
* Case-insensitive station names, with "did you mean...?" suggestions on a typo

---

## How It Works

The application represents the metro network as a graph:

* **Nodes** -> Metro stations
* **Edges** -> Direct connections between stations
* **Weights** -> Each edge carries *two* costs: distance (100m units) and time (6s units)

Distance and time are not proportional. A direct edge between two adjacent stations on the same line is fast per unit of distance; an interchange edge between two lines is short in distance but costs extra time for the platform walk. That's why "shortest by distance" and "shortest by time" can pick genuinely different routes -- both are computed with **Dijkstra's Algorithm**, once per metric.

---

## Sample Menu

1. List all stations in the metro network
2. Display the metro map
3. Find shortest distance between two stations (returns a number, in km)
4. Find shortest travel time between two stations (returns a number, in minutes)
5. Find shortest route by distance (returns the full station-by-station path)
6. Find shortest route by time (returns the full station-by-station path)
7. Exit

---

## Example Usage

**Raidurg -> Narayanguda**

* Shortest distance: **13.5 km**
* Shortest time: **33.5 min**
* Shortest route (distance-wise) and shortest route (time-wise) happen to follow the same station sequence for this particular pair (through the Ameerpet and MG Bus Station interchanges), but the reported *cost* differs because distance and time are independently modeled -- for other station pairs the routes themselves can diverge too.

---

## Technologies Used

* **Java** (Core Programming, JDK 11+)
* **Graph Data Structures** (adjacency list, dual-weighted edges)
* **Dijkstra's Algorithm** for shortest path computation

---

## How to Run

```bash
javac Graph_M.java MetroData.java MetroApp.java
java MetroApp
```

---

## Running Tests

```bash
javac Graph_M.java MetroData.java GraphMTest.java
java GraphMTest
```
Covers direct connections, unreachable stations, distance vs. time genuinely producing different routes, symmetry, case-insensitive lookup, and the typo-suggestion feature.

---

## Project Structure

* `Graph_M.java` -- Graph model: dual-weighted (distance + time) adjacency list, Dijkstra's algorithm.
* `MetroData.java` -- Station and corridor data (the three metro lines) and the logic that builds the graph from them.
* `GraphMTest.java` -- Dependency-free test suite for `Graph_M` / `MetroData`.
* `MetroApp.java` -- Interactive console menu.

---

## Author

**Sonali-b23**
