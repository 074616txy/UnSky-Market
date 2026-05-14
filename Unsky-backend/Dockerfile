FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /workspace

COPY pom.xml .
COPY Unsky-common ./Unsky-common
COPY Unsky-backend ./Unsky-backend

RUN mvn -q -DskipTests package

FROM eclipse-temurin:11-jre

WORKDIR /app

COPY --from=build /workspace/Unsky-backend/target/Unsky-backend-1.0-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
