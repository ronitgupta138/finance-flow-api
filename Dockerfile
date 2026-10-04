# Stage 1: Build JAR with Maven & OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy dependency descriptor first for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Distroless / JRE 17 Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Security: Non-root user execution
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Dynamic port binding for Cloud Deployments (Render / Railway / Fly.io / AWS ECS)
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -jar app.jar"]
