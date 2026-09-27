FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/ai-assistant-service-1.0.0.jar app.jar

EXPOSE 8095

ENTRYPOINT ["java", "-jar", "app.jar"]