FROM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY contracts/ contracts/
COPY employee-rest-api/ employee-rest-api/
WORKDIR /build/employee-rest-api
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/employee-rest-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]