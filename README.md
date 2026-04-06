# Hyderabad Metro Navigation System

A robust **Java-based console application** designed to help users efficiently navigate the Hyderabad Metro rail network. The system models metro stations and their interconnections using a **weighted graph**, enabling accurate computation of shortest routes based on distance or travel time.

---

## Features

* Display a complete list of metro stations in the network
* Visualize station connectivity along with associated travel costs
* Compute the shortest distance between any two stations
* Estimate minimum travel time between selected stations
* Determine the optimal route based on distance
* Determine the fastest route based on time
* Interactive, menu-driven console interface for ease of use
* Input validation and basic error handling for invalid station entries

---

## How It Works

The application represents the metro network as a graph:

* **Nodes** → Metro stations
* **Edges** → Direct connections between stations
* **Weights** → Distance or time required to travel between stations

The system uses efficient shortest-path algorithms like **Dijkstra’s Algorithm** to compute optimal routes. Users can interact with the program via a structured menu to retrieve distances, travel times, and paths between any two stations.

---

## Sample Menu

1. List all stations in the metro network
2. Display the metro map
3. Find shortest distance between two stations
4. Find shortest travel time between two stations
5. Find shortest path (distance-based)
6. Find shortest path (time-based)
7. Exit

---

## Example Usage

**Shortest Distance: Madhura Nagar → Moosapet**

* Minimum Cost: 30
* Path:
  Madhura Nagar → Ameerpet → S.R. Nagar → ESI Hospital → Erragadda → Bharat Nagar → Moosapet

**Shortest Time: Raidurg → Narayanguda**

* Minimum Cost: 58
* Path:
  Raidurg → HITEC City → Durgam Cheruvu → Madhapur → Peddamma Gudi → Jubilee Hills Check Post → Road No.5 Jubilee Hills → Yusufguda → Madhura Nagar → Ameerpet → MG Bus Station → Sultan Bazar → Narayanguda

---

## Technologies Used

* **Java** (Core Programming)
* **Graph Data Structures** (Adjacency List/Matrix)
* **Dijkstra’s Algorithm** for shortest path computation
* Console-based UI for user interaction

---

## How to Run

1. Clone or download the repository
2. Open terminal/command prompt and navigate to the project directory
3. Compile the program:

   ```
   javac MetroApp.java
   ```
4. Run the application:

   ```
   java MetroApp
   ```
5. Use the on-screen menu to explore metro routes

---

## Project Highlights

* Accurate modeling of metro systems using weighted graphs
* Efficient shortest path computation using optimized algorithms
* Simple and user-friendly interface
* Modular and scalable design for future enhancements

---

## Future Enhancements

* GUI-based interface (JavaFX or Swing)
* Real-time data integration (train timings, delays)
* Fare calculation module
* Multi-line interchange optimization
* Mobile or web-based version

---

## Author

**Sonali-b23**

---
