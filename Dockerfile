# ============================================================
# Stage 1 – Build
# eclipse-temurin:17 JDK + Maven 3.9.6 on Alpine
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy the Maven descriptor first so dependency resolution is
# cached as a separate layer – rebuilds only when pom.xml changes.
COPY pom.xml .
RUN mvn dependency:go-offline --no-transfer-progress -B

# Copy source and build the fat JAR (tests run separately in CI).
COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress -B

# ============================================================
# Stage 2 – Runtime
# Minimal JRE-only image keeps the final image small (~180 MB).
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Non-root user for security best practice.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the fat JAR produced by the build stage.
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the application port (matches server.port in application.properties).
EXPOSE 8080

# -XX:+UseContainerSupport  → JVM honours cgroup memory/cpu limits
# -XX:MaxRAMPercentage=75.0 → cap heap at 75 % of container RAM
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]
