FROM openjdk:21-jdk-slim
COPY target/cache-service.jar cache-service.jar
ENTRYPOINT ["java", "-jar", "/cache-service.jar"]
