FROM openjdk:17-jdk


RUN apk add --no-cache libsodium

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
COPY src/main/resources/static /app/static
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]
