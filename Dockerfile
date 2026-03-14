# Stage 1: Build the app using pre-installed Maven
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
# Use standard 'mvn' to avoid wrapper missing errors
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:21-jre-jammy
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
# Keep memory usage low for the free tier; use shell form so $PORT is expanded
ENTRYPOINT java -Xmx300m -Xss512k -Dserver.port=${PORT:-8080} -jar app.jar