# Web catalog

Product catalog monorepo: a Spring Boot 3 backend on Java 21 with PostgreSQL, and a frontend in plain HTML, CSS and JavaScript served by Vite.

> Status: initial setup. Build configuration, dependencies and tooling are in place; application features are implemented module by module.

## Structure

```text
webCatalog/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/                    libs.versions.toml (dependency versions) + wrapper
├── gradlew, gradlew.bat
├── apps/
│   ├── backend/               Java + PostgreSQL (Spring Boot)
│   └── frontend/              Plain HTML/JS served by Vite (development proxy to the backend)
├── packages/
│   └── shared/                Shared Java library: typed API client and OpenAPI contract
├── tests/
│   └── integration/           Tests that need a real PostgreSQL (Testcontainers)
├── docker-compose.yml
└── .env.example
```

## Requirements

- JDK 21 (Gradle provisions the toolchain if it is missing)
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22 or later (for `npm run dev` in the frontend)

## Setup

```bash
cp .env.example .env          # set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d          # start PostgreSQL on localhost:5432
```

The backend reads its database connection from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Export these variables in your shell or in your IDE run configuration.

The schema is managed by Flyway. Migrations in `apps/backend/src/main/resources/db/migration` run at startup, and Hibernate only validates the entities. For new schema changes, add a file named `V<n>__description.sql`; never edit a migration that has already been applied.

## Backend

```bash
./gradlew :apps:backend:bootRun     # http://localhost:8080
./gradlew :apps:backend:test        # unit tests
./gradlew :apps:backend:bootJar     # apps/backend/build/libs/backend-1.0.0.jar
```

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
npm install
npm run dev                         # http://localhost:5500
```

Vite forwards `/api` and `/actuator` to the backend at `http://localhost:8080`. Start the backend first to see its health status on the page.

## Dependencies

Versions are declared in `gradle/libs.versions.toml`; the Spring Boot BOM manages the rest.

| Module | Libraries |
|--------|-----------|
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, spring-boot-starter-actuator, flyway-core, flyway-database-postgresql, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (core, postgresql, junit-jupiter), postgresql (runtime) |
| `packages/shared` | jackson-databind, jackson-datatype-jsr310, jakarta.annotation-api (generated client), junit-jupiter (test) |
| `apps/frontend` | Vite for development and build |
