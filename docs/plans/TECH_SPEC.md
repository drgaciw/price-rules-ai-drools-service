# Price Rules AI Drools Service Technical Specification

## 1. System Architecture

### 1.1 Microservice Architecture

#### Core Components
- `PriceDroolsApplication`: Main Spring Boot application
- `RuleEngineService`: Manages Drools rule engine integration
- `PricingService`: Handles pricing calculations
- `CacheService`: Implements caching strategy
- `AuditService`: Manages audit logging

#### Layered Architecture
```
+-----------------------------+
|         Presentation        |
|  (REST Controllers)         |
+-----------------------------+
|          Service           |
| (Business Logic Layer)     |
+-----------------------------+
|         Repository         |
|  (Data Access Layer)       |
+-----------------------------+
|          Infrastructure     |
|  (External Services)       |
+-----------------------------+
```

### 1.2 Technology Stack

#### Core Technologies
- Spring Boot 3.4.5
- Java 21+
- Maven
- Docker

#### Integration Technologies
- Spring Integration Core
- Spring Cloud Stream
- Spring Kafka
- Spring Cloud Gateway

#### Database Technologies
- PostgreSQL
- Redis
- JPA/Hibernate

## 2. Component Design

### 2.1 Drools Integration Component

#### Interfaces
```java
public interface DroolsIntegrationService {
    // Rule Deployment
    RuleDeploymentResult deployRules(String ruleContent);
    RuleDeploymentResult updateRules(String ruleContent, String version);
    void undeployRules(String version);
    
    // Rule Validation
    RuleValidationResult validateRules(String ruleContent);
    RuleValidationResult validateRuleSet(String ruleSetId);
    
    // Rule Execution
    <T> T executeRules(String ruleSetId, Map<String, Object> facts);
    <T> List<T> executeBatchRules(String ruleSetId, List<Map<String, Object>> facts);
    
    // Rule Management
    RuleSetMetadata getRuleSetMetadata(String ruleSetId);
    List<RuleSetMetadata> listRuleSets();
    void reloadRuleSet(String ruleSetId);
    
    // Rule Monitoring
    RuleExecutionMetrics getRuleExecutionMetrics(String ruleSetId);
    RuleCacheMetrics getRuleCacheMetrics(String ruleSetId);
}
```

#### Implementation
- Uses Spring Integration for rule deployment
- Implements rule validation using Drools
- Uses Spring Data JPA for persistence
- Implements versioning using optimistic locking
- Implements distributed caching with Redis
- Uses Spring AI for rule optimization

### 2.2 Financial Metrics Integration

#### 2.2.1 Financial Metrics Data Model

```java
@Entity
@Table(name = "financial_metrics")
public class FinancialMetrics {
    @Id
    private String id;
    
    @Column(name = "quote_id")
    private String quoteId;
    
    @Column(name = "arr")
    private Double arr;  // Annual Recurring Revenue
    
    @Column(name = "tcv")
    private Double tcv;  // Total Contract Value
    
    @Column(name = "acv")
    private Double acv;  // Annual Contract Value
    
    @Column(name = "clv")
    private Double clv;  // Customer Lifetime Value
    
    @Column(name = "churn_score")
    private Integer churnScore;
    
    @Column(name = "discount_rate")
    private Double discountRate;
    
    @Column(name = "price_multiplier")
    private Double priceMultiplier;
    
    @Column(name = "min_commitment")
    private Double minCommitment;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```

#### 2.2.2 Financial Metrics Service

```java
public interface FinancialMetricsService {
    // Calculate all financial metrics for a quote
    FinancialMetrics calculateMetrics(Quote quote);
    
    // Get specific metric
    Double getARR(String quoteId);
    Double getTCV(String quoteId);
    Double getACV(String quoteId);
    Double getCLV(String quoteId);
    
    // Apply pricing strategies
    PricingResult applyPricingStrategy(Quote quote, PricingStrategy strategy);
    
    // Get historical metrics
    List<FinancialMetrics> getHistoricalMetrics(String customerId, Duration period);
    
    // Calculate churn risk
    Integer calculateChurnRisk(String customerId);
}

@Service
@RequiredArgsConstructor
public class FinancialMetricsServiceImpl implements FinancialMetricsService {
    private final FinancialMetricsRepository metricsRepository;
    private final CustomerRepository customerRepository;
    private final RuleEngineService ruleEngineService;
    
    @Override
    @Transactional
    public FinancialMetrics calculateMetrics(Quote quote) {
        // Implementation details...
    }
    
    // Other method implementations...
}
```

