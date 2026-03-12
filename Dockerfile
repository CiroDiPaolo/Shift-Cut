# ── Stage 1: Build ──────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copiamos primero el pom para aprovechar el cache de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y compilamos (sin tests para agilizar el build)
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Run ─────────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiamos el JAR generado en el stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto que expone la app Spring Boot
EXPOSE 8080

# Los valores reales son inyectados por docker-compose desde el archivo .env
# Estos son solo placeholders para que Spring Boot no falle si no se pasan
ENV SPRING_DATASOURCE_URL="" \
    SPRING_DATASOURCE_USERNAME="" \
    SPRING_DATASOURCE_PASSWORD="" \
    JWT_SECRET="" \
    SERVER_PORT=8080

ENTRYPOINT ["java", "-jar", "app.jar"]
