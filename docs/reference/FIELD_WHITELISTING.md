# Field Whitelisting Security Implementation

## Overview

This document describes the WebDataBinder field whitelisting implementation to prevent mass assignment vulnerabilities (CWE-915) in the Price Rules AI Drools Service.

## Security Context

### OWASP Reference
- **A5:2021 – Security Misconfiguration**: Preventing unintended property binding
- **CWE-915**: Improperly Controlled Modification of Dynamically-Determined Object Attributes

### Attack Vectors Mitigated
1. **Mass Assignment**: Attackers attempting to set unauthorized fields
2. **Prototype Pollution**: Manipulation of object prototypes
3. **Class Injection**: Attempts to inject class loaders or runtime classes
4. **Privilege Escalation**: Setting admin flags or roles through request manipulation

## Implementation Architecture

### Core Component
- **Location**: `/src/main/java/com/example/pricerulesaidrools/security/config/WebDataBinderConfig.java`
- **Type**: `@ControllerAdvice` with global `@InitBinder`
- **Scope**: All `@RestController` annotated classes

### Security Configuration
```java
@ControllerAdvice(annotations = RestController.class)
public class WebDataBinderConfig {
    @InitBinder
    public void initBinder(WebDataBinder binder, NativeWebRequest webRequest, HandlerMethod handlerMethod) {
        // Controller-specific whitelisting
        // Auto-grow nested paths disabled
        binder.setAutoGrowNestedPaths(false);
    }
}
```

## Controller Field Whitelists

### RuleController (`/api/v1/drools`)

#### POST `/rules` - Deploy Rules
**Allowed Fields**: `name`, `description`, `content`, `version`, `metadata`
**DTO**: RuleRequest
**Rationale**: Only rule definition fields needed for deployment

#### PUT `/rules/{version}` - Update Rules
**Allowed Fields**: `name`, `description`, `content`, `version`, `metadata`
**DTO**: RuleRequest
**Rationale**: Same as deployment, ensures consistency

#### POST `/execute` - Execute Rules
**Allowed Fields**: `ruleSetId`, `facts`
**DTO**: RuleExecutionRequest
**Rationale**: Only execution context needed

#### POST `/validate` - Validate Rules
**Allowed Fields**: `name`, `description`, `content`, `version`, `metadata`
**DTO**: RuleRequest
**Rationale**: Validation needs full rule definition

### AuthController (`/auth`)

#### POST `/login` - User Authentication
**Allowed Fields**: `username`, `password`
**DTO**: LoginRequest
**Rationale**: Only credentials needed for authentication
**Security Note**: Prevents injection of roles or admin flags

#### POST `/signup` - User Registration
**Allowed Fields**: `username`, `email`, `password`, `firstName`, `lastName`, `roles`
**DTO**: SignupRequest
**Rationale**: Basic user information and requested roles
**Security Note**: Role assignment validated server-side

### FinancialMetricsController (`/api/v1/financial-metrics`)

#### POST `/calculate` - Calculate Metrics
**Allowed Fields**: `quoteId`, `customerId`, `monthlyPrice`, `durationInMonths`, `expectedDuration`, `customerType`, `subscriptionType`, `basePrice`, `customerTenureMonths`, `productId`
**DTO**: FinancialMetricsRequest
**Rationale**: All fields needed for accurate metric calculation

#### POST `/apply-strategy` - Apply Pricing Strategy
**Allowed Fields**: `quoteId`, `strategy`, `parameters`
**DTO**: PricingStrategyRequest
**Rationale**: Strategy identification and configuration

### AIRuleController (`/ai/rules`)

#### POST `/` - Create Rule with AI
**Allowed Fields**: `businessRequirement`, `ruleType`, `ruleName`, `testFacts`, `includeDocumentation`, `generateTestCases`, `tags`
**DTO**: RuleCreationRequest
**Rationale**: AI needs business context for rule generation

#### POST `/documentation-enhancement` - Enhance with Documentation
**Allowed Fields**: `topic`, `rulePattern`, `includeCodeExamples`, `includeBestPractices`, `context`
**DTO**: DocumentationEnhancementRequest
**Rationale**: Documentation retrieval parameters

### RuleTemplateController (`/api/v1/rule-templates`)

#### POST/PUT - Create/Update Templates
**Allowed Fields**: `name`, `description`, `category`, `templateContent`, `variables`, `tags`, `version`
**Rationale**: Template definition and metadata

#### POST `/generate` - Generate from Template
**Allowed Fields**: `templateId`, `variables`, `name`, `description`
**Rationale**: Template instantiation parameters

