
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src


RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends libstdc++6 libc6 curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /app/target/*.jar eduflex-api.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "eduflex-api.jar"]
