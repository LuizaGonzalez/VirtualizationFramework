FROM amazoncorretto:21

WORKDIR /app

COPY target/*.jar app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
