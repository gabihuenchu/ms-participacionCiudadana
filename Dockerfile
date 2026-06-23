# Etapa 1: compilar con Maven + Java 21 (no requiere Java/Maven en tu PC)
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Etapa 2: imagen liviana solo con el JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/ms-citizen-*.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=8 \
  CMD wget -qO- http://127.0.0.1:8084/actuator/health 2>/dev/null | grep -q UP || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
