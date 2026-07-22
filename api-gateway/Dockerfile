# =========================================================
# STAGE 1: BUILD
# Purpose: compile the Java source code into a runnable .jar
# This stage is temporary — it will NOT be part of the final image
# =========================================================

# Start from an image that already has Maven + Java 25 installed
# "AS build" gives this stage a nickname so we can reference it later
FROM maven:3.9-eclipse-temurin-25 AS build

# Create and switch into a working folder inside the container
WORKDIR /app

# Copy only the pom.xml first (dependency list)
# Kept separate from source code copy for Docker layer caching efficiency
COPY pom.xml .

# Copy the actual Java source code into the container
COPY src ./src

# Run the Maven build inside the container, same as running it yourself
# -DskipTests skips tests here since this is a packaging step, not a test step
RUN mvn clean package -DskipTests


# =========================================================
# STAGE 2: RUN
# Purpose: create a small, clean image that only runs the app
# Nothing from Stage 1 (Maven, JDK, source code) carries over automatically
# =========================================================

# Start fresh from a lightweight image with just a Java Runtime (JRE), not a full JDK
FROM eclipse-temurin:25-jre-alpine

# Create and switch into a working folder in this new image
WORKDIR /app

# Copy ONLY the finished .jar file from Stage 1 (nicknamed "build") into this image
COPY --from=build /app/target/*.jar app.jar

# Documents which port this service listens on
# api-gateway runs on 8080
EXPOSE 8080

# The command that runs when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]
