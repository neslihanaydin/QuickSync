FROM openjdk:25-jdk-oracle

WORKDIR /app

COPY target/gatewayservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
