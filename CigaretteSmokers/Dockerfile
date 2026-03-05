FROM gradle:8.14-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle :SleepingBarber:bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/SleepingBarber/build/libs/*.jar app.jar

EXPOSE 3000
ENTRYPOINT ["java", "-jar", "app.jar"]