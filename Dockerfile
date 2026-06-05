FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/ms-citizen-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
