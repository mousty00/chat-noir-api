# ── Build stage ────────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

ENV MAVEN_OPTS="-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US"

# Cache dependencies before copying source
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src

# Required by GraphQL code generator
RUN mkdir -p src/main/resources/graphql-client src/main/resources/graphql

RUN mvn clean package -DskipTests -B -q \
    -Dfile.encoding=UTF-8 \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8




# ── Layer extraction stage ──────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS layers

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Extract Spring Boot layers for optimal Docker layer caching
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted




# ── Runtime stage ───────────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime

LABEL maintainer="mousty00" \
      org.opencontainers.image.title="chat-noir-api" \
      org.opencontainers.image.description="Chat Noir REST + GraphQL API"

RUN apk add --no-cache curl

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

# Copy layers in dependency-cache-friendly order
COPY --from=layers --chown=spring:spring /app/extracted/dependencies/ ./
COPY --from=layers --chown=spring:spring /app/extracted/spring-boot-loader/ ./
COPY --from=layers --chown=spring:spring /app/extracted/snapshot-dependencies/ ./
COPY --from=layers --chown=spring:spring /app/extracted/application/ ./

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=50.0", \
    "-XX:+UseSerialGC", \
    "-XX:MaxMetaspaceSize=128m", \
    "-Xss512k", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "org.springframework.boot.loader.launch.JarLauncher"]
