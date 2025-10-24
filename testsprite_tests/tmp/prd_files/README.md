# Price Rules AI Drools Service

A microservice that integrates with the Drools rule engine to provide dynamic pricing rule evaluation capabilities.

## Project Structure
```
price-rules-ai-drools-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.pricerulesaidrools/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── service/
│   │   │       └── PriceRulesAIDroolsApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── rules/
│   └── test/
│       └── java/
│           └── com.example.pricerulesaidools/
├── docker/
│   └── Dockerfile
├── .gitignore
├── .editorconfig
└── pom.xml
```

## Prerequisites
- Java 21+
- Drools 8.x
- Spring Boot 3.4.5
- Spring Cloud 2024.0.0
- Spring AI 0.1.0
- Lombok 1.18.28
- Maven 3.8+
- Docker (for containerization)

## Building the Project
```bash
mvn clean install
```

## Running the Application
```bash
# Run locally
mvn spring-boot:run

# Run with Docker
docker build -t price-dools-service .
docker run -p 8080:8080 price-dools-service
```

## API Documentation
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Spec: http://localhost:8080/v3/api-docs

## Configuration
The application can be configured through:
- `application.yml`
- Environment variables
- Command line arguments

## Monitoring
- Actuator endpoints available at `/actuator`
- Prometheus metrics at `/actuator/prometheus`
- Health checks at `/actuator/health`

## Security
- JWT authentication enabled
- Role-based access control
- Rate limiting configured

## Contributing
1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License
MIT License - see LICENSE file for details