### 2.3 Financial Metrics API Endpoints

#### 1. Calculate Financial Metrics
```http
POST /api/v1/financial-metrics/calculate
Content-Type: application/json
Authorization: Bearer <token>

{
    "quoteId": "string",
    "customerId": "string",
    "monthlyPrice": "number",
    "durationInMonths": "integer",
    "expectedDuration": "integer",
    "customerType": "string",
    "subscriptionType": "string"
}

Response:
{
    "metrics": {
        "arr": "number",
        "tcv": "number",
        "acv": "number",
        "clv": "number",
        "churnScore": "integer",
        "discountRate": "number",
        "priceMultiplier": "number",
        "minCommitment": "number"
    },
    "pricing": {
        "basePrice": "number",
        "finalPrice": "number",
        "currency": "string",
        "appliedRules": ["string"]
    }
}
```

#### 2. Apply Pricing Strategy
```http
POST /api/v1/financial-metrics/apply-strategy
Content-Type: application/json
Authorization: Bearer <token>

{
    "quoteId": "string",
    "strategy": "VOLUME|VALUE|RISK_ADJUSTED",
    "parameters": {
        "minCommitment": "number",
        "discountTiers": [
            {"threshold": "number", "discount": "number"}
        ],
        "premiumTiers": [
            {"threshold": "number", "multiplier": "number"}
        ]
    }
}
```

### 2.4 Pre-Execution Rule Optimization

#### 2.4.1 AI-Enhanced Rule Loader

```java
public class AIEnhancedRuleLoader {
    private final DroolsIntegrationService droolsService;
    private final AIRuleOptimizationService aiService;
    private final OptimizationMetricsCollector metricsCollector;
    
    @Autowired
    public AIEnhancedRuleLoader(
        DroolsIntegrationService droolsService,
        AIRuleOptimizationService aiService,
        OptimizationMetricsCollector metricsCollector) {
        this.droolsService = droolsService;
        this.aiService = aiService;
        this.metricsCollector = metricsCollector;
    }
    
    /**
     * Loads rules with AI optimization applied.
     * 
     * @param ruleContent Original rule content
     * @param optimizationContext Context for optimization (rule type, business context)
     * @return KieSession with optimized rules loaded
     */
    public KieSession loadRulesWithAIOptimization(
            String ruleContent, 
            OptimizationContext optimizationContext) {
            
        // Track original rule metrics
        String ruleHash = DigestUtils.md5Hex(ruleContent);
        metricsCollector.recordOriginalRule(ruleHash, ruleContent);
        
        // Step 1: Check optimization cache
        Optional<String> cachedOptimizedRule = 
            aiService.getCachedOptimizedRule(ruleHash);
            
        if (cachedOptimizedRule.isPresent()) {
            return droolsService.loadRules(cachedOptimizedRule.get());
        }
        
        // Step 2: Send rules to AI service for optimization
        RuleOptimizationResult optimizationResult = 
            aiService.optimizeRules(ruleContent, optimizationContext);
        
        // Step 3: If improvements were suggested, apply them
        String optimizedRuleContent = optimizationResult.isOptimized() 
            ? optimizationResult.getOptimizedRules() 
            : ruleContent;
            
        // Step 4: Cache the optimization result
        if (optimizationResult.isOptimized()) {
            aiService.cacheOptimizedRule(ruleHash, optimizedRuleContent);
            metricsCollector.recordOptimization(
                ruleHash, 
                optimizationResult.getOptimizationMetrics());
        }
            
        // Step 5: Load the optimized rules into Drools
        return droolsService.loadRules(optimizedRuleContent);
    }
    
    /**
     * Asynchronous version for long-running optimizations
     */
    public CompletableFuture<KieSession> loadRulesWithAIOptimizationAsync(
            String ruleContent, 
            OptimizationContext optimizationContext) {
        return CompletableFuture.supplyAsync(() -> 
            loadRulesWithAIOptimization(ruleContent, optimizationContext));
    }
}
```

