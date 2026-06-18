# Skytouch

Backend API for the Skytouch platform, built with Spring Boot. The application uses a domain-driven package structure where each feature area owns its controller, API models, service, and repository layers.

## Tech Stack

- Java 21
- Spring Boot 4.0.6
- Spring Data JPA
- PostgreSQL
- Flyway (database migrations)
- Springdoc OpenAPI (Swagger UI)
- Lombok
- Maven

## Prerequisites

- JDK 21
- PostgreSQL (local database: `skytouch_db`)
- Git

## Getting Started

All Maven commands must be run from the `Skytouch` directory (where `pom.xml` and `mvnw.cmd` live).

```powershell
cd Skytouch
```

### Build

```powershell
.\mvnw.cmd clean install
```

Skip tests during development:

```powershell
.\mvnw.cmd clean install -DskipTests
```

### Run

```powershell
.\mvnw.cmd spring-boot:run
```

The app starts on **http://localhost:8083** by default (`local` profile).

### API Documentation

Once running, open Swagger UI at:

- http://localhost:8083/swagger-ui.html

## Configuration

| Profile | File | Purpose |
|---------|------|---------|
| `local` | `application-local.yml` | Local development (default) |
| `prod` | `application-prod.yml` | Production (env-based config) |

Base settings live in `application.yml`. The active profile is `local` unless overridden:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Database

Local PostgreSQL connection (configure in `application-local.yml`):

- **URL:** `jdbc:postgresql://localhost:5432/skytouch_db`
- **Migrations:** `src/main/resources/db/migration/`

Flyway runs automatically on startup and applies pending migrations.

## Project Structure

```
src/main/java/com/backend/Skytouch/
├── SkytouchApplication.java
├── authentication/          # Auth domain (scaffold)
│   ├── controller/
│   ├── apimodel/
│   ├── service/
│   └── repository/
├── jobseeker/               # Job seeker domain
│   ├── controller/
│   ├── apimodel/
│   ├── service/
│   └── repository/
├── user/                    # Shared user/account domain
│   ├── entity/
│   └── repository/
└── common/                  # Cross-cutting concerns
    ├── config/
    ├── enums/
    ├── exception/
    ├── mapper/
    └── utils/
```

### Domain Layout

Each domain follows the same vertical slice:

| Layer | Responsibility |
|-------|----------------|
| `controller` | REST endpoints |
| `apimodel` | Request/response DTOs |
| `service` | Business logic |
| `repository` | Data access |

Shared code (exceptions, mappers, utilities, enums) lives under `common/`. The `user` domain holds the core `Users` entity used across roles.

## API Endpoints

### Job Seekers

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/job-seekers` | List all job seekers |
| `GET` | `/api/job-seekers/{id}` | Get job seeker by ID |
| `POST` | `/api/job-seekers` | Register a new job seeker |

### Error Responses

API errors return a consistent JSON body via `GlobalExceptionHandler`:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Job seeker not found: ...",
  "timestamp": "2026-06-18T21:00:00"
}
```

## User Roles

| Role | Description |
|------|-------------|
| `JOB_SEEKER` | Job seeker account |
| `EMPLOYER` | Employer account |
| `ADMIN` | Platform administrator |

## Development Notes

- Use `.\mvnw.cmd` on Windows PowerShell (not `./mvnw` from the parent folder).
- If port `8083` is in use, stop the process or set `PORT` env var / `server.port` in config.
- Spring Security is not yet enabled; authentication domain scaffolding is in place for future implementation.
