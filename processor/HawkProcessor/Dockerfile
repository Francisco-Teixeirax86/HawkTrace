# Simple Dockerfile for Scala processor (matching Java collector style)
FROM sbtscala/scala-sbt:eclipse-temurin-jammy-21.0.2_13_1.9.8_2.13.12 AS build
WORKDIR /app
COPY . .
RUN sbt clean assembly

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/scala-2.13/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]