FROM openjdk:17-jdk-buster

RUN apt-get update && apt-get install -y libsodium23 unzip
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/static /app/static
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
