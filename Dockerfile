FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Install Maven and build all dependencies for offline use
RUN apt-get update && \
    apt-get install -y maven && \
    mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the built artifact from the build stage
COPY --from=0 /app/target/*.jar app.jar

# Environment variables
ENV SPRING_PROFILES_ACTIVE=prod
ENV STRIPE_API_KEY=your_stripe_api_key
ENV STRIPE_WEBHOOK_SECRET=your_stripe_webhook_secret

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
