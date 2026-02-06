# Hyderabad Metro Navigation System

A robust Java-based console application designed to help users navigate the Hyderabad Metro rail network efficiently. The system models metro stations and their connections using a weighted graph, enabling calculation of shortest routes based on distance or travel time.

## Features

- List all metro stations in the Hyderabad Metro map.
- Display the metro map showing station connectivity with associated costs.
- Calculate the shortest distance between any two stations.
- Calculate the shortest travel time between any two stations.
- Find and display the shortest path based on distance.
- Find and display the shortest path based on time.

## How it Works

The system represents the Hyderabad Metro network as a graph where:

- Nodes correspond to metro stations.
- Edges correspond to direct connections between stations.
- Weights on edges represent the cost or time to travel between stations.

Users interact through a menu-driven console interface to query the shortest distance, time, or route between specified source and destination stations.

## Sample Menu
1. List all the stations in the map
2. Show the metro map
3. Get shortest distance from a source station to a destination station
4. Get shortest time to reach from a source station to a destination station
5. Get shortest path (distance-wise) to reach from a source station to a destination station
6. Get shortest path (time-wise) to reach from a source station to a destination station
7. Exit the menu

## Example Usage

**Finding shortest distance from Madhura Nagar to Moosapet**
Minimum Cost: 30
Shortest Path: Madhura Nagar -> Ameerpet -> S.R. Nagar -> ESI Hospital -> Erragadda -> Bharat Nagar -> Moosapet

**Finding shortest time from Raidurg to Narayanguda**
Minimum Cost: 58
Shortest Path: Raidurg -> HITEC City -> Durgam Cheruvu -> Madhapur -> Peddamma Gudi -> Jubilee Hills Check Post -> Road No.5 Jubilee Hills -> Yusufguda -> Madhura Nagar -> Ameerpet -> MG Bus Station -> Sultan Bazar -> Narayanguda

## Technologies Used

- Java programming language  
- Graph data structures for modeling stations and routes  
- Console-based user interface

## How to Run

1. Clone or download the project repository.  
2. Navigate to the project directory in the terminal or command prompt.  
3. Compile the Java source file using the command:  
   javac MetroApp.java
4. Run the application using the command:
   java MetroApp
5. Follow the on-screen menu instructions to interact with the metro navigation system.

## Project Highlights

* Uses a weighted graph to model Hyderabad Metro stations and routes accurately.
* Implements shortest path algorithms (such as Dijkstra’s algorithm) for route calculations.
* Provides user-friendly text interface for metro route exploration.
* Includes comprehensive station connectivity with weighted costs representing distance/time.

## Author
Sonali-b23
