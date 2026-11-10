# syntax=docker/dockerfile:1
# Multi-stage build: one Dockerfile, three stages.
#   tests   -> runs the automated tests (H2 in memory, no external database needed)
#   build   -> packages the executable jar
#   runtime -> lightweight JRE image that runs the API (default target)

# ---------- Stage 1: dependencies + tests ----------
FROM maven:3.9-eclipse-temurin-21 AS tests
WORKDIR /app
# Copy the pom first so the dependency layer is cached between builds.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
CMD ["mvn", "-B", "test"]

# ---------- Stage 2: build the jar ----------
FROM tests AS build
# Tests are executed in the "tests" stage (and in CI); here we only package.
RUN mvn -B -q package -DskipTests

# ---------- Stage 3: runtime ----------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app
# Run as an unprivileged user.
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/orders.jar app.jar
USER app
ENV SPRING_PROFILES_ACTIVE=dev \
    PORT=8080
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
  CMD wget -qO- http://localhost:8080/api/info || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