#### 2.4.2 Optimization Context

```java
public class OptimizationContext {
    private String ruleType;           // e.g., "PRICING", "DISCOUNT", "TAX"
    private String businessDomain;     // e.g., "FINANCE", "ENTERPRISE"
    private Set<String> optimizationCategories; // e.g., "PERFORMANCE", "LOGIC"
    private Integer optimizationLevel; // 1-5, with 5 being most aggressive
    private Map<String, Object> additionalContext;
    
    // Getters, setters, builder pattern implementation
}
```

#### 2.4.3 Rule Optimization API

```http
POST /api/v1/rules/optimize
Content-Type: application/json
Authorization: Bearer <token>

{
    "ruleContent": "string",
    "context": {
        "ruleType": "PRICING",
        "businessDomain": "FINANCE",
        "optimizationCategories": ["PERFORMANCE", "LOGIC"],
        "optimizationLevel": 3,
        "additionalContext": {
            "key": "value"
        }
    },
    "options": {
        "timeoutMs": 5000,
        "fallbackStrategy": "USE_ORIGINAL"
    }
}

Response:
{
    "optimized": true,
    "originalRuleHash": "string",
    "optimizedRuleContent": "string",
    "optimizationMetrics": {
        "optimizationTime": 1234,
        "improvementScore": 0.85,
        "changes": [
            {
                "type": "PERFORMANCE",
                "description": "Optimized condition ordering for early termination",
                "impact": "HIGH"
            }
        ]
    }
}
```

#### 2.4.4 Configuration

```yaml
drools:
  optimization:
    enabled: true
    default-mode: pre-execution  # pre-execution, background, manual
    service-url: http://ai-optimization-service/api/optimize
    timeout-ms: 5000
    fallback-strategy: use-original
    caching:
      enabled: true
      ttl-seconds: 3600
    async:
      enabled: true
      thread-pool-size: 5
```

### 2.5 Implementation Details

#### 2.5.1 Financial Metrics Calculation

```java
@Service
@RequiredArgsConstructor
public class FinancialMetricsCalculator {
    
    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;
    
    public FinancialMetrics calculateMetrics(Quote quote) {
        FinancialMetrics metrics = new FinancialMetrics();
        metrics.setQuoteId(quote.getId());
        
        // Calculate ARR (Annual Recurring Revenue)
        double arr = calculateARR(quote);
        metrics.setArr(arr);
        
        // Calculate TCV (Total Contract Value)
        double tcv = calculateTCV(quote);
        metrics.setTcv(tcv);
        
        // Calculate ACV (Annual Contract Value)
        double acv = calculateACV(quote);
        metrics.setAcv(acv);
        
        // Calculate CLV (Customer Lifetime Value)
        double clv = calculateCLV(quote);
        metrics.setClv(clv);
        
        // Calculate churn risk
        int churnScore = calculateChurnRisk(quote.getCustomerId());
        metrics.setChurnScore(churnScore);
        
        metrics.setCreatedAt(LocalDateTime.now());
        metrics.setUpdatedAt(LocalDateTime.now());
        
        return metrics;
    }
    
    private double calculateARR(Quote quote) {
        return quote.getMonthlyPrice() * 12;
    }
    
    private double calculateTCV(Quote quote) {
        return quote.getMonthlyPrice() * quote.getDurationInMonths();
    }
    
    private double calculateACV(Quote quote) {
        return (quote.getMonthlyPrice() * quote.getDurationInMonths()) / 12.0;
    }
    
    private double calculateCLV(Quote quote) {
        return quote.getMonthlyPrice() * quote.getExpectedDuration();
    }
    
    private int calculateChurnRisk(String customerId) {
        // Implementation using customer behavior data
        return 0; // 0-100 scale
    }
}
```

#### 2.4.2 Pricing Strategy Implementation