### RuleConflictController (`/api/v1/rule-conflicts`)

#### POST `/detect` - Detect Conflicts
**Allowed Fields**: `ruleSetIds`, `checkLevel`, `includeResolutions`
**Rationale**: Conflict detection scope and options

#### POST `/resolve` - Resolve Conflicts
**Allowed Fields**: `conflictId`, `resolutionStrategy`, `parameters`
**Rationale**: Conflict resolution configuration

### FinancialMetricsHistoryController (`/api/v1/metrics-history`)

#### POST `/record` - Record Metrics
**Allowed Fields**: `customerId`, `quoteId`, `metrics`, `timestamp`, `source`
**Rationale**: Historical tracking data

### RuleTestController (`/api/v1/rule-tests`)

#### POST `/run` - Run Tests
**Allowed Fields**: `ruleSetId`, `testCases`, `testData`, `expectedResults`, `options`
**Rationale**: Test execution configuration

#### POST `/create` - Create Test Case
**Allowed Fields**: `name`, `description`, `ruleSetId`, `inputData`, `expectedOutput`, `tags`
**Rationale**: Test case definition

## Globally Disallowed Fields

The following fields are explicitly disallowed across all controllers:

```java
"class"          // Prevents class injection
"Class"          // Case variant protection
"*.class"        // Nested class injection
"*.Class"        // Nested case variant
"classLoader"    // Prevents classloader manipulation
"*.classLoader"  // Nested classloader injection
```

## Security Best Practices

### 1. Principle of Least Privilege
- Each endpoint only accepts fields necessary for its operation
- No endpoint accepts internal fields like `id`, `createdAt`, `updatedAt` in creation requests

### 2. Nested Path Protection
```java
binder.setAutoGrowNestedPaths(false);
```
Prevents automatic creation of nested objects through path manipulation

### 3. Default Deny Strategy
- Unknown controllers receive empty whitelist (no fields allowed)
- GET/DELETE operations have empty whitelists (no body expected)

### 4. Audit Logging
- All whitelist configurations logged at DEBUG level
- Unknown controller access logged at WARN level
- Security violations logged for monitoring

## Testing Strategy

### Unit Tests
- Verify whitelisted fields are accepted
- Verify non-whitelisted fields are rejected
- Test each controller endpoint individually

### Security Tests
- Class injection prevention
- Prototype pollution prevention
- Nested path injection prevention
- Mass assignment attack prevention

### Test Location
`/src/test/java/com/example/pricerulesaidrools/security/WebDataBinderSecurityTest.java`

## Monitoring and Alerts

### Log Monitoring
Monitor for:
- `WARN` logs indicating unknown controller access
- Multiple failed binding attempts from same source
- Attempts to bind disallowed fields

### Metrics to Track
- Number of binding rejections per endpoint
- Frequency of unknown controller access
- Patterns in rejected field names

## Maintenance Guidelines

### Adding New Controllers
1. Add case in `WebDataBinderConfig.initBinder()`
2. Create controller-specific configuration method
3. Document allowed fields in this file
4. Add corresponding security tests

### Modifying Existing Whitelists
1. Review security implications
2. Update configuration method
3. Update this documentation
4. Update corresponding tests
5. Perform security review

### Security Review Checklist
- [ ] No internal fields exposed in creation endpoints
- [ ] No privilege escalation fields allowed
- [ ] Nested path growth disabled
- [ ] Class/classLoader fields blocked
- [ ] Tests verify both positive and negative cases

## Incident Response

### If Mass Assignment Detected
1. Check logs for source IP and user
2. Review attempted field injections
3. Verify no data corruption occurred
4. Update whitelist if legitimate field missing
5. Enhance monitoring for pattern

### If Unknown Controller Accessed
1. Verify if new controller added without configuration
2. Add appropriate whitelist configuration
3. Review default deny behavior worked correctly
4. Update documentation and tests

## References

- [OWASP Mass Assignment](https://owasp.org/www-community/vulnerabilities/Mass_Assignment)
- [CWE-915](https://cwe.mitre.org/data/definitions/915.html)
- [Spring Security Best Practices](https://docs.spring.io/spring-security/reference/features/exploits/index.html)
- [Spring WebDataBinder](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/WebDataBinder.html)

## Version History

| Version | Date | Changes | Author |
|---------|------|---------|--------|
| 1.0.0 | 2024-01-20 | Initial implementation | Security Team |

## Contact

For security concerns or questions about this implementation:
- Security Team: security@example.com
- Lead Developer: dev-lead@example.com