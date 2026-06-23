FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=build /build/target/ms-citizen-*.jar app.jar
EXPOSE 8084
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=8 \
  CMD wget -qO- http://127.0.0.1:8084/actuator/health 2>/dev/null | grep -q UP || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
