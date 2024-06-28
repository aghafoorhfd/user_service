FROM openjdk:11-jre-slim
ENV TZ=Asia/Karachi
COPY build/libs/user_service-0.0.1-SNAPSHOT.jar /app/user_service.jar

WORKDIR /app

EXPOSE 8080

ENTRYPOINT java -jar user_service.jar
