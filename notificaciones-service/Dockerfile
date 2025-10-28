
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY . .

RUN mvn -f gymfex-common-events/pom.xml clean install -DskipTests

RUN mvn -f notificaciones-service/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/notificaciones-service/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
