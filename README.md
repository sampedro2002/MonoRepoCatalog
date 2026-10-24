# webCatalog

Product catalog monorepo: a Spring Boot 3 (Java 21) and PostgreSQL backend, and a static HTML/CSS/JavaScript frontend.

> Status: project setup. Build configuration, dependencies and tooling are in place; application features are not implemented yet.

## Structure

```text
webCatalog/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/                    libs.versions.toml (dependency versions) + wrapper
├── gradlew, gradlew.bat
├── apps/
│   ├── backend/               Java + PostgreSQL (Spring Boot)
│   └── frontend/              Vanilla HTML/JS served by Vite (dev proxy to the backend)
├── packages/
│   └── shared/                Shared Java library
├── tests/
│   └── integration/           Tests that need a real PostgreSQL (Testcontainers)
└── .env.example
```

## Requirements

- JDK 21 (Gradle provisions the toolchain if it is missing)
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22+ (only for the frontend's `npm run dev`)

## Setup

```bash
cp .env.example .env          # set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d          # start PostgreSQL on localhost:5432
```

The backend reads its datasource from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Export them in your shell or in your IDE run configuration.

The schema is managed by Flyway: scripts in `apps/backend/src/main/resources/db/migration` run on startup, and Hibernate only validates the entities against them. Add new changes as a new `V<n>__description.sql` file; never edit one that has already been applied.

## Backend

```bash
./gradlew :apps:backend:bootRun     # http://localhost:8080
./gradlew :apps:backend:test        # unit tests
./gradlew :apps:backend:bootJar     # apps/backend/build/libs/backend-1.0.0.jar
```

## Integration tests

```bash
./gradlew :tests:integration:test   # starts a PostgreSQL container; needs Docker running
```

## Frontend

```bash
cd apps/frontend
npm install
npm run dev                         # http://localhost:5500
```

Vite proxies `/api` and `/actuator` to the backend on `http://localhost:8080`, so start the backend first to see its health status on the page.

## Dependencies

Versions are declared in `gradle/libs.versions.toml`; the Spring Boot BOM manages the rest.

| Module | Libraries |
|--------|-----------|
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, spring-boot-starter-actuator, flyway-core, flyway-database-postgresql, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (postgresql, junit-jupiter), postgresql (runtime) |
| `packages/shared` | none yet |
| `apps/frontend` | vite (dev/build only) |
