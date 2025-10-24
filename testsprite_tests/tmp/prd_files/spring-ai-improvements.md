# Product Requirements Document: Spring AI & Rules Engine Modernization

## Executive Summary

### Purpose
Modernize the price-rules-ai-drools-service to leverage the latest Spring Framework, Spring AI, and rules-engine capabilities, enabling AI-orchestrated pricing workflows with enhanced reliability, security, and performance.

### Objectives
1. Integrate Spring AI patterns (Routing, Chain workflows) for AI-augmented pricing decisions
2. Implement structured AI outputs feeding directly into Drools rule engine
3. Establish semantic caching with Redis for improved performance
4. Harden security posture addressing known CVEs
5. Implement observability and reliability patterns

### Success Metrics
- 95% of AI-assisted pricing runs leverage structured outputs without manual fallback
- <200ms latency improvement on repeated AI queries via semantic caching
- Zero critical security findings in quarterly scans
- Documented playbooks for prompt templates and AI workflow operations

## Background

### Current State
- Spring Boot 3.4.5 with Drools 8.44.0.Final
- Spring AI integration currently commented out (dependency issues)
- Manual AI response processing without structured output
- Limited caching capabilities
- Basic security configuration

### Problem Statement
The current implementation lacks modern AI orchestration patterns, structured output parsing, and semantic caching, resulting in:
- Inconsistent AI response handling
- Performance bottlenecks on repeated queries
- Manual intervention for AI output validation
- Security vulnerabilities in WebFlux and WebDataBinder configurations

## Functional Requirements

### FR-1: AI-Orchestrated Rule Workflows

#### FR-1.1: Routing Workflows
- **Description**: Implement Spring AI Routing to direct deal evaluations to specialized prompts
- **User Story**: As a pricing analyst, I want deal evaluations automatically routed to the appropriate specialized prompt (billing, technical, risk review) based on deal characteristics
- **Acceptance Criteria**:
  - Routing configuration for billing, technical, and risk review scenarios
  - Automatic prompt selection based on deal type, size, and complexity
  - Routing decisions logged for auditability
  - Fallback mechanism when routing confidence is low

#### FR-1.2: Chain Workflows
- **Description**: Implement Spring AI Chain workflows for sequential prompt reasoning
- **User Story**: As a system, I need to chain multiple AI prompts sequentially (base price → discount justification → compliance check) to build comprehensive pricing recommendations
- **Acceptance Criteria**:
  - Sequential prompt execution with context passing
  - Chain workflow for: base price calculation → discount justification → compliance verification
  - Intermediate results stored for debugging
  - Chain short-circuit on validation failures

#### FR-1.3: Workflow Documentation
- **Description**: Document routing and chain choices in architecture diagrams
- **Acceptance Criteria**:
  - Architecture diagrams showing workflow patterns
  - Code comments explaining routing/chain logic
  - Decision matrix for when to use routing vs. chain patterns

### FR-2: Structured AI Outputs for Drools

#### FR-2.1: BeanOutputParser Integration
- **Description**: Use Spring AI BeanOutputParser to convert LLM responses into typed DTOs
- **User Story**: As a developer, I want AI responses automatically parsed into Java objects that can be consumed by Drools
- **Acceptance Criteria**:
  - BeanOutputParser configured for pricing response DTOs
  - Type-safe conversion from LLM JSON to Java objects
  - Validation of parsed objects before Drools ingestion
  - Error handling for malformed AI responses

#### FR-2.2: DTO Extensions for AI Context
- **Description**: Extend existing DTOs to accept AI-enriched context
- **Acceptance Criteria**:
  - DTOs enhanced with fields for: churn indicators, revenue opportunities, risk scores
  - Backward compatibility with existing non-AI flows
  - Documentation of AI-specific fields

#### FR-2.3: Drools Validation Rules
- **Description**: Add Drools rules to validate AI-enriched inputs
- **Acceptance Criteria**:
  - Validation rules for required AI fields
  - Bounds checking for numeric AI predictions
  - Rejection of incomplete AI responses
  - Logging of validation failures

### FR-3: Prompt Quality Controls

#### FR-3.1: ChatOptions Configuration
- **Description**: Configure Top-K and Top-P sampling for deterministic vs. exploratory responses
- **User Story**: As a pricing manager, I want consistent AI responses for standard scenarios and creative responses for edge cases
- **Acceptance Criteria**:
  - ChatOptions profiles for: deterministic (low temperature), exploratory (high temperature)
  - Per-scenario configuration (volume discount, churn mitigation, commitment adjustments)
  - Configuration externalized to application.yml

