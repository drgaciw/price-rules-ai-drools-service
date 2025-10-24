# Spring Improvements Plan

## Purpose
Capture prioritized actions that align the price-rules-ai-drools-service with the latest Spring Framework, Spring AI, and rules-engine capabilities.

## Key Objectives
- Modernize AI-enabled pricing workflows with current Spring AI patterns.
- Harden rule orchestration and supporting infrastructure for resilience and governance.
- Ensure secure, performant delivery of AI-augmented pricing experiences.

## Recommended Enhancements

### 1. AI-Orchestrated Rule Workflows
- Integrate Spring AI **Routing** workflows to direct deal evaluations toward specialized prompts (billing, technical, risk review).
- Adopt Spring AI **Chain** workflows so sequential prompts can reason over pricing data (e.g., base price → discount justification → compliance check).
- Document routing/chain choice in architecture diagrams and code comments for maintainability.

### 2. Structured AI Outputs Feeding Drools
- Use `BeanOutputParser` (Spring AI) to convert LLM responses into typed DTOs consumable by Drools facts.
- Extend existing DTOs in `com.example.pricerulesaidrools.dto` to accept AI-enriched context (e.g., churn indicators, revenue opportunities).
- Add validation rules in Drools to guard against incomplete or malformed AI responses.

### 3. Prompt Quality Controls
- Configure `ChatOptions` with **Top-K** and **Top-P** sampling for deterministic vs. exploratory responses, tuned per pricing scenario.
- Maintain per-use-case prompt templates (volume discount, churn mitigation, commitment adjustments) with clear owners.
- Introduce regression tests verifying LLM outputs stay within expected variance for critical scenarios.

### 4. Reliability via Self-Consistency
- Implement the **Self-Consistency** pattern: execute multiple LLM inferences with varied temperature and majority-vote the result for sensitive classifications (e.g., churn risk, upsell eligibility).
- Log intermediate results for auditability and to fine-tune temperature values.

### 5. Semantic Caching with Redis
- Provision a Redis-backed `VectorStore` for semantic caching of frequent AI calls (common deal archetypes, recurring customer questions).
- Add cache hit/miss metrics and alert thresholds to ensure cache health.
- Evaluate data retention policies to comply with privacy requirements.

### 6. Security Posture
- Review Spring WebFlux configurations guarding AI/rule dashboards to avoid CVE-2024-38821 static resource bypass.
- Enforce `setAllowedFields` on WebDataBinder usage to whitelist bindable fields.
- Schedule recurring dependency scans covering Spring AI and Redis integrations.

### 7. Automation & Tooling
- Leverage the OpenRewrite recipe for upgrading to Spring AI 1.0.0-M8+ when available, documenting deviations.
- Update CI pipelines to include semantic cache and AI workflow integration tests.
- Add observability hooks (distributed tracing, structured logs) around each AI-augmented rule evaluation.

## Sequencing & Ownership
1. **Discovery (Week 1)**
   - Architecture review, identify touchpoints between Drools and prospective AI components.
2. **Pilot (Weeks 2-3)**
   - Implement routing/chain workflows for a single pricing scenario; add structured output DTO.
3. **Scale-Out (Weeks 4-6)**
   - Expand to additional rule sets, activate semantic caching, integrate self-consistency for risk scoring.
4. **Hardening (Weeks 7-8)**
   - Security audits, performance tuning, observability and resilience tests.

## Success Criteria
- 95% of AI-assisted pricing runs leverage structured outputs without manual fallback.
- <200ms latency improvement on repeated AI queries due to semantic caching.
- Zero critical security findings related to Spring WebFlux or WebDataBinder in quarterly scans.
- Documented playbooks covering prompt templates, AI workflow fallback, and cache operations.

## Open Questions
- Preferred Redis deployment model (managed vs. self-hosted) and SLA expectations.
- Governance policies for storing and replaying AI responses.
- Alignment with broader enterprise AI standards and model selection roadmap.

## Next Steps
- Secure stakeholder sign-off on objectives and sequencing.
- Spike implementation for routing workflow plus structured output integration.
- Draft updates to `pom.xml` for any new Spring AI modules, pending dependency review.
