# Etapa de build con Maven + JDK
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copiamos todo el repo (necesario para poder compilar el módulo common)
COPY . .

# Instalamos la librería common en el repositorio local de maven
RUN mvn -f gymfex-common-events/pom.xml clean install -DskipTests

# Compilamos el servicio
RUN mvn -f notificaciones-service/pom.xml clean package -DskipTests

# Etapa runtime (JRE más ligera)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el jar generado del servicio
COPY --from=build /workspace/notificaciones-service/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
