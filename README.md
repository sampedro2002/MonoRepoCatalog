feature/integration-quality-MW-5
# Web catalog

Product catalog monorepo: a Spring Boot 3 backend on Java 21 with PostgreSQL, and a frontend in plain HTML, CSS and JavaScript served by Vite.

> Status: initial setup. Build configuration, dependencies and tooling are in place; application features are implemented module by module.
# webCatalog

Product catalog monorepo: a Spring Boot 3 (Java 21) and PostgreSQL backend, and a static HTML/CSS/JavaScript frontend.

> Status: project setup. Build configuration, dependencies and tooling are in place; application features are not implemented yet.test

## Structure

```text
webCatalog/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/                    libs.versions.toml (dependency versions) + wrapper
├── gradlew, gradlew.bat
├── apps/feature/catalog-ui-MW-4
│   ├── backend/               Java + PostgreSQL (Spring Boot)feature/integration-quality-MW-5
│   └── frontend/              Plain HTML/JS served by Vite (development proxy to the backend)
├── packages/
│   └── shared/                Shared Java library: typed API client and OpenAPI contract
├── tests/
│   └── integration/           Tests that need a real PostgreSQL (Testcontainers)
├── docker-compose.yml
│   └── frontend/              Static pages (no build step)
│   ├── backend/               Java + PostgreSQL (Spring Boot)feature/product-crud-MW-3
│   └── frontend/              Static pages (no build step)
│   └── frontend/              Vanilla HTML/JS served by Vite (dev proxy to the backend) testtest
├── packages/
│   └── shared/                Shared Java library
├── tests/
│   └── integration/           Tests that need a real PostgreSQL (Testcontainers)test
└── .env.example
```

## Requirements
feature/integration-quality-MW-5
- JDK 21 (Gradle provisions the toolchain if it is missing)
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22 or later (for `npm run dev` in the frontend)
- 
- JDK 21 (Gradle provisions the toolchain if it is missing)feature/catalog-ui-MW-4
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22+ (only for the frontend's `npm start`)

- Docker (PostgreSQL via `docker compose`, and the integration tests) feature/product-crud-MW-3
- Node.js 22+ (only for the frontend's `npm start`)

- Node.js 22+ (only for the frontend's `npm run dev`)
- test test
- test
## Setup

```bash
cp .env.example .env          # set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d          # start PostgreSQL on localhost:5432
```
feature/integration-quality-MW-5
The backend reads its database connection from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Export these variables in your shell or in your IDE run configuration.

The schema is managed by Flyway. Migrations in `apps/backend/src/main/resources/db/migration` run at startup, and Hibernate only validates the entities. For new schema changes, add a file named `V<n>__description.sql`; never edit a migration that has already been applied.
The backend reads its datasource from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Export them in your shell or in your IDE run configuration.feature/catalog-ui-MW-4
feature/product-crud-MW-3

The schema is managed by Flyway: scripts in `apps/backend/src/main/resources/db/migration` run on startup, and Hibernate only validates the entities against them. Add new changes as a new `V<n>__description.sql` file; never edit one that has already been applied.

## Backend

```bash
./gradlew :apps:backend:bootRun     # http://localhost:8080
./gradlew :apps:backend:test        # unit tests
./gradlew :apps:backend:bootJar     # apps/backend/build/libs/backend-1.0.0.jar
```
feature/integration-quality-MW-5
Health endpoints: `GET /api/health` (part of the typed client contract) and `GET /actuator/health`.

## Errors

Every error response is an RFC 7807 problem (`application/problem+json`) with a `traceId` property. The same id is in the `X-Trace-Id` response header and in the server log. If the caller sends a valid `X-Trace-Id` header (letters, digits and dashes, up to 64 characters), that value is reused; otherwise a new one is generated.

```json
{"type":"about:blank","title":"Not Found","status":404,"detail":"No static resource api/unknown.","instance":"/api/unknown","traceId":"3f2b8c1e-9a7d-4e21-b0c4-6d5e8f1a2b3c"}
```

Unexpected errors return a generic 500 message; the details stay in the log under the same `traceId`.

## Typed API client

`packages/shared` contains a typed Java client generated from `packages/shared/src/main/openapi/catalog-api.yaml` by the `openapi-generator` Gradle plugin.
The generated sources go to `packages/shared/build/generated/openapi/catalog-client` and are rebuilt on every compile. To change the API, edit the spec, then rebuild:

```bash
./gradlew :packages:shared:compileJava   # regenerates the client
## Integration tests

```bash
./gradlew :tests:integration:test   # starts a PostgreSQL container; needs Docker runningtest
```

Usage (package `org.example.shared.catalog`):

```java
ApiClient client = new ApiClient();
client.updateBaseUri("http://localhost:8080");
HealthStatus status = new HealthApi(client).getHealth();
```

## Publishing the shared package

`packages/shared` is published as `org.example:catalog-shared`. The jar contains the typed client and the OpenAPI contract under `openapi/catalog-api.yaml`.

```bash
./gradlew :packages:shared:publishToMavenLocal   # installs into ~/.m2 for local consumers
```

No remote repository is configured yet.

## Demo data

With the `dev` profile, the backend inserts 3 demo categories and 6 demo products into an empty database. It does nothing if the data already exists.

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew :apps:backend:bootRun
```

To enable it permanently, export `SPRING_PROFILES_ACTIVE=dev` in your shell or add it to your IDE run configuration. Spring does not read `.env` by itself.

## Integration tests

```bash
./gradlew :tests:integration:test   # starts a PostgreSQL container; needs Docker running
```

The integration tests use Testcontainers 1.21.4. Older Testcontainers versions fail against Docker Engine 29 with a 400 error from the Docker API.

## Frontend

```bash
cd apps/frontend
npm start                           # http://localhost:5500
```
feature/integration-quality-MW-5
Vite forwards `/api` and `/actuator` to the backend at `http://localhost:8080`. Start the backend first to see its health status on the page.

feature/catalog-ui-MW-4
feature/product-crud-MW-3
Vite proxies `/api` and `/actuator` to the backend on `http://localhost:8080`, so start the backend first to see its health status on the page.

test
test
test
## Dependencies

Versions are declared in `gradle/libs.versions.toml`; the Spring Boot BOM manages the rest.

| Module | Libraries |
|--------|-----------|feature/integration-quality-MW-5
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, spring-boot-starter-actuator, flyway-core, flyway-database-postgresql, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (core, postgresql, junit-jupiter), postgresql (runtime) |
| `packages/shared` | jackson-databind, jackson-datatype-jsr310, jakarta.annotation-api (generated client), junit-jupiter (test) |
| `apps/frontend` | Vite for development and build |
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (postgresql, junit-jupiter), postgresql (runtime) |feature/catalog-ui-MW-4
| `packages/shared` | none yet |
| `apps/frontend` | none (vanilla HTML/CSS/JS) |
| `packages/shared` | none yet | feature/product-crud-MW-3
| `apps/frontend` | none (vanilla HTML/CSS/JS) |
| `apps/frontend` | vite (dev/build only) | test test
test
