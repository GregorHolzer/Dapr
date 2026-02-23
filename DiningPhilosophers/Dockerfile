FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :DiningPhilosophers:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/DiningPhilosophers/build/libs/*.jar app.jar

ENV NUMBER_OF_PHILOSOPHERS=20
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]