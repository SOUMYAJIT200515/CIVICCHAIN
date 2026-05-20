# Stage 1: Build the Spring Boot application
FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /app

# Copy the Gradle wrapper and configurations
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle* ./
COPY src src

# Make the wrapper executable and build the project
RUN chmod +x gradlew
RUN ./gradlew bootJar -x test

# Stage 2: Create the lightweight runtime image
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Use a wildcard (*.jar) to grab your built file regardless of its exact name
COPY --from=builder /app/build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
