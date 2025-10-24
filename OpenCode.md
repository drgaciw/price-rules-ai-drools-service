# OpenCode.md

## Build, Lint, and Test Commands

### Building
- Clean and build: `mvn clean install`
- Skip tests: `mvn clean install -DskipTests`
- Run application:
  - Locally: `mvn spring-boot:run`
  - With profile: `mvn spring-boot:run -Dspring.profiles.active=dev`

### Testing
- All tests: `mvn test`
- Specific class: `mvn test -Dtest=TestClassName`
- Specific method: `mvn test -Dtest=TestClassName#testMethodName`
- Tests with coverage report: `mvn clean verify`

### Docker
- Build: `docker build -t price-dools-service -f docker/Dockerfile .`
- Run:
  - Simple container: `docker run -p 8080:8080 price-dools-service`
  - With environment variables (e.g., profiles, secrets, caches): [see `CLAUDE.md`](CLAUDE.md)

## Code Style Guidelines

### General Code Conventions
1. **Service Layer**:
   - Use interfaces for services.
   - Mandatory `@Transactional` annotation where required.
   - Always favor constructor injection over field injection (@RequiredArgsConstructor).

2. **Error Handling**:
   - Implement custom exception handling (e.g., `GlobalExceptionHandler`).
   - Ensure proper HTTP status mappings in controllers.

3. **Testing**:
- Adopt unit tests prepared (integration) :pattern as criteria (test-driven)
4 ensure perfect usability aligns-cli rules alignment success-properties