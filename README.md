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
│   └── frontend/              Static pages (no build step)
├── packages/
│   └── shared/                Shared Java library
├── tests/
│   └── integration/           Tests that need a real PostgreSQL (Testcontainers)
└── .env.example
```

## Requirements

- JDK 21 (Gradle provisions the toolchain if it is missing)
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22+ (only for the frontend's `npm start`)

## Setup

```bash
cp .env.example .env          # set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d          # start PostgreSQL on localhost:5432
```

The backend reads its datasource from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Export them in your shell or in your IDE run configuration.

## Backend

```bash
./gradlew :apps:backend:bootRun     # http://localhost:8080
./gradlew :apps:backend:test        # unit tests
./gradlew :apps:backend:bootJar     # apps/backend/build/libs/backend-1.0.0.jar
```

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

## Integration tests

```bash
./gradlew :tests:integration:test   # starts a PostgreSQL container; needs Docker running
```

## Frontend

```bash
cd apps/frontend
npm start                           # http://localhost:5500
```

## Dependencies

Versions are declared in `gradle/libs.versions.toml`; the Spring Boot BOM manages the rest.

| Module | Libraries |
|--------|-----------|
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (postgresql, junit-jupiter), postgresql (runtime) |
| `packages/shared` | jackson-databind, jackson-datatype-jsr310, jakarta.annotation-api (generated client), junit-jupiter (test) |
| `apps/frontend` | none (vanilla HTML/CSS/JS) |
