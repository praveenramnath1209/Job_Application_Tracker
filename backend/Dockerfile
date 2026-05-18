# Multi-stage build for Spring Boot backend
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy POM first (layer caching for dependencies)
COPY pom.xml pom.xml

# Download dependencies (cached layer)
COPY src/ src/
RUN mvn clean package -DskipTests -q

# Runtime stage — slim JRE
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

RUN chown appuser:appgroup app.jar
USER appuser

# Optimized JVM flags for 512MB RAM constraint
ENV JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
