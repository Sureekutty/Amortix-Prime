# -----------------------------------------------------------------------------
# AMORTIX | PRIME — Enterprise Multi-Stage Production Dockerfile
# Optimized for Render, Railway, Cloud Run, and Kubernetes
# -----------------------------------------------------------------------------

# Stage 1: Build with Maven and Temurin Java 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# Cache Maven dependencies
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copy source and package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Lightweight Production Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root secure user
RUN addgroup -S amortix && adduser -S amortix -G amortix
USER amortix

# Copy executable jar from builder
COPY --from=builder /workspace/target/*.jar app.jar

# Environment defaults (override in cloud portal)
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/amortix_prime_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