#### FR-3.2: Prompt Template Management
- **Description**: Maintain versioned prompt templates with clear ownership
- **Acceptance Criteria**:
  - Templates for: volume discount, churn mitigation, commitment adjustments, upsell scenarios
  - Version control for templates
  - Owner assignment per template
  - Template testing framework

#### FR-3.3: Regression Testing for AI Outputs
- **Description**: Verify LLM outputs stay within expected variance
- **Acceptance Criteria**:
  - Automated tests for critical pricing scenarios
  - Variance thresholds configurable per scenario
  - Test failure alerts to template owners
  - Test results stored for trend analysis

### FR-4: Self-Consistency Pattern

#### FR-4.1: Multi-Inference Execution
- **Description**: Execute multiple LLM inferences with varied temperature for sensitive classifications
- **User Story**: As a risk analyst, I want high-confidence AI predictions for churn risk and upsell eligibility through majority voting
- **Acceptance Criteria**:
  - Multiple inferences (3-5) with temperature variation
  - Majority voting algorithm for final classification
  - Confidence score based on vote distribution
  - Applied to: churn risk, upsell eligibility, contract risk

#### FR-4.2: Auditability
- **Description**: Log intermediate results for self-consistency runs
- **Acceptance Criteria**:
  - All individual inference results logged
  - Vote distribution recorded
  - Final decision with confidence score
  - Retrieval capability for audit purposes

### FR-5: Semantic Caching with Redis

#### FR-5.1: VectorStore Provisioning
- **Description**: Provision Redis-backed VectorStore for semantic caching
- **Acceptance Criteria**:
  - Redis VectorStore configured for AI responses
  - Similarity threshold tuning for cache hits
  - Cache for common deal archetypes and customer questions

#### FR-5.2: Cache Observability
- **Description**: Add metrics for cache performance
- **Acceptance Criteria**:
  - Hit/miss rate metrics
  - Cache size monitoring
  - Alert thresholds for unhealthy cache (low hit rate)
  - Dashboard integration

#### FR-5.3: Data Retention & Privacy
- **Description**: Implement data retention policies
- **Acceptance Criteria**:
  - Configurable TTL for cached responses
  - PII scrubbing before caching
  - GDPR-compliant data deletion
  - Audit log for cache operations

### FR-6: Security Enhancements

#### FR-6.1: WebFlux Static Resource Protection
- **Description**: Fix CVE-2024-38821 static resource bypass vulnerability
- **Acceptance Criteria**:
  - WebFlux configuration hardened
  - Security tests verifying no bypass
  - Documentation of fix applied

#### FR-6.2: WebDataBinder Field Whitelisting
- **Description**: Enforce setAllowedFields on WebDataBinder
- **Acceptance Criteria**:
  - Explicit field whitelisting for all controllers
  - Rejection of non-whitelisted fields
  - Unit tests verifying whitelist enforcement

#### FR-6.3: Dependency Scanning
- **Description**: Schedule recurring dependency scans
- **Acceptance Criteria**:
  - Automated weekly scans for Spring AI, Redis, Drools
  - Critical vulnerability alerts
  - Remediation tracking

### FR-7: Automation & Tooling

#### FR-7.1: Spring AI Upgrade Automation
- **Description**: Use OpenRewrite recipe for Spring AI 1.0.0-M8+ upgrade
- **Acceptance Criteria**:
  - OpenRewrite recipe executed successfully
  - Deviations documented
  - Post-upgrade validation tests

#### FR-7.2: CI Pipeline Integration
- **Description**: Enhance CI with AI workflow tests
- **Acceptance Criteria**:
  - Semantic cache integration tests
  - AI workflow end-to-end tests
  - Performance regression tests

#### FR-7.3: Observability Hooks
- **Description**: Add distributed tracing and structured logs
- **Acceptance Criteria**:
  - Trace context propagation through AI workflows
  - Structured logs for: routing decisions, chain execution, cache hits
  - Integration with existing Prometheus metrics

## Non-Functional Requirements

### NFR-1: Performance
- AI query latency: <500ms for non-cached, <50ms for cached
- Cache hit rate: >70% for common scenarios
- System throughput: Support 1000 concurrent pricing evaluations

