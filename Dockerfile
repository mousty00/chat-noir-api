# ── Build stage ────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

ENV MAVEN_OPTS="-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US"

COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mkdir -p src/main/resources/graphql-client src/main/resources/graphql

RUN mvn clean package -DskipTests -B -q \
    -Dfile.encoding=UTF-8 \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8


# ── Runtime stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine

RUN apk add --no-cache curl

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:${PORT:-8080}/api/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+UseZGC", \
    "-XX:+ZGenerational", \
    "-XX:+OptimizeStringConcat", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
