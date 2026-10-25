# --- Build stage ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace

# Gradle wrapper and build scripts first, so the dependency layer is cached
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY gradle.properties .

# Build scripts of every module in settings.gradle.kts, otherwise Gradle cannot configure the build
COPY apps/backend/build.gradle.kts apps/backend/
COPY packages/shared/build.gradle.kts packages/shared/
COPY tests/integration/build.gradle.kts tests/integration/

# Line endings: a Windows checkout may bring CRLF, which breaks the shebang
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Sources last: changing code does not invalidate the dependency layer
COPY apps/backend/src apps/backend/src
COPY packages/shared/src packages/shared/src
RUN ./gradlew :apps:backend:bootJar -x test --no-daemon

# --- Runtime stage ---
# Alpine keeps the image well below the 500 MB budget of the base jammy JRE.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# curl is only needed by the container healthcheck, which calls /api/health
RUN apk add --no-cache curl \
    && adduser -D appuser
USER appuser

COPY --from=builder /workspace/apps/backend/build/libs/app.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
