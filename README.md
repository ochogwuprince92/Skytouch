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
- Podman
- Jenkins (CI/CD)

## Prerequisites

- JDK 21
- PostgreSQL (local database: `skytouch_db`)
- Git
- Podman (optional, for containerized runs)

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

Secrets and environment-specific values live in a `.env` file at the project root. Config files reference them via `${VAR_NAME}` — no secrets are hardcoded in YAML.

### Setup

```powershell
cp .env.example .env
```

Edit `.env` with your local values. `.env` is gitignored; `.env.example` is the committed template.

| Variable | Used in |
|----------|---------|
| `PORT` | `application.yml`, Docker |
| `DATASOURCE_URL` | `application-local.yml`, `application-prod.yml` |
| `DATASOURCE_USERNAME` | `application-local.yml`, `application-prod.yml` |
| `DATASOURCE_PASSWORD` | `application-local.yml`, `application-prod.yml`, Docker Postgres |
| `POSTGRES_DB` | `docker-compose.yml` |
| `POSTGRES_USER` | `docker-compose.yml` |
| `POSTGRES_PASSWORD` | `docker-compose.yml` |
| `MAIL_USERNAME` | `application-local.yml`, `application-prod.yml` |
| `MAIL_PASSWORD` | `application-local.yml`, `application-prod.yml` |
| `JWT_SECRET` | `application-local.yml`, `application-prod.yml` |
| `APP_FRONTEND_URL` | `application-local.yml`, `application-prod.yml` |

Spring Boot loads `.env` automatically via `spring.config.import` in `application.yml`.

| Profile | File | Purpose |
|---------|------|---------|
| `local` | `application-local.yml` | Local development (default) |
| `prod` | `application-prod.yml` | Production (env-based config) |
| `test` | `application-test.yml` | CI/tests (PostgreSQL via env vars) |

Base settings live in `application.yml`. The active profile is `local` unless overridden:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### Database

Local PostgreSQL connection (values in `.env`):

- **URL:** `${DATASOURCE_URL}` → `jdbc:postgresql://localhost:5432/skytouch_db`
- **Migrations:** `src/main/resources/db/migration/`

Flyway runs automatically on startup and applies pending migrations.

## Docker

### Run with Podman Compose

Starts PostgreSQL and the app together:

```powershell
podman compose up --build
```

App: **http://localhost:8083**

Stop containers:

```powershell
podman compose down
```

### Build image only

```powershell
podman build -t skytouch-app .
```

Run the image (requires a running PostgreSQL instance):

```powershell
podman run -p 8083:5000 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e DATASOURCE_URL=jdbc:postgresql://host.containers.internal:5432/skytouch_db `
  -e DATASOURCE_USERNAME=postgres `
  -e DATASOURCE_PASSWORD=your-password `
  -e JWT_SECRET=your-jwt-secret `
  -e MAIL_USERNAME=noreply@example.com `
  -e MAIL_PASSWORD=changeme `
  -e APP_FRONTEND_URL=http://localhost:4174 `
  skytouch-app
```

## CI/CD (Jenkins + Podman)

### Team setup (simplest)

**One Jenkins server for the whole team** — not on every laptop.

| Who | Needs Jenkins? | Needs Podman? |
|-----|----------------|---------------|
| Each developer | No | Yes (local app only) |
| One shared server / teammate PC | Yes | Yes |

Everyone else: `git push` → Jenkins on the shared server runs `Jenkinsfile`.

### Run Jenkins locally (test the pipeline)

From the `Skytouch` folder:

```powershell
cd C:\Users\Staff\Downloads\skytouch_project\Skytouch
copy .env.jenkins.example .env.jenkins
# Edit .env.jenkins — set POSTGRES_PASSWORD and JWT_SECRET (CI-only values)
podman compose -f docker-compose.jenkins.yml up --build -d
```

> `.env.jenkins` is gitignored. Never commit real credentials. Use disposable values for local CI only.

Open **http://localhost:8080**

**First-time setup:**

1. Get the initial admin password:
   ```powershell
   podman exec skytouch-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
2. Install suggested plugins
3. Create an admin user
4. **New Item** → **Pipeline** → name it `skytouch`
5. **Pipeline** → Definition: *Pipeline script from SCM*
6. SCM: **Git** → `https://github.com/ochogwuprince92/Skytouch.git`
7. Script Path: `Jenkinsfile`
8. Save → **Build Now**

Stop Jenkins:

```powershell
podman compose -f docker-compose.jenkins.yml down
```

> **Note:** Skytouch app uses port **8083**, Jenkins uses **8080** — no conflict.

### Pipeline stages (`Jenkinsfile`)

| Stage | Description |
|-------|-------------|
| Checkout | Pulls source code |
| Test | `./mvnw clean test` (uses `postgres-ci` + env from `.env.jenkins`) |
| Package | Builds JAR |
| Build Image | `podman build` |
| Push Image | `main` / `dev` / `v*` tags only |

### Registry push (optional)

Only needed when pushing images. In Jenkins → **Credentials** → add `docker-registry-credentials` (username/password).

| Variable | Example |
|----------|---------|
| `DOCKER_REGISTRY` | `docker.io` |
| `DOCKER_IMAGE_NAME` | `your-org/skytouch` |

### Required environment variables (production)

| Variable | Description |
|----------|-------------|
| `DATASOURCE_URL` | PostgreSQL JDBC URL |
| `DATASOURCE_USERNAME` | Database username |
| `DATASOURCE_PASSWORD` | Database password |
| `JWT_SECRET` | JWT signing secret |
| `MAIL_USERNAME` | SMTP username |
| `MAIL_PASSWORD` | SMTP password |
| `APP_FRONTEND_URL` | Frontend base URL |
| `PORT` | Server port (default: `5000` in prod) |

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