```java
@Service
@RequiredArgsConstructor
public class PricingStrategyServiceImpl implements PricingStrategyService {
    
    private final FinancialMetricsRepository metricsRepository;
    private final RuleEngineService ruleEngineService;
    
    @Override
    public PricingResult applyPricingStrategy(Quote quote, PricingStrategy strategy) {
        FinancialMetrics metrics = metricsRepository.findByQuoteId(quote.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Metrics not found"));
            
        switch (strategy.getType()) {
            case VOLUME:
                return applyVolumePricing(quote, metrics, strategy);
            case VALUE:
                return applyValuePricing(quote, metrics, strategy);
            case RISK_ADJUSTED:
                return applyRiskAdjustedPricing(quote, metrics, strategy);
            default:
                throw new UnsupportedOperationException("Unsupported pricing strategy");
        }
    }
    
    private PricingResult applyVolumePricing(Quote quote, FinancialMetrics metrics, PricingStrategy strategy) {
        double discount = calculateVolumeDiscount(metrics.getArr(), strategy);
        double finalPrice = quote.getBasePrice() * (1 - discount);
        
        PricingResult result = new PricingResult();
        result.setFinalPrice(finalPrice);
        result.setDiscountRate(discount);
        result.setAppliedStrategy("VOLUME");
        
        return result;
    }
    
    private double calculateVolumeDiscount(double arr, PricingStrategy strategy) {
        // Implementation based on ARR tiers
        return 0.0; // 0-1.0 discount rate
    }
    
    // Other strategy implementations...
}
```

### 2.5 Drools API Specification

#### 1. Rule Deployment API
```http
POST /api/v1/drools/rules
Content-Type: application/json
Authorization: Bearer <token>

{
    "name": "string",
    "description": "string",
    "content": "string",
    "version": "string",
    "metadata": {
        "author": "string",
        "createdDate": "string",
        "lastUpdated": "string"
    }
}

Response:
{
    "id": "string",
    "status": "DEPLOYED|PENDING|FAILED",
    "message": "string",
    "validationErrors": [
        {
            "code": "string",
            "message": "string",
            "severity": "ERROR|WARNING|INFO"
        }
    ]
}
```

#### 2. Rule Execution API
```http
POST /api/v1/drools/execute
Content-Type: application/json
Authorization: Bearer <token>

{
    "ruleSetId": "string",
    "facts": {
        "customer": {
            "id": "string",
            "type": "string",
            "segment": "string"
        },
        "product": {
            "id": "string",
            "category": "string",
            "price": "number"
        },
        "context": {
            "region": "string",
            "currency": "string",
            "timestamp": "string"
        }
    }
}

Response:
{
    "results": {
        "finalPrice": "number",
        "discount": "number",
        "rulesApplied": [
            {
                "id": "string",
                "name": "string",
                "priority": "number"
            }
        ],
        "executionTime": "number"
    },
    "metadata": {
        "cacheHit": "boolean",
        "ruleSetVersion": "string",
        "executionId": "string"
    }
}
```

#### 3. Rule Management API
```http
GET /api/v1/drools/rules/{id}
Authorization: Bearer <token>

Response:
{
    "id": "string",
    "name": "string",
    "version": "string",
    "status": "ACTIVE|INACTIVE|DELETED",
    "metadata": {
        "createdDate": "string",
        "lastUpdated": "string",
        "author": "string",
        "executionCount": "number"
    },
    "validationStatus": {
        "isValid": "boolean",
        "errors": [
            {
                "code": "string",
                "message": "string"
            }
        ]
    }
}
```

#### 4. Rule Monitoring API
```http
GET /api/v1/drools/metrics/{ruleSetId}
Authorization: Bearer <token>

Response:
{
    "executionMetrics": {
        "totalExecutions": "number",
        "averageExecutionTime": "number",
        "cacheHitRate": "number",
        "errorRate": "number"
    },
    "memoryUsage": {
        "current": "number",
        "peak": "number",
        "limit": "number"
    },
    "performance": {
        "throughput": "number",
        "latency": {
            "p50": "number",
            "p95": "number",
            "p99": "number"
        }
    }
}
```

### 2.3 Error Handling
```java
public class DroolsApiException extends RuntimeException {
    private final DroolsErrorCode errorCode;
    private final String ruleSetId;
    private final Map<String, Object> context;
}

public enum DroolsErrorCode {
    RULE_VALIDATION_FAILED,
    RULE_DEPLOYMENT_FAILED,
    RULE_EXECUTION_ERROR,
    RULE_NOT_FOUND,
    RULE_VERSION_CONFLICT,
    RULE_CACHE_ERROR,
    RULE_COMPILE_ERROR
}
```

