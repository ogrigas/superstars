FROM openjdk:8-jre-alpine
COPY superstars.jar superstars.jar
ENTRYPOINT ["java", "-jar", "/superstars.jar"]
