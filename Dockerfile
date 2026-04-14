FROM eclipse-temurin:18-jre

WORKDIR /app
COPY build/libs/java-course-1.0-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
