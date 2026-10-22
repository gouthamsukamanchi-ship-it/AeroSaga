AeroSaga — Drone Simulator
AeroSaga is a Java-based drone mission simulator built with Spring Boot, Maven, MySQL, and Temporal. The current Temporal prototype demonstrates a mission workflow with three activities—takeoff, travel, and return home—and automatic retries when the simulated travel activity fails.
Current prototype note: The Temporal activity implementation shown in this version simulates drone operations in memory. It does not yet represent real drone control or automatically persist the simulated activity results to the existing MySQL mission records.

Features
- Spring Boot backend with Maven.
- MySQL database integration through Spring Data JPA and the MySQL JDBC driver.
- Temporal workflow named DroneMissionWorkflow.
- Mission activity steps:
  1. takeOff(missionId)
  2. travel(missionId)
  3. returnHome(missionId)
- Configured activity timeout and retry policy.
- A deliberate first-attempt travel failure to demonstrate Temporal retries.
- A worker that listens on the DRONE_TASK_QUEUE task queue.
- A starter program that launches a mission workflow.
Technology stack
- Java 25 (as configured in the supplied pom.xml; use the JDK configured for your project)
- Spring Boot (the supplied POM currently specifies 4.2.0-SNAPSHOT; use a stable release if that snapshot cannot be resolved)
- Maven
- MySQL
- Spring Data JPA
- Temporal Java SDK 1.38.0
Project structure
Keep the Temporal files in the same package to avoid package/import mismatches:
src/
└── main/
    ├── java/
    │   └── com/
    │       └── aerosaga/
    │           └── dronesimulator/
    │               └── workflow/
    │                   ├── DroneMissionActivities.java
    │                   ├── DroneMissionActivitiesImpl.java
    │                   ├── DroneMissionWorkflow.java
    │                   ├── DroneMissionWorkflowImpl.java
    │                   ├── DroneWorker.java
    │                   └── DroneMissionStarter.java
    └── resources/
        └── application.properties
Each of these six Java files should declare:
package com.aerosaga.dronesimulator.workflow;
The folder path and package declaration must match exactly. Java package names are case-sensitive.
Prerequisites
Install or configure:
- A JDK compatible with the java.version value in pom.xml.
- Maven, or use the project's Maven Wrapper (mvnw.cmd).
- MySQL Server and a database configured for AeroSaga if you are running the Spring Boot backend.
- Temporal CLI for running the local Temporal development server.
Verify Java and Maven in Command Prompt or PowerShell:
java -version
mvn -version
If the project has a Maven Wrapper, you can use mvnw.cmd instead of mvn.
Configure Maven
The supplied pom.xml includes the Temporal SDK dependency:
<dependency>
    <groupId>io.temporal</groupId>
    <artifactId>temporal-sdk</artifactId>
    <version>1.38.0</version>
</dependency>
Do not add the same dependency twice. If Maven cannot resolve the configured Spring Boot parent version 4.2.0-SNAPSHOT, change it to a stable Spring Boot version available from your configured repositories and compatible with the project.

Build the project from its root directory:
mvnw.cmd clean compile
Or, if Maven is installed globally:
mvn clean compile
In Eclipse, you can also right-click the project and select Maven → Update Project.
Start the local Temporal server
The Java programs use WorkflowServiceStubs.newLocalServiceStubs(), which connects to a local Temporal service at 127.0.0.1:7233 by default.

After installing the Temporal CLI, open a terminal and run:
temporal server start-dev
If temporal is not recognized, either add the directory containing temporal.exe to your Windows PATH and reopen the terminal, or run the executable by its full path. For example, if it is stored in C:\Temporal:
C:\Temporal\temporal.exe --version
C:\Temporal\temporal.exe server start-dev
Keep this terminal open while testing. The Temporal Web UI is normally available at:

http://localhost:8233

To check whether the service port is listening, open another Command Prompt:
netstat -ano | findstr :7233
A connection-refused error to 127.0.0.1:7233 means the local Temporal service is not accepting connections at that address/port. Start the server before starting the worker or starter.
Run a mission
Use separate terminals. Run the programs in this order:
1. Terminal 1 — Temporal server
   temporal server start-dev
2. Terminal 2 — Worker
   Run DroneWorker.java from Eclipse, or use your project's configured Java run command. The worker registers DroneMissionWorkflowImpl and DroneMissionActivitiesImpl on DRONE_TASK_QUEUE.
   Expected startup message:
   AeroSaga Drone Worker Started
   Task Queue: DRONE_TASK_QUEUE
   Waiting for missions...
3. Terminal 3 — Starter
   Run DroneMissionStarter.java. The current example starts MISSION-001.
The worker and starter must use the same task queue: DRONE_TASK_QUEUE.
Expected retry demonstration
The current DroneMissionActivitiesImpl deliberately throws a RuntimeException on the first call to travel(missionId). The workflow's retry policy allows up to three attempts, with an initial retry interval of two seconds and a backoff coefficient of 2.0.

With a fresh worker and the current in-memory counter, the expected sequence is:
1. Takeoff succeeds.
2. The first travel attempt fails deliberately.
3. Temporal retries travel; the next attempt succeeds.
4. Return home succeeds.
5. The workflow returns a completion message similar to:
   Mission result: Mission completed: MISSION-001
The maximum-attempt setting includes the initial attempt. This demonstration depends on the in-memory attempt counter; restarting the worker resets that counter. Do not use this counter as production retry tracking.
MySQL and Spring Boot
The Temporal example above is a standalone Java worker/starter demonstration. Its current activity implementation uses console output and an in-memory map. It does not automatically update mission status, telemetry, or workflow-signal rows in MySQL.

The existing Spring Boot database configuration should remain in src/main/resources/application.properties, and database credentials should be kept out of source control. Before connecting Temporal activities to the application's repositories or services, configure the worker to obtain those services through Spring dependency injection rather than constructing a service-dependent activity implementation with new.
Troubleshooting
temporal is not recognized as an internal or external command
The Temporal CLI is not installed or its executable directory is not on PATH. Verify the location of temporal.exe, run it by full path, or add its folder to PATH and open a new terminal.
Connection refused: /127.0.0.1:7233
The Temporal service is not running or is not listening on the expected address/port. Start temporal server start-dev and keep it running before launching the worker or starter.
No package found matching input criteria from Winget
The package identifier used in the Winget command was not found. Use the official Temporal CLI installation instructions instead of repeatedly guessing package names:

https://docs.temporal.io/cli
Java cannot find DroneMissionWorkflowImpl or DroneMissionActivitiesImpl
Check that all six Temporal files have the same package declaration and are in the matching directory. In Eclipse, refresh the project and run Maven → Update Project.
Maven cannot resolve the Spring Boot parent
The supplied POM uses 4.2.0-SNAPSHOT and a Spring snapshot repository. If that snapshot is unavailable, use a stable Spring Boot version available from your configured Maven repositories.
Current limitations / next steps
- Replace simulated activities with operations that safely validate and update the relevant mission and drone records.
- Integrate activity implementations with existing Spring services using dependency injection.
- Persist mission execution outcomes and workflow signals in MySQL.
- Add tests for successful execution, retry exhaustion, missing missions, and persistence failures.
- Add configuration for Temporal address, namespace, and task queue rather than hard-coding them.
- Make activity operations idempotent before using automatic retries with real hardware or external systems.
License
Add your project's license and contribution guidelines here if applica
