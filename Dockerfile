# Build the jar with Maven, then run it on a small Java 17 image.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# Render passes the port in $PORT; application.properties reads it.
CMD ["java", "-Xmx384m", "-jar", "app.jar"]
