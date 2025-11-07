# Price Rules AI Drools Service

[![Java Version](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Drools](https://img.shields.io/badge/Drools-8.44.0-orange.svg)](https://www.drools.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A mission-critical microservice that integrates the Drools rule engine with Spring AI to provide intelligent, dynamic pricing rule evaluation and financial metrics calculations for both on-premise and SaaS software products.

## 🎯 Overview

This service acts as a bridge between business applications and the Drools rule engine, offering:
- **Dynamic Pricing Rules**: Sophisticated pricing strategies including volume-based, tiered, and subscription models
- **Financial Metrics**: ARR, TCV, ACV, CLV, and churn rate calculations
- **AI-Powered Intelligence**: Leverages Spring AI with OpenAI integration for semantic caching, rule routing, and optimization
- **Enterprise Security**: JWT authentication, field whitelisting, and role-based access control
- **Observability**: Comprehensive monitoring with Prometheus metrics and distributed tracing

## 🏗️ Architecture

```
price-rules-ai-drools-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/pricerulesaidrools/
│   │   │   ├── ai/               # AI integration (routing, semantic cache, embeddings)
│   │   │   ├── config/           # Application configuration
│   │   │   ├── controller/       # REST API endpoints
│   │   │   ├── drools/           # Drools engine integration
│   │   │   ├── model/            # Domain models
│   │   │   ├── repository/       # Data persistence layer
│   │   │   ├── security/         # Security configuration & filters
│   │   │   └── service/          # Business logic
│   │   └── resources/
│   │       ├── application.yml   # Application configuration
│   │       ├── rules/            # Drools rule definitions
│   │       └── db/changelog/     # Liquibase migrations
│   └── test/
│       ├── java/                 # Unit and integration tests
│       └── resources/            # Test configurations
├── docker/                       # Docker configuration
├── docs/                         # Documentation
│   ├── ai/                       # AI implementation guides
│   ├── product/                  # Product requirements
│   └── reports/                  # Project reports
└── pom.xml                       # Maven configuration
```

## 🚀 Quick Start

### Prerequisites

- **Java 21+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **PostgreSQL 14+** - For production data persistence
- **Redis 7+** - For caching and semantic vector store
- **Docker** - (Optional) For containerization

### Environment Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd price-rules-ai-drools-service
   ```

2. **Set up environment variables**
   ```bash
   export OPENAI_API_KEY=your-openai-api-key
   export JWT_SECRET=your-jwt-secret-key
   export JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/price_rules_db
   export JDBC_DATABASE_USERNAME=postgres
   export JDBC_DATABASE_PASSWORD=postgres
   export REDIS_HOST=localhost
   export REDIS_PORT=6379
   ```

3. **Start dependencies (PostgreSQL & Redis)**
   ```bash
   # Using Docker
   docker run -d --name postgres -e POSTGRES_DB=price_rules_db -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:14
   docker run -d --name redis -p 6379:6379 redis:7
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080/api`

## 📚 API Documentation

### Interactive Documentation
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8080/api/v3/api-docs

### Key Endpoints

#### Pricing Rules
- `POST /api/rules` - Create a new pricing rule
- `GET /api/rules/{id}` - Get rule by ID
- `PUT /api/rules/{id}` - Update existing rule
- `DELETE /api/rules/{id}` - Delete a rule
- `POST /api/rules/evaluate` - Evaluate rules against input data

#### Financial Metrics
- `POST /api/financial-metrics/calculate` - Calculate ARR, TCV, ACV, CLV
- `GET /api/financial-metrics/history` - Get calculation history

#### AI Features
- `POST /api/ai/route` - AI-powered request routing
- `POST /api/ai/parse` - Structured output parsing

#### Authentication
- `POST /api/auth/login` - User authentication
- `POST /api/auth/register` - User registration

#### Health & Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Application metrics
- `GET /actuator/prometheus` - Prometheus metrics endpoint

## 🔧 Configuration

### Application Profiles

The service supports multiple profiles:
- **dev** - Development environment with debug logging
- **test** - Test environment using H2 in-memory database
- **prod** - Production environment with optimized settings

Activate a profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Key Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `spring.ai.openai.api-key` | OpenAI API key for AI features | - |
| `spring.security.jwt.secret` | JWT signing secret | change-me-in-production |
| `spring.security.jwt.expiration` | JWT token expiration (ms) | 86400000 (24h) |
| `drools.rule-expiration` | Rule cache expiration (seconds) | 3600 |
| `spring.ai.redis.vector-store.similarity-threshold` | Semantic cache threshold | 0.85 |
| `financial-metrics.default-churn-rate` | Default monthly churn rate | 0.03 (3%) |

See `src/main/resources/application.yml` for complete configuration options.

## 🤖 AI Features

### Semantic Caching
- Uses Redis vector store with OpenAI embeddings (text-embedding-ada-002)
- Reduces duplicate AI queries with 85% similarity threshold
- Configurable TTL and cache size limits

### AI Routing
- Intelligent request routing based on deal type, complexity, and risk score
- Confidence-based routing with fallback mechanisms
- Supports multiple routing strategies with priority ordering

### Structured Output Parsing
- Parse unstructured text into structured domain models
- Supports JSON schema-based parsing
- Integrated with financial metrics calculations

## 🔐 Security

### Authentication & Authorization
- **JWT-based authentication** with configurable expiration
- **Role-based access control** (RBAC)
- **Field whitelisting** to prevent mass assignment vulnerabilities
- **CORS configuration** for cross-origin requests

### Best Practices
- Use strong JWT secrets in production (minimum 256 bits)
- Enable HTTPS in production environments
- Rotate JWT secrets periodically
- Configure rate limiting for public endpoints

## 🐳 Docker Deployment

### Build Docker Image
```bash
docker build -f docker/Dockerfile -t price-rules-ai-drools:latest .
```

### Run Container
```bash
docker run -d \
  --name price-rules-service \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e OPENAI_API_KEY=your-key \
  -e JDBC_DATABASE_URL=jdbc:postgresql://host:5432/db \
  -e REDIS_HOST=redis-host \
  price-rules-ai-drools:latest
```

## 📊 Monitoring & Observability

### Health Checks
```bash
curl http://localhost:8080/api/actuator/health
```

### Prometheus Metrics
The service exposes metrics at `/actuator/prometheus` for monitoring:
- HTTP request metrics
- JVM metrics (memory, threads, GC)
- Database connection pool metrics
- Redis cache metrics
- Custom business metrics

### Logging
- Structured logging with configurable levels
- Log rotation with 7-day retention
- Separate log files in `./logs/application.log`

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=FinancialMetricsControllerTest
```

### Test Coverage
Generate coverage report with JaCoCo:
```bash
mvn clean test jacoco:report
```
View report at `target/site/jacoco/index.html`

### Integration Tests
Uses Testcontainers for PostgreSQL integration tests:
```bash
mvn verify
```

## 🛠️ Technology Stack

### Core Technologies
- **Java 21** - LTS release with modern language features
- **Spring Boot 3.4.5** - Application framework
- **Spring AI 1.0.0-M6** - AI integration framework
- **Drools 8.44.0** - Business rules engine
- **PostgreSQL 42.7.1** - Relational database
- **Redis 4.0.0** - Cache & vector store

### Key Libraries
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Data persistence
- **Liquibase 4.26.0** - Database migrations
- **Lombok 1.18.36** - Boilerplate reduction
- **SpringDoc OpenAPI 2.3.0** - API documentation
- **Micrometer** - Metrics & observability
- **Testcontainers 1.19.3** - Integration testing

## 📖 Additional Documentation

- [Product Requirements Document](docs/product/PRD.md)
- [AI Implementation Guide](docs/ai/AI_SPEC.md)
- [Spring AI M6 Migration Guide](docs/ai/SPRING_AI_M6_MIGRATION_GUIDE.md)
- [Security Best Practices](docs/security/SECURITY.md)
- [Test Improvements Report](docs/TEST_IMPROVEMENTS_REPORT.md)

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java code conventions
- Use Lombok annotations where appropriate
- Write tests for new features
- Update documentation

### Code Quality
- Maintain test coverage above 80%
- No compiler warnings
- Pass all existing tests

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Support

For questions or issues:
- Create an issue in the repository
- Check existing documentation in `docs/`
- Review API documentation at `/swagger-ui.html`

## 🗺️ Roadmap

- [ ] GraphQL API support
- [ ] Real-time rule updates via WebSocket
- [ ] Advanced ML-based pricing optimization
- [ ] Multi-tenancy support
- [ ] Rule versioning and rollback
- [ ] Enhanced analytics dashboard

---

**Built with ❤️ using Spring Boot, Drools, and Spring AI**
