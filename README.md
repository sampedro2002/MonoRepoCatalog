# Web catalog

Product catalog monorepo: a Spring Boot 3 backend on Java 21 with PostgreSQL, and a frontend in plain HTML, CSS and JavaScript served by Vite.

## Structure

```text
webCatalog/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/                    libs.versions.toml (dependency versions) + wrapper
├── gradlew, gradlew.bat
├── apps/
│   ├── backend/               Spring Boot API (Java 21, PostgreSQL, Flyway)
│   └── frontend/              Plain HTML/CSS/JS served by Vite (dev proxy to the backend)
├── packages/
│   └── shared/                Shared Java library: typed API client and OpenAPI contract
├── tests/
│   └── integration/           Tests that need a real PostgreSQL (Testcontainers)
├── .github/workflows/ci.yml   Continuous integration (backend and frontend)
├── docker-compose.yml         PostgreSQL for local development
└── .env.example
```

## Requirements

- JDK 21 (Gradle provisions the toolchain if it is missing)
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22 or later (frontend only)

## Setup

```bash
cp .env.example .env          # set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d          # start PostgreSQL on localhost:5432
```

The backend reads its database connection from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Export these variables in your shell or in your IDE run configuration. Spring does not read `.env` by itself.

The schema is managed by Flyway: scripts in `apps/backend/src/main/resources/db/migration` run on startup, and Hibernate only validates the entities against them. Add new changes as a new `V<n>__description.sql` file; never edit one that has already been applied.

## Backend

```bash
./gradlew :apps:backend:bootRun     # http://localhost:8080
./gradlew :apps:backend:test        # unit tests
./gradlew :apps:backend:bootJar     # apps/backend/build/libs/backend-1.0.0.jar
```

Health endpoints: `GET /api/health` (part of the typed client contract) and `GET /actuator/health`.

Component scanning covers both `org.example` (platform code) and `org.ejemplo` (catalog domain). JPA entity and repository scanning is configured in `PersistenceConfiguration`.

## Errors

Every error response is an RFC 7807 problem (`application/problem+json`) with a `traceId` property. The same id is in the `X-Trace-Id` response header and in the server log. If the caller sends a valid `X-Trace-Id` header (letters, digits and dashes, up to 64 characters), that value is reused; otherwise a new one is generated.

```json
{"type":"about:blank","title":"Not Found","status":404,"detail":"No static resource api/unknown.","instance":"/api/unknown","traceId":"3f2b8c1e-9a7d-4e21-b0c4-6d5e8f1a2b3c"}
```

Unexpected errors return a generic 500 message; the details stay in the log under the same `traceId`.

## Typed API client

`packages/shared` contains a typed Java client generated from `packages/shared/src/main/openapi/catalog-api.yaml` by the `openapi-generator` Gradle plugin. The generated sources go to `packages/shared/build/generated/openapi/catalog-client` and are rebuilt on every compile. To change the API, edit the spec and rebuild:

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

With the `dev` profile, the backend inserts 3 demo categories and 6 demo products (`DemoDataSeeder`) into an empty database. It does nothing if categories already exist.

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew :apps:backend:bootRun
```

To enable it permanently, export `SPRING_PROFILES_ACTIVE=dev` in your shell or add it to your IDE run configuration.

## Integration tests

```bash
./gradlew :tests:integration:test   # starts PostgreSQL containers; needs Docker running
```

The integration tests use Testcontainers 1.21.4. Older Testcontainers versions fail against Docker Engine 29 with a 400 error from the Docker API.

## Frontend

```bash
cd apps/frontend
npm ci                              # install from package-lock.json
npm run dev                         # http://localhost:5500
npm run build                       # production build into apps/frontend/dist
```

Vite proxies `/api` and `/actuator` to the backend on `http://localhost:8080`, so start the backend first to see its health status on the page.

## Continuous integration

`.github/workflows/ci.yml` runs on pull requests and on pushes to `main`:

- **backend:** `./gradlew build` (compiles and runs all tests, including the integration tests, which need Docker on the runner).
- **frontend:** `npm ci` and `npm run build` in `apps/frontend` on Node 22.

## Dependencies

Versions are declared in `gradle/libs.versions.toml`; the Spring Boot BOM manages the rest.

| Module | Libraries |
|--------|-----------|
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, spring-boot-starter-actuator, flyway-core, flyway-database-postgresql, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `packages/shared` | jackson-databind, jackson-datatype-jsr310, jakarta.annotation-api (generated client), junit-jupiter (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (core, postgresql, junit-jupiter), postgresql (runtime) |
| `apps/frontend` | vite (development and build only) |
