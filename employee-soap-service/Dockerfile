FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY contracts/ contracts/
COPY employee-soap-service/ employee-soap-service/
WORKDIR /build/employee-soap-service
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/employee-soap-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]