# Stripe-Lunar ETL Application

A Spring Boot backend API with Apache Camel for ETL (Extract, Transform, Load) operations from Stripe to an Oracle database.

## Overview

Stripe-Lunar is an ETL application that synchronizes data from Stripe to an Oracle database. It provides REST APIs for manual ETL operations and includes scheduled jobs for automatic synchronization, comprehensive job history tracking, and notification features.

## Features

- **Stripe Integration**: Fetches customer and payment data from Stripe API
- **Oracle Database Storage**: Persists data to Oracle database using JPA/Hibernate
- **Apache Camel Routes**: Provides REST endpoints and ETL processing pipelines
- **Scheduled Jobs**: Automatic daily, weekly, and monthly synchronization of data
- **Webhook Support**: Real-time updates via Stripe webhooks
- **Job History Tracking**: Comprehensive tracking of ETL job execution
- **Email Notifications**: Automated notifications for job completion and failures
- **CSV Export**: Export job history data to CSV format
- **Health Monitoring**: Dedicated health endpoint with detailed application status

## Technology Stack

- Java 21
- Spring Boot 3.2.1
- Apache Camel 4.3.0
- Stripe Java SDK 22.21.0
- Oracle Database 21.3 with JPA/Hibernate 6
- SpringDoc OpenAPI for API documentation
- Flyway for database migrations
- Docker for containerization
- Maven for dependency management

## Project Structure

```
stripe-lunar/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/lunar/stripelunar/
│   │   │       ├── component/       # Camel components and processors
│   │   │       ├── config/          # Application configuration
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── exception/       # Exception handling
│   │   │       ├── model/           # Entity models
│   │   │       ├── repository/      # Data repositories
│   │   │       ├── route/           # Camel routes
│   │   │       ├── service/         # Business logic
│   │   │       ├── util/            # Utility classes
│   │   │       └── StripeLunarApplication.java
│   │   └── resources/
│   │       ├── application.yml      # Main configuration
│   │       ├── application-prod.yml # Production configuration
│   │       └── db/migration/        # Flyway migration scripts
│   └── test/
├── init-scripts/                    # Database initialization scripts
├── Dockerfile                        # Docker configuration
├── docker-compose.yml               # Docker Compose configuration
└── pom.xml
```

## Setup and Configuration

### Prerequisites

- Java 21 or higher
- Maven 3.8 or higher
- Docker and Docker Compose (for containerized deployment)
- Oracle Database 21.3 (or compatible version)
- Stripe API credentials

### Configuration

The application uses YAML configuration files for different environments:

#### Main Configuration (application.yml)

```yaml
# Server configuration
server:
  port: 8080

# Database Configuration
spring:
  datasource:
    url: jdbc:oracle:thin:@//localhost:1521/XEPDB1
    username: stripe_lunar
    password: password

  # Flyway configuration
  flyway:
    baseline-on-migrate: true
    schemas: STRIPE_LUNAR
    create-schemas: true
    default-schema: STRIPE_LUNAR

# Stripe API configuration
stripe:
  api:
    key: your_stripe_api_key
  webhook:
    secret: your_stripe_webhook_secret
```

#### Production Configuration (application-prod.yml)

Contains production-specific settings with environment variable support:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:oracle:thin:@//db:1521/XEPDB1}
    username: ${DB_USERNAME:stripe_lunar}
    password: ${DB_PASSWORD:password}

  # Security settings
  security:
    user:
      name: ${ADMIN_USERNAME:admin}
      password: ${ADMIN_PASSWORD:admin}

stripe:
  api:
    key: ${STRIPE_API_KEY}
  webhook:
    secret: ${STRIPE_WEBHOOK_SECRET}
```

### Running the Application

#### Using Maven

```bash
# Development mode
mvn spring-boot:run

# Production mode
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

#### Using Docker

```bash
# Build and start the application with Oracle database
docker-compose up -d
```

The application will start on port 8080 by default.

## API Endpoints

### Customer Endpoints

- `GET /api/stripe/customers` - Get all customers
- `GET /api/stripe/customers/sync` - Sync customers from Stripe
- `GET /api/stripe/customers/{id}` - Get customer by ID
- `GET /api/stripe/customers/{customerId}/payments` - Get payments by customer ID

### Payment Endpoints

- `GET /api/stripe/payments` - Get all payments
- `GET /api/stripe/payments/sync` - Sync payments from Stripe
- `GET /api/stripe/payments/{id}` - Get payment by ID

### ETL Operations

- `GET /api/stripe/sync/all` - Sync all data from Stripe
- `GET /api/stripe/status` - Get ETL status
- `GET /api/etl/sync/all` - Manual sync of all Stripe data
- `GET /api/etl/status` - Check ETL status

### Webhook

- `POST /webhook/stripe` - Stripe webhook endpoint

## Scheduled Jobs

- Customer sync: Daily at midnight
- Payment sync: Daily at 1 AM
- Full sync: Every Sunday at 2 AM

## Development

### Building the Project

```bash
mvn clean install
```

## Testing

The Stripe-Lunar application includes a comprehensive test suite to ensure the reliability and correctness of the ETL operations, API endpoints, and business logic.

### Testing Approach

The application follows a layered testing approach:

- **Unit Tests**: Test individual classes and methods in isolation
- **Integration Tests**: Test the interaction between components
- **API Tests**: Verify the REST endpoints work as expected

### Testing Tools

- **JUnit 5**: Core testing framework
- **Mockito**: Mocking framework for isolating components
- **Spring Test**: Testing Spring Boot applications
- **TestETLMetricsProcessor**: Custom test double for ETL metrics testing

### Key Test Areas

1. **Service Layer Tests**
   - Customer and payment retrieval operations
   - Stripe API integration
   - Exception handling

2. **Controller Tests**
   - ETL controller operations
   - Health monitoring endpoints
   - Webhook processing
   - Response structure and status codes

3. **Component Tests**
   - ETL metrics processing
   - Error handling
   - Job history tracking

4. **Exception Handling Tests**
   - API error responses
   - Global exception handler
   - Custom exception types

### Best Practices

- **Arrange-Act-Assert Pattern**: Clear test structure
- **Descriptive Test Names**: Format of `methodName_condition_expectedBehavior`
- **Isolated Tests**: Fresh test objects for each test case
- **Proper Mocking**: Mock dependencies, not classes under test
- **Limited Reflection Usage**: Only when necessary for testing

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with coverage report
mvn verify

# Run specific test class
mvn test -Dtest=ClassName

# Run specific test method
mvn test -Dtest=ClassName#methodName
```

### Test Coverage

The project uses JaCoCo for code coverage reporting. After running tests with the `mvn verify` command, coverage reports are available in the `target/site/jacoco` directory.

```bash
# Generate and view the coverage report
mvn verify
open target/site/jacoco/index.html
```

## License

This project is proprietary and confidential.
