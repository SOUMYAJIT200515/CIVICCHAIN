## Multi-stage Dockerfile for the CivicPulse Spring Boot backend

# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven descriptor first for better layer caching
COPY pom.xml ./

# Copy application source
COPY src ./src

# Package the application (skip tests for faster build)
RUN mvn -DskipTests clean package

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-slim
WORKDIR /app

# Copy the built jar from the build stage
# (Spring Boot produces exactly one jar in target/ for standard setups)
COPY --from=build /app/target/*.jar /app/app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run as non-root user (best practice)
RUN addgroup --system appgroup \
  && adduser --system --ingroup appgroup appuser
USER appuser