### NFR-2: Reliability
- AI service availability: 99.5%
- Graceful degradation when AI service unavailable
- Automatic retry with exponential backoff

### NFR-3: Scalability
- Horizontal scaling support for stateless services
- Redis cluster support for semantic cache
- Connection pooling for database and cache

### NFR-4: Security
- Zero critical CVEs in production dependencies
- JWT token expiration: 1 hour
- Rate limiting on AI endpoints: 100 req/min per user

### NFR-5: Maintainability
- Code coverage: >80% for new AI components
- Documentation: All AI workflows documented
- Runbook for common AI failure scenarios

## Technical Architecture

### Architecture Changes

#### Before
```
Controller → Service → Drools Engine → Database
                ↓
        Manual AI Integration (commented out)
```

#### After
```
Controller → Service → AI Orchestration Layer → Drools Engine → Database
                           ↓                           ↑
                    Spring AI (Routing/Chain)    Structured DTOs
                           ↓
                    Redis Semantic Cache
```

### Component Design

#### AI Orchestration Service
- **Responsibilities**: Route requests, execute chains, parse outputs
- **Interfaces**:
  - `AIOrchestrationService`
  - `RoutingStrategy`
  - `ChainExecutor`

#### Structured Output Service
- **Responsibilities**: Parse AI responses, validate, convert to Drools facts
- **Interfaces**:
  - `StructuredOutputParser`
  - `AIResponseValidator`

#### Semantic Cache Service
- **Responsibilities**: Cache AI responses, similarity search, eviction
- **Interfaces**:
  - `SemanticCacheService`
  - `VectorStoreManager`

### Data Models

#### AI-Enhanced DTOs
```java
@Data
public class AIEnrichedPricingRequest extends PricingRequest {
    private ChurnIndicator churnIndicator;
    private RevenueOpportunity revenueOpportunity;
    private RiskScore riskScore;
    private ConfidenceScore confidenceScore;
}

@Data
public class ChurnIndicator {
    private Double probability;
    private List<String> factors;
    private LocalDateTime calculatedAt;
}

@Data
public class RevenueOpportunity {
    private BigDecimal potentialValue;
    private String opportunityType;
    private Integer confidenceLevel;
}
```

#### Self-Consistency Result
```java
@Data
public class SelfConsistencyResult<T> {
    private T finalDecision;
    private Double confidenceScore;
    private List<T> individualResults;
    private Map<T, Long> voteDistribution;
}
```

## Implementation Plan

### Phase 1: Discovery (Week 1)
- Architecture review session
- Identify Drools-AI touchpoints
- Dependency audit and version selection
- Create detailed technical design

### Phase 2: Pilot (Weeks 2-3)
- Implement routing workflow for volume discount scenario
- Add structured output DTO for pricing response
- Unit tests for routing and parsing
- Integration test with Drools

### Phase 3: Scale-Out (Weeks 4-6)
- Expand to additional rule sets (churn, upsell, risk)
- Activate semantic caching with Redis
- Implement self-consistency for risk scoring
- Chain workflows for multi-step pricing

### Phase 4: Hardening (Weeks 7-8)
- Security audit and CVE remediation
- Performance tuning and load testing
- Observability integration
- Resilience testing (chaos engineering)

### Phase 5: Documentation & Training (Week 9)
- Playbooks for prompt templates
- Runbooks for AI workflow failures
- Cache operations documentation
- Team training sessions

## Dependencies

### External Dependencies
- Spring AI 1.0.1 (or latest stable)
- Redis 7.x with vector search capability
- OpenAI or compatible LLM API

### Internal Dependencies
- Existing Drools rule sets
- Financial metrics services
- Authentication/authorization services

### Third-Party Integrations
- OpenAI/Azure OpenAI for LLM
- Redis Cloud or self-hosted Redis
- Prometheus for monitoring

## Testing Strategy

### Unit Testing
- AI orchestration service mocking
- Structured output parser validation
- Cache service logic

### Integration Testing
- End-to-end AI workflow with test LLM
- Drools integration with AI-enriched DTOs
- Redis cache integration

### Performance Testing
- Load testing with 1000 concurrent requests
- Cache hit rate validation
- Latency measurements for cached vs. non-cached

### Security Testing
- CVE scanning
- WebFlux bypass attempts
- WebDataBinder field injection tests

