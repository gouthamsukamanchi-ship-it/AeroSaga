# AeroSaga — Autonomous Drone Mission Control

AeroSaga is a Spring Boot-based backend project for managing drones and their missions. It provides REST APIs to register drones, create and manage missions, track mission statuses, and validate requests using PostgreSQL as the database.

## Project Status

**Current Phase: Week 1 — Backend Foundation and REST API Development**

The initial backend APIs, database integration, request validation, and exception handling have been implemented.

## Technologies Used

- **Language:** Java
- **Framework:** Spring Boot
- **Backend:** Spring Web, Spring Data JPA
- **Database:** PostgreSQL
- **Validation:** Jakarta Bean Validation
- **Build Tool:** Maven
- **API Testing:** Postman
- **Testing:** JUnit 5, Spring Boot Test
- **Version Control:** Git and GitHub

## Features Implemented

### 1. Drone Management
- Create a new drone.
- Retrieve all drones.
- Retrieve a drone by ID.
- Update drone details.
- Validate required drone fields.
- Track drone availability and status.

### 2. Mission Management
- Create a mission and associate it with a drone.
- Retrieve all missions.
- Retrieve a mission by ID.
- Filter missions by status.
- Update mission details.
- Delete a mission.
- Verify that the selected drone exists and is available before creating a mission.

### 3. Validation and Exception Handling
- Validate required fields using `@NotBlank`.
- Validate mission request fields, including drone ID.
- Return `400 Bad Request` for validation errors.
- Return `404 Not Found` when a requested drone or mission does not exist.
- Return `409 Conflict` when a drone is unavailable for a new mission.
- Handle API errors through a centralized `@RestControllerAdvice` class.

### 4. Database Integration
- Connected Spring Boot to PostgreSQL.
- Used Spring Data JPA repositories for database operations.
- Created entity relationships between drones and missions.

## API Endpoints

Base URL: `http://localhost:8080`

### Drone APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/drones` | Create a drone |
| GET | `/api/drones` | Retrieve all drones |
| GET | `/api/drones/{id}` | Retrieve a drone by ID |
| PUT | `/api/drones/{id}` | Update a drone |

### Mission APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/missions` | Create a mission |
| GET | `/api/missions` | Retrieve all missions |
| GET | `/api/missions/{id}` | Retrieve a mission by ID |
| GET | `/api/missions?status=PENDING` | Filter missions by status |
| PUT | `/api/missions/{id}` | Update a mission |
| DELETE | `/api/missions/{id}` | Delete a mission |

## Sample API Requests

### Create a Drone

`POST /api/drones`

```json
{
  "name": "Drone Alpha",
  "model": "Aero-X1",
  "status": "AVAILABLE"
}
```

### Create a Mission

`POST /api/missions`

```json
{
  "droneId": 1,
  "missionName": "Solar Panel Inspection",
  "destination": "Rooftop-A"
}
```

New missions are initially assigned the `PENDING` status. A drone must exist and have `AVAILABLE` status before it can be assigned a new mission.

## Mission Status Flow

The intended mission lifecycle is:

`PENDING → IN_PROGRESS → COMPLETED`

A mission may also transition to `FAILED` when a failure occurs. Automated workflow-driven transitions are planned for a later phase.

## Project Structure

```text
src/
├── main/
│   ├── java/com/aerosaga/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── exception/
│   │   ├── repository/
│   │   ├── service/
│   │   └── AerosagaBackendApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/aerosaga/
        └── AerosagaBackendApplicationTests.java
```

## Setup and Run

### Prerequisites
- Java JDK
- PostgreSQL
- Maven, or the included Maven Wrapper

### 1. Clone the repository

```bash
git clone https://github.com/Ankit-mudi/AeroSaga.git
cd AeroSaga
```

Switch to the development branch:

```bash
git switch ankit-mudi
```

### 2. Configure PostgreSQL

Create a PostgreSQL database named `aerosaga_db`.

Configure the database URL, username, and password in `src/main/resources/application.properties`. Use your own local database credentials and avoid committing passwords or other secrets.

### 3. Run the application

On Windows:

```cmd
mvnw.cmd spring-boot:run
```

Alternatively, if Maven is installed:

```bash
mvn spring-boot:run
```

The application runs on port `8080` by default.

## Testing

The following checks have been performed during Week 1 development:

- Tested drone and mission API operations using Postman.
- Verified request validation and error responses.
- Tested unavailable-drone conflict handling.
- Ran the Spring Boot context-load test.
- Built the project successfully using Maven.

## Upcoming Work

- Integrate Temporal workflows for mission execution.
- Implement workflow activities and worker configuration.
- Add retry policies, timeouts, and failure handling.
- Add further automated tests and project documentation.

## Repository

GitHub: https://github.com/Ankit-mudi/AeroSaga

Development branch: `ankit-mudi`
