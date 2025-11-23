# # Use Maven image to build the application
FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .

RUN mvn dependency:go-offline

# Now copy the rest of the source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Use a smaller image to run the app
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
 COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