### Chaos Testing
- AI service unavailability
- Redis cache failures
- Network latency injection

## Monitoring & Observability

### Metrics
- `ai.routing.decision.count{route}`
- `ai.chain.execution.duration{chain_name}`
- `ai.cache.hit.rate`
- `ai.cache.miss.count`
- `ai.output.validation.failures`
- `ai.self_consistency.confidence{scenario}`

### Alerts
- Cache hit rate < 50%
- AI response time > 1s
- Validation failure rate > 5%
- CVE detection on dependencies

### Dashboards
- AI Workflow Performance
- Semantic Cache Health
- Security Posture
- Cost Optimization

## Risks & Mitigations

### Risk 1: AI Response Inconsistency
- **Impact**: High
- **Probability**: Medium
- **Mitigation**: Self-consistency pattern, regression testing

### Risk 2: Cache Poisoning
- **Impact**: High
- **Probability**: Low
- **Mitigation**: Input validation, cache TTL, monitoring

### Risk 3: Spring AI Dependency Issues
- **Impact**: High
- **Probability**: Medium
- **Mitigation**: Version pinning, thorough testing, rollback plan

### Risk 4: Performance Degradation
- **Impact**: Medium
- **Probability**: Medium
- **Mitigation**: Load testing, caching, circuit breakers

### Risk 5: Security Vulnerabilities
- **Impact**: Critical
- **Probability**: Low
- **Mitigation**: Regular scanning, patching process, security reviews

## Open Questions

1. **Redis Deployment Model**: Managed (Redis Cloud) vs. self-hosted?
2. **LLM Provider**: OpenAI vs. Azure OpenAI vs. self-hosted?
3. **Governance**: Policies for storing/replaying AI responses?
4. **Enterprise Alignment**: Integration with broader AI standards?
5. **Budget**: Cost projections for LLM API calls and Redis?

## Success Criteria

### Phase Completion Criteria

#### Phase 1 (Discovery)
- [x] Architecture review completed
- [x] Technical design approved
- [x] Dependency versions selected

#### Phase 2 (Pilot)
- [ ] Single routing workflow functional
- [ ] Structured output parsing working
- [ ] Integration test passing

#### Phase 3 (Scale-Out)
- [ ] All rule sets AI-enabled
- [ ] Semantic cache operational
- [ ] Self-consistency implemented

#### Phase 4 (Hardening)
- [ ] Security audit clean
- [ ] Performance targets met
- [ ] Resilience tests passing

#### Phase 5 (Documentation)
- [ ] Playbooks published
- [ ] Team trained
- [ ] Runbooks tested

### Overall Success Metrics
- 95%+ structured output success rate
- <200ms cache latency improvement
- Zero critical CVEs
- Complete documentation suite
- 99.5% AI service availability

## Approval & Sign-Off

### Stakeholders
- **Product Owner**: TBD
- **Technical Lead**: TBD
- **Security Lead**: TBD
- **Operations Lead**: TBD

### Approval Status
- [ ] Product Owner Approved
- [ ] Technical Lead Approved
- [ ] Security Lead Approved
- [ ] Operations Lead Approved
- [ ] Budget Approved

## Appendices

### Appendix A: Glossary
- **BeanOutputParser**: Spring AI component for converting LLM JSON to Java objects
- **Routing Workflow**: AI pattern for directing requests to specialized prompts
- **Chain Workflow**: Sequential AI prompt execution with context passing
- **Self-Consistency**: Multiple AI inferences with majority voting
- **Semantic Cache**: Vector-based similarity search for cached responses

### Appendix B: References
- Spring AI Documentation: https://docs.spring.io/spring-ai/
- Drools Documentation: https://www.drools.org/
- Redis Vector Search: https://redis.io/docs/stack/search/
- CVE-2024-38821: Spring WebFlux Security Advisory

### Appendix C: Configuration Examples

#### Routing Configuration
```yaml
spring:
  ai:
    routing:
      strategies:
        - name: billing-review
          condition: "deal.type == 'ENTERPRISE' && deal.value > 100000"
          prompt-template: billing-review-template
        - name: technical-review
          condition: "deal.complexity == 'HIGH'"
          prompt-template: technical-review-template
```

#### Cache Configuration
```yaml
spring:
  redis:
    vector-store:
      similarity-threshold: 0.85
      max-cache-size: 10000
      ttl: 3600
```