### 2.4 Security Requirements
- JWT authentication required for all endpoints
- Role-based access control:
  - ADMIN: Full access
  - RULE_MANAGER: Rule deployment and management
  - RULE_EXECUTOR: Rule execution only
  - MONITOR: Monitoring access only

### 2.5 Performance Requirements
- Rule deployment: < 1s
- Rule validation: < 500ms
- Rule execution: < 200ms
- Batch execution: < 1s per 1000 rules
- Cache hit rate: > 90% for hot rules

### 2.6 Monitoring Metrics
```yaml
management:
  metrics:
    enable:
      all: true
    tags:
      application: price-dools-service
    distribution:
      percentiles-histogram:
        drools.rule.execution: true
        drools.rule.validation: true
        drools.cache.hit: true
      percentiles:
        drools.rule.execution: 0.95,0.99
        drools.rule.validation: 0.95,0.99
        drools.cache.hit: 0.95,0.99
```

### 2.7 Caching Strategy
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h
  drools:
    cache:
      enabled: true
      max-size: 1000
      ttl: 3600  # seconds
      eviction-policy: LRU
```

### 2.8 Configuration
```yaml
# Drools Configuration
drools:
  server:
    url: ${DROOLS_SERVER_URL:http://localhost:8080}
    timeout:
      connect: 5000  # ms
      read: 10000  # ms
    retry:
      max-attempts: 3
      delay: 1000  # ms
    security:
      enabled: true
      token-url: ${DROOLS_TOKEN_URL:http://localhost:8080/auth/realms/drools/protocol/openid-connect/token}
      client-id: ${DROOLS_CLIENT_ID}
      client-secret: ${DROOLS_CLIENT_SECRET}

# Rule Engine Configuration
rule-engine:
  validation:
    enabled: true
    strict: true
    timeout: 5000  # ms
  execution:
    batch-size: 1000
    timeout: 2000  # ms
    retry:
      max-attempts: 3
      delay: 500  # ms

# Monitoring Configuration
monitoring:
  enabled: true
  metrics:
    enabled: true
    collection-interval: 1000  # ms
    retention: 3600  # seconds
  alerts:
    enabled: true
    thresholds:
      execution-time: 200  # ms
      cache-hit-rate: 0.9
      error-rate: 0.01
```

### 2.2 Pricing Engine Component

#### Interfaces
```java
public interface PricingEngine {
    PricingResult evaluateSingle(PricingRequest request);
    List<PricingResult> evaluateBatch(List<PricingRequest> requests);
    void compileRules();
    void reloadRules();
}
```

#### Implementation
- Uses Drools Rule Engine
- Implements caching using Spring Cache
- Uses Redis for distributed caching
- Implements rule compilation and validation

### 2.3 Cache Management

#### Cache Configuration
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 hour
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=1h
```

#### Implementation
- Uses Spring Cache Abstraction
- Implements Redis as distributed cache
- Uses Caffeine for local caching
- Implements cache eviction strategies

## 3. API Design

### 3.1 Rule Management API

#### Endpoints
```http
POST /api/v1/rules - Create rule set
GET /api/v1/rules/{id} - Get rule set
PUT /api/v1/rules/{id} - Update rule set
DELETE /api/v1/rules/{id} - Delete rule set
GET /api/v1/rules - List all rule sets
```

#### Request/Response
```json
// Request
{
    "name": "string",
    "description": "string",
    "rules": [
        {
            "name": "string",
            "content": "string",
            "version": "string"
        }
    ]
}

// Response
{
    "id": "long",
    "name": "string",
    "description": "string",
    "rules": [
        {
            "id": "long",
            "name": "string",
            "version": "string",
            "status": "ACTIVE|INACTIVE"
        }
    ]
}
```

### 3.2 Pricing Evaluation API

#### Endpoints
```http
POST /api/v1/evaluate - Single evaluation
POST /api/v1/batch - Batch evaluation
```

#### Request/Response
```json
// Single Evaluation Request
{
    "productId": "string",
    "quantity": "integer",
    "context": {
        "customerType": "string",
        "region": "string",
        "discountCode": "string"
    }
}

// Batch Evaluation Request
[
    {
        "productId": "string",
        "quantity": "integer",
        "context": {...}
    }
]

// Response
{
    "productId": "string",
    "price": "double",
    "discount": "double",
    "finalPrice": "double",
    "rulesApplied": [
        "string"
    ]
}
```

## 4. Security Design

### 4.1 Authentication

#### JWT Configuration
```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days
```

### 4.2 Authorization

#### Role Definitions
```java
public enum Role {
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_READONLY
}
```

#### Permission Matrix
| Resource | Admin | User | Readonly |
|----------|-------|------|----------|
| Rule Management | ✅ | ✅ | ✅ |
| Rule Creation | ✅ | ✅ | ❌ |
| Rule Deletion | ✅ | ❌ | ❌ |
| Pricing Evaluation | ✅ | ✅ | ✅ |

## 5. Database Design

### 5.1 Entity Models

```java
@Entity
public class RuleSet {
    @Id
    private Long id;
    
    private String name;
    private String description;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Rule> rules;
    
    @Version
    private Long version;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}

@Entity
public class Rule {
    @Id
    private Long id;
    
    private String name;
    private String content;
    private String version;
    
    @Enumerated(EnumType.STRING)
    private RuleStatus status;
}
```

### 5.2 Database Indexes
```sql
CREATE INDEX idx_rule_set_name ON rule_set(name);
CREATE INDEX idx_rule_version ON rule(version);
CREATE INDEX idx_rule_status ON rule(status);
```

## 6. Monitoring and Logging

### 6.1 Metrics Configuration
```yaml
management:
  metrics:
    enable:
      all: true
    tags:
      application: price-dools-service
    distribution:
      percentiles-histogram:
        http.server.requests: true
        rule.evaluation.time: true
      percentiles:
        http.server.requests: 0.95,0.99
        rule.evaluation.time: 0.95,0.99
```

### 6.2 Logging Configuration
```yaml
logging:
  level:
    com.example.pricedools: DEBUG
    org.drools: INFO
    org.springframework: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 7. Deployment Configuration

### 7.1 Docker Configuration
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar
COPY docker/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 7.2 Kubernetes Configuration
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: price-dools-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: price-dools-service
  template:
    metadata:
      labels:
        app: price-dools-service
    spec:
      containers:
      - name: price-dools-service
        image: price-dools-service:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1"
```

## 8. Error Handling

### 8.1 Exception Hierarchy
```java
public class PriceDroolsException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String message;
}

public enum ErrorCode {
    RULE_VALIDATION_ERROR,
    RULE_COMPILATION_ERROR,
    RULE_EXECUTION_ERROR,
    CACHE_ERROR,
    CONFIGURATION_ERROR
}
```

### 8.2 Global Exception Handler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(PriceDroolsException.class)
    public ResponseEntity<ErrorResponse> handlePriceDroolsException(
            PriceDroolsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(ex.getErrorCode(), ex.getMessage()));
    }
}
```

## 9. Testing Strategy

### 9.1 Unit Tests
- Rule validation tests
- Pricing calculation tests
- Cache behavior tests
- Error handling tests

### 9.2 Integration Tests
- Rule deployment tests
- Pricing evaluation tests
- Cache integration tests
- Security tests

### 9.3 Performance Tests
- Rule compilation performance
- Pricing evaluation throughput
- Cache hit rate
- Memory usage

## 10. API Documentation

### 10.1 Swagger Configuration
```java
@Configuration
@EnableSwagger2WebMvc
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example.pricedools"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo());
    }
}
```

### 10.2 OpenAPI Specification
```yaml
openapi: 3.0.0
info:
  title: Price Drools Service API
  version: 1.0.0
  description: API for pricing rule evaluation

paths:
  /api/v1/rules:
    post:
      summary: Create a new rule set
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/RuleSet'

components:
  schemas:
    RuleSet:
      type: object
      properties:
        name:
          type: string
        description:
          type: string
        rules:
          type: array
          items:
            $ref: '#/components/schemas/Rule'
```
