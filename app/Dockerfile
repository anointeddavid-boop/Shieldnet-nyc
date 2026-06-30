# ShieldNet NYC - Benefits Eligibility API
# Multi-stage build: compile with Maven, run with a lightweight JRE

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user for security (best practice for production)
RUN addgroup -S shieldnet && adduser -S shieldnet -G shieldnet

COPY --from=build /build/target/benefits-eligibility-api-1.0.0.jar app.jar

RUN chown shieldnet:shieldnet app.jar
USER shieldnet

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
