# POCForce - AI CRM Dashboard

## Overview

POCForce is a CRM dashboard application designed to help sales engineers and customer success engineers manage Proof of Concept (POC) engagements with potential customers. The system tracks POCs through various phases (Discovery, Planning, Execution, Validation, and Closed statuses), manages requirements, and provides comprehensive metrics and dashboards for team oversight.

## Technology Stack

- **Language**: Kotlin 1.9.25
- **Framework**: Spring Boot 3.4.0
- **Build Tool**: Gradle 8.10.2
- **Java Version**: Java 17
- **Database (Development)**: H2 (in-memory)
- **Database (Production)**: PostgreSQL (configurable)
- **ORM**: Spring Data JPA / Hibernate
- **Validation**: Bean Validation (Jakarta Validation)
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit 5, MockK, Spring Boot Test

## Prerequisites

- Java 17 or higher
- Gradle 8.x (or use the included Gradle wrapper)
- (Optional) Docker for containerized deployment

## Getting Started

### Clone the Repository

```bash
git clone <repository-url>
cd ai-crm-dash
```

### Build the Application

```bash
./gradlew build
```

### Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`.

### Run Tests

Run all tests:
```bash
./gradlew test
```

Run specific test class:
```bash
./gradlew test --tests "com.example.aicrmdash.service.PocServiceTest"
```

## Accessing the Application

### H2 Database Console

The H2 console is available for development/debugging:

- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:pocforce`
- **Username**: `sa`
- **Password**: (leave empty)

### Swagger UI (API Documentation)

Interactive API documentation is available via Swagger UI:

- **URL**: http://localhost:8080/swagger-ui.html
- All REST endpoints are documented with request/response examples and status codes

## API Endpoints

### POC Management

- `GET /api/pocs` - List all POCs (with filters: phase, status, ownerId)
- `GET /api/pocs/{id}` - Get POC details by ID
- `POST /api/pocs` - Create a new POC
- `PUT /api/pocs/{id}` - Update POC details
- `PATCH /api/pocs/{id}/phase` - Transition POC to next phase
- `PATCH /api/pocs/{id}/status` - Update POC status
- `DELETE /api/pocs/{id}` - Delete a POC (cascade deletes requirements)

### Requirement Management

- `GET /api/requirements?pocId={pocId}` - List requirements for a POC
- `POST /api/requirements` - Create a new requirement
- `PUT /api/requirements/{id}` - Update a requirement
- `PATCH /api/requirements/{id}/complete` - Mark requirement as complete
- `PATCH /api/requirements/{id}/uncomplete` - Mark requirement as incomplete
- `DELETE /api/requirements/{id}` - Delete a requirement

### User Management

- `GET /api/users` - List all users
- `GET /api/users/{id}` - Get user details by ID
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update user details
- `DELETE /api/users/{id}` - Delete a user

### Dashboard / Metrics

- `GET /api/dashboard/summary` - Get overall POC statistics
- `GET /api/dashboard/by-phase` - Get POC counts grouped by phase
- `GET /api/dashboard/by-owner` - Get POC counts per user/owner
- `GET /api/dashboard/at-risk` - Get list of at-risk POCs

### Health Check

- `GET /actuator/health` - Application health status (public endpoint)

## Configuration Options

Configuration is managed via `application.properties`:

### Database Configuration

```properties
# H2 (Development - default)
spring.datasource.url=jdbc:h2:mem:pocforce
spring.datasource.username=sa
spring.datasource.password=

# PostgreSQL (Production)
# spring.datasource.url=jdbc:postgresql://localhost:5432/pocforce
# spring.datasource.username=postgres
# spring.datasource.password=yourpassword
# spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### CORS Configuration

```properties
# Allowed frontend origins (comma-separated)
cors.allowed-origins=http://localhost:3000,http://localhost:4200
```

### JPA Configuration

```properties
spring.jpa.hibernate.ddl-auto=create-drop  # Use 'update' or 'validate' in production
spring.jpa.show-sql=true  # Set to false in production
```

## Project Structure

```
ai-crm-dash/
├── docs/
│   └── spec-driven-development/
│       ├── spec.md            # Product specification
│       ├── requirements.md    # Detailed requirements
│       └── tasks.md           # Implementation tasks
├── src/
│   ├── main/
│   │   ├── kotlin/com/example/aicrmdash/
│   │   │   ├── config/        # Configuration classes
│   │   │   ├── controller/    # REST controllers
│   │   │   ├── domain/        # JPA entities and enums
│   │   │   ├── dto/           # Data Transfer Objects
│   │   │   ├── exception/     # Exception handling
│   │   │   ├── repository/    # JPA repositories
│   │   │   └── service/       # Business logic
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── kotlin/com/example/aicrmdash/
│           ├── controller/    # Integration tests
│           └── service/       # Unit tests
├── build.gradle.kts
└── README.md
```

## Development Guidelines

### Code Style

- Follow Kotlin coding conventions
- Use KDoc comments for all public classes and methods
- Use meaningful variable and function names
- Keep methods focused and concise

### Testing

- Write unit tests for service layer logic
- Write integration tests for controller endpoints
- Aim for high test coverage on business logic
- Use MockK for mocking in Kotlin tests

### Layered Architecture

- **Controller Layer**: Handle HTTP requests/responses, validation, and error handling
- **Service Layer**: Implement business logic and orchestration
- **Repository Layer**: Data access via Spring Data JPA
- **Domain Layer**: JPA entities representing the data model
- **DTO Layer**: Data transfer objects for API contracts

## Docker Deployment

### Build Docker Image

```bash
docker build -t ai-crm-dash:latest .
```

### Run with Docker Compose

```bash
docker-compose up
```

This will start the application with PostgreSQL database.

## Documentation

For detailed product specifications and requirements, see:

- [Product Specification](docs/spec-driven-development/spec.md)
- [Requirements](docs/spec-driven-development/requirements.md)
- [Implementation Tasks](docs/spec-driven-development/tasks.md)

## License

[Specify your license here]

## Contact

[Specify contact information here]
