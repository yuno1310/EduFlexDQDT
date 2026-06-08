
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jre-jammy
WORKDIR /app


RUN apt-get update && apt-get install -y libstdc++6 libc6 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar eduflex-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "eduflex-api.jar"]