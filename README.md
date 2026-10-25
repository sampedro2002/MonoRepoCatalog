# Catalogo web

Monorepositorio del catalogo de productos: backend con Spring Boot 3, Java 21 y PostgreSQL, junto con un frontend estatico en HTML, CSS y JavaScript.

> Estado: configuracion inicial. La configuracion de compilacion, las dependencias y las herramientas estan listas; las funcionalidades de la aplicacion se implementan por modulos.

## Estructura

```text
catalogo-web/
├── settings.gradle.kts / build.gradle.kts / gradle.properties
├── gradle/                    libs.versions.toml (dependency versions) + wrapper
├── gradlew, gradlew.bat
├── apps/
│   ├── backend/               Java + PostgreSQL (Spring Boot)
│   └── frontend/              HTML/JS puro servido por Vite (proxy de desarrollo al backend)
├── packages/
│   └── shared/                Libreria Java compartida
├── tests/
│   └── integration/           Pruebas que requieren PostgreSQL real (Testcontainers)
└── .env.example
```

## Requisitos

- JDK 21 (Gradle aprovisiona el toolchain si no esta instalado)
- Docker (PostgreSQL mediante `docker compose` y las pruebas de integracion)
- Node.js 22 o superior (para `npm run dev` del frontend)

## Configuracion

```bash
cp .env.example .env          # set POSTGRES_PASSWORD and DB_PASSWORD
docker compose up -d          # start PostgreSQL on localhost:5432
```

El backend lee la conexion de base de datos desde `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. Exporta estas variables en la terminal o en la configuracion de ejecucion del IDE.

El esquema es administrado por Flyway. Las migraciones de `apps/backend/src/main/resources/db/migration` se ejecutan al iniciar y Hibernate solo valida las entidades. Para nuevos cambios, agrega un archivo `V<n>__descripcion.sql`; nunca edites una migracion ya aplicada.

## Backend

```bash
./gradlew :apps:backend:bootRun     # http://localhost:8080
./gradlew :apps:backend:test        # pruebas unitarias
./gradlew :apps:backend:bootJar     # apps/backend/build/libs/backend-1.0.0.jar
```

## Pruebas de integracion

```bash
./gradlew :tests:integration:test   # inicia un contenedor PostgreSQL; requiere Docker
```

## Frontend

```bash
cd apps/frontend
npm install
npm run dev                         # http://localhost:5500
```

Vite redirige `/api` y `/actuator` al backend en `http://localhost:8080`. Inicia primero el backend para consultar su estado de salud en la pagina.

## Dependencias

Las versiones se declaran en `gradle/libs.versions.toml`; el BOM de Spring Boot administra las versiones compatibles.

| Modulo | Librerias |
|--------|-----------|
| `apps/backend` | spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, spring-boot-starter-actuator, flyway-core, flyway-database-postgresql, postgresql (runtime), spring-boot-devtools (development only), spring-boot-starter-test (test) |
| `tests/integration` | spring-boot-starter-web, spring-boot-starter-test, spring-boot-testcontainers, testcontainers (postgresql, junit-jupiter), postgresql (runtime) |
| `packages/shared` | aun sin contenido |
| `apps/frontend` | vite para desarrollo y compilacion |
