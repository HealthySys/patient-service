FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY patient-service/pom.xml patient-service/
COPY record-service/pom.xml record-service/
COPY triage-service/pom.xml triage-service/
COPY notification-service/pom.xml notification-service/
COPY patient-service/src patient-service/src/
RUN mvn -pl patient-service -am package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/patient-service/target/patient-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]