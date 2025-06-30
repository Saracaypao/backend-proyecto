# Usa una imagen base de Java
FROM eclipse-temurin:21-jdk

# Crea un directorio para la app
WORKDIR /app

# Copia el jar generado al contenedor
COPY target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expón el puerto que usa Spring Boot
EXPOSE 8081

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
