FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

COPY . .

RUN mkdir -p src/main/resources/graphql-client

RUN mvn clean package -DskipTests -B -e

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/chat-noir-api-*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
