FROM maven:3.9-eclipse-temurin-25-alpine AS build

WORKDIR /app

ENV MAVEN_OPTS="-Dfile.encoding=UTF-8 -Duser.language=en -Duser.country=US"

COPY pom.xml .

RUN mvn dependency:go-offline -B

COPY src ./src

# Graphql directories
RUN mkdir -p src/main/resources/graphql-client \
    && mkdir -p src/main/resources/graphql

# explicit encoding
RUN mvn clean package -DskipTests -B -e \
    -Dfile.encoding=UTF-8 \
    -Dproject.build.sourceEncoding=UTF-8 \
    -Dproject.reporting.outputEncoding=UTF-8

FROM eclipse-temurin:25-jre-alpine AS runtime

RUN apk add --no-cache curl

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=build /app/target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]