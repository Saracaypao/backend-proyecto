# Multi-stage build para optimizar el tamaño de la imagen
FROM eclipse-temurin:21-jdk AS builder

# Instalar Maven
RUN apt-get update && apt-get install -y maven

# Crea un directorio para la app
WORKDIR /app

# Copia el código fuente
COPY backend/ ./backend/

# Compila el proyecto
RUN cd backend && mvn clean package -DskipTests

# Segunda etapa: imagen de runtime
FROM eclipse-temurin:21-jre

# Crea un directorio para la app
WORKDIR /app

# Copia el jar generado desde la etapa de build
COPY --from=builder /app/backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expón el puerto que usa Spring Boot
EXPOSE 8081

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
