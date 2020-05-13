FROM openjdk:8-alpine

COPY target/uberjar/y-video-postgres-swagger.jar /y-video-postgres-swagger/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/y-video-postgres-swagger/app.jar"]
