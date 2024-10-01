Vehicle Journey Simulation
------
This project is a Java-based simulation service that models vehicle journeys between random cities. It utilizes Spring Boot for application configuration, Kafka for messaging, and various services for calculating distances, managing vehicles, and generating random routes.

Features
------
Simulate Multiple Vehicles: Create and manage multiple vehicle simulations concurrently.

Route Generation: Generate random routes between cities using external APIs.

Distance Calculation: Calculate distances and steps between coordinates.

Kafka Integration: Send vehicle data, status updates, and notifications via Kafka topics.

Asynchronous Execution: Utilize asynchronous tasks for non-blocking operations.

Status Management: Track the status of each vehicle (READY, ON_ROAD, COMPLETED, FAILED).


Architecture
------
The application is structured into several key components:

RouteSimulator: Main service that orchestrates the simulation of vehicle journeys.

VehicleManager: Handles creation and management of vehicle instances.

RandomRouteService: Provides random city pairs and generates routes between them.

DistanceCalculatorService: Calculates distances and retrieves route steps.

VehicleProducerService: Sends vehicle data and notifications to Kafka topics.

VehicleRepository: Interfaces with the database to persist vehicle states.


Prerequisites
------
Java 11 or higher

Maven 3.6+
Apache Kafka

Spring Boot 2.5+

A running instance of a database supported by Spring Data JPA (e.g., MySQL, PostgreSQL)

------------------------------
![GPS_Simulator- (1)](https://github.com/user-attachments/assets/633d7428-4680-4ad1-932a-859ff44615ef)

------------------------------
<img width="1637" alt="readme1" src="https://github.com/user-attachments/assets/c7640571-2fcd-4fe7-a6c5-c0522ddc3e2d">

------------------------------
<img width="1572" alt="readme2" src="https://github.com/user-attachments/assets/6a043cd0-6413-465a-8a32-521d5993d339">

------------------------------
<img width="1629" alt="readme3" src="https://github.com/user-attachments/assets/a85e3891-9254-4dbb-b0c1-885fc0a436f3">

------------------------------
<img width="1625" alt="readme4" src="https://github.com/user-attachments/assets/08fbc77c-4af8-4e8a-922f-37f039e90ca2">

------------------------------

