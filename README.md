# webCatalog

Product catalog monorepo: a Spring Boot 3 (Java 21) REST API backed by PostgreSQL, and a plain HTML/CSS/JavaScript frontend with no build step. The frontend also includes a Rick and Morty browser that calls its public API directly.

## Structure

```text
webCatalog/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/                    libs.versions.toml (dependency versions) + wrapper
├── gradlew, gradlew.bat
├── apps/
│   ├── backend/               Spring Boot API (Java 21, PostgreSQL, Flyway)
│   │   └── src/main/java/org/example/...
│   └── frontend/              Static pages (no build step): products and Rick and Morty
├── packages/
│   └── shared/                Typed Java client and OpenAPI contract for the API
├── tests/
│   ├── integration/           Tests that need a real PostgreSQL (Testcontainers)
│   └── e2e/                   Playwright API tests against a running backend
├── .github/workflows/         ci.yml (build and tests) and suitest.yml (API smoke test)
├── docker-compose.yml         PostgreSQL 16 for local development
├── suitest.config.json        Database and base URL used by the Suitest workflow
└── .env.example
```

## Requirements

- JDK 21 (Gradle downloads the toolchain only if it is missing; the wrapper needs nothing installed)
- Docker (PostgreSQL via `docker compose`, and the integration tests)
- Node.js 22+ (only to run the frontend tests and the static server)

## Configuration

```bash
cp .env.example .env   # then set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d
```

The backend reads its datasource from `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`. Spring does not read `.env` by itself, so provide them in one of two ways:

- **Local development (default):** create `apps/backend/src/main/resources/application-local.properties` with the three variables, plus `spring.flyway.locations=classpath:db/migration,classpath:db/seed` so a fresh database is seeded with the 15 demo products. The `local` profile is active when no profile is set, and the file is gitignored. Never commit it.
- **Environment variables:** export them in your shell or in your IDE run configuration. Environment variables take precedence over the local file, which is how CI and deployments configure the database.

## Backend

```bash
./gradlew :apps:backend:bootRun        # API at http://localhost:8080
./gradlew :apps:backend:bootJar        # apps/backend/build/libs/app.jar
```

### Schema and seed data

The schema is managed by Flyway, and Hibernate only validates the entities against it:

- `db/migration/V1__catalog_schema.sql` creates the `products` table. Applied once, in every environment.
- `db/seed/R__seed_products.sql` inserts the 15 demo products when their names are missing. It is a repeatable migration, so it runs on startup, never duplicates a product, and keeps edits to existing products.

`spring.flyway.locations` decides what is applied. Production uses only `classpath:db/migration` (schema, no demo data); local development and QA add `classpath:db/seed`. A database that already applied the seed keeps starting when the seed is no longer on the path, thanks to `spring.flyway.ignore-migration-patterns=repeatable:missing`.

Add schema changes as a new `V<n>__description.sql` file. Never edit a migration that has already been applied to a database you want to keep.

Because the project has not been released yet, the initial schema may be rebuilt. If you hit `checksum mismatch` after an update, reset your local database instead of editing migrations:

```bash
docker compose down -v        # deletes the local PostgreSQL volume and its data
docker compose up -d
```

### Endpoints

| Operation     | Method | Route                  |
|---------------|--------|------------------------|
| List / search | GET    | `/api/products?q=text` |
| Get one       | GET    | `/api/products/{id}`   |
| Create        | POST   | `/api/products`        |
| Update        | PUT    | `/api/products/{id}`   |
| Delete        | DELETE | `/api/products/{id}`   |

Health endpoints: `GET /api/health` and `GET /actuator/health`.

Errors are RFC 7807 problems (`application/problem+json`) with a `traceId` property. The same id is in the `X-Trace-Id` response header and in the server log. A valid incoming `X-Trace-Id` header (letters, digits and dashes, up to 64 characters) is reused; otherwise a new id is generated. Unexpected errors return a generic 500 message; the details stay in the log under the same `traceId`.

## Typed API client

`packages/shared` contains a typed Java client generated from `packages/shared/src/main/openapi/catalog-api.yaml` by the `openapi-generator` Gradle plugin. The generated sources go to `packages/shared/build/generated/openapi/catalog-client` and are regenerated on every compile. To change the API, edit the spec and compile:

```bash
./gradlew :packages:shared:compileJava   # regenerates the client
```

Usage (package `org.example.shared.catalog`):

```java
ApiClient client = new ApiClient();
client.updateBaseUri("http://localhost:8080");
List<Product> products = new ProductsApi(client).listProducts(null);
```

### Publishing the shared package

`packages/shared` is published as `org.example:catalog-shared`. The jar contains the typed client and the OpenAPI contract under `openapi/catalog-api.yaml`.

```bash
./gradlew :packages:shared:publishToMavenLocal   # installs into ~/.m2 for local consumers
```

No remote repository is configured yet.

## Integration tests

```bash
./gradlew :tests:integration:test      # starts PostgreSQL containers, needs Docker running
```

The integration tests use Testcontainers 1.21.4. Older Testcontainers versions fail against Docker Engine 29 with a 400 error from the Docker API.

## End-to-end API tests (Playwright)

`tests/e2e/api` holds Playwright tests that call the running API over HTTP: list, search, create, read, update, and delete with a 404 check. They use the `request` fixture, so no browser is needed. Tests run one at a time, because they create and delete their own product.

```bash
./gradlew :apps:backend:bootRun          # in one terminal, with the backend running on port 8080
cd tests/e2e
npm ci
npx playwright test                       # API_URL defaults to http://localhost:8080
```

## Frontend

```bash
cd apps/frontend
npm start        # http://localhost:5500 (python3 -m http.server)
npm test         # node --test for the Rick and Morty logic
```

The product page calls `http://localhost:8080/api/products` directly, so start the backend first. CORS is enabled for `/api/**`. If your API runs elsewhere, change `API_URL` in `apps/frontend/js/app.js`.

## Continuous integration

- `.github/workflows/ci.yml` runs on pull requests and on pushes to `main`: `./gradlew build` for the backend, and `npm test` for the frontend.
- `.github/workflows/suitest.yml` runs on pushes to `test`: it builds the backend jar, starts it against PostgreSQL 16, runs the Playwright API tests in `tests/e2e`, and runs the frontend tests. Its database settings are in `suitest.config.json`.

## Dependencies

Versions are declared in `gradle/libs.versions.toml`; the Spring Boot BOM manages the rest.

| Module | Libraries |
|--------|-----------|
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, spring-boot-starter-actuator, flyway-core, flyway-database-postgresql, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `packages/shared` | jackson-databind, jackson-datatype-jsr310, jakarta.annotation-api (generated client), junit-jupiter (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (core, postgresql, junit-jupiter), postgresql (runtime) |
| `apps/frontend` | none (plain HTML/CSS/JS) |
