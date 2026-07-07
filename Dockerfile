# =============================================================================
# ETAPA 1 — BUILD
# Usa Maven con JDK 17 para compilar y empaquetar la aplicación.
# Los tests se omiten aquí porque requieren MySQL/H2; el CI los ejecuta por separado.
# =============================================================================
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copiar el descriptor de dependencias primero para aprovechar el caché de capas.
# Si el pom.xml no cambia, Docker reutiliza esta capa en builds subsiguientes.
COPY pom.xml .
RUN mvn dependency:go-offline -B --quiet

# Copiar el código fuente y compilar el JAR ejecutable
COPY src ./src
RUN mvn -B clean package -DskipTests

# =============================================================================
# ETAPA 2 — RUNTIME
# Imagen mínima JRE (sin JDK ni Maven) para el contenedor final de producción.
# Reduce significativamente el tamaño de imagen.
# =============================================================================
FROM eclipse-temurin:17-jre-alpine

# Crear usuario no-root por seguridad (buenas prácticas en producción)
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar únicamente el JAR generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar al usuario no-privilegiado
USER spring

# Puerto por defecto de Spring Boot (configurable con variable PORT)
EXPOSE 8080

# Variables de entorno con valores por defecto (sobreescribibles en docker-compose)
ENV PORT=8080 \
    JAVA_OPTS="-Xms256m -Xmx512m"

# Punto de entrada: ejecutar el JAR con opciones JVM configurables
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
