# webCatalog

Product catalog monorepo: a Spring Boot 3 (Java 21) and PostgreSQL backend, and a static HTML/CSS/JavaScript frontend.

> Status: project setup. Build configuration, dependencies and tooling are in place; application features are not implemented yet.

## Structure

```text
webCatalog/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/                    libs.versions.toml (dependency versions) + wrapper
├── gradlew, gradlew.bat
├── apps/feature/catalog-ui-MW-4
│   ├── backend/               Java + PostgreSQL (Spring Boot)
│   └── frontend/              Static pages (no build step)
│   ├── backend/               Java + PostgreSQL (Spring Boot)feature/product-crud-MW-3
│   └── frontend/              Static pages (no build step)
│   └── frontend/              Vanilla HTML/JS served by Vite (dev proxy to the backend) testtest
├── packages/
│   └── shared/                Shared Java library
├── tests/
│   └── integration/           Tests that need a real PostgreSQL (Testcontainers)
└── .env.example
```

## Requirements

- JDK 21 (Gradle provisions the toolchain if it is missing)feature/catalog-ui-MW-4
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22+ (only for the frontend's `npm start`)

- Docker (PostgreSQL via `docker compose`, and the integration tests) feature/product-crud-MW-3
- Node.js 22+ (only for the frontend's `npm start`)

- Node.js 22+ (only for the frontend's `npm run dev`)
- test test
## Setup

```bash
cp .env.example .env          # set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d          # start PostgreSQL on localhost:5432
```

The backend reads its datasource from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Export them in your shell or in your IDE run configuration.feature/catalog-ui-MW-4
feature/product-crud-MW-3

The schema is managed by Flyway: scripts in `apps/backend/src/main/resources/db/migration` run on startup, and Hibernate only validates the entities against them. Add new changes as a new `V<n>__description.sql` file; never edit one that has already been applied.
test
test

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
npm start                           # http://localhost:5500
```

feature/catalog-ui-MW-4
feature/product-crud-MW-3
Vite proxies `/api` and `/actuator` to the backend on `http://localhost:8080`, so start the backend first to see its health status on the page.

test
test
## Dependencies

Versions are declared in `gradle/libs.versions.toml`; the Spring Boot BOM manages the rest.

| Module | Libraries |
|--------|-----------|
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (postgresql, junit-jupiter), postgresql (runtime) |feature/catalog-ui-MW-4
| `packages/shared` | none yet |
| `apps/frontend` | none (vanilla HTML/CSS/JS) |
| `packages/shared` | none yet | feature/product-crud-MW-3
| `apps/frontend` | none (vanilla HTML/CSS/JS) |
| `apps/frontend` | vite (dev/build only) | test test
