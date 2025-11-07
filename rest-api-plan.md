# REST API Comprehensive Plan
# Price Rules AI Drools Service

**Version:** 1.0
**Date:** 2025-01-24
**Status:** Draft for Implementation

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Current State Analysis](#2-current-state-analysis)
3. [Requirements Summary](#3-requirements-summary)
4. [API Design Principles](#4-api-design-principles)
5. [New API Specifications](#5-new-api-specifications)
6. [Implementation Roadmap](#6-implementation-roadmap)
7. [Security & Authentication](#7-security--authentication)
8. [Error Handling & i18n](#8-error-handling--internationalization)
9. [Testing Strategy](#9-testing-strategy)
10. [Monitoring & Observability](#10-monitoring--observability)
11. [Salesforce Integration Architecture](#11-salesforce-integration-architecture)
12. [Appendices](#12-appendices)

---

## 1. Executive Summary

### 1.1 Purpose
This document provides a comprehensive plan to expose all service methods via REST APIs for integration with:
- Internal web applications
- Third-party partner systems (primarily Salesforce CPQ/RCA)
- Other microservices

### 1.2 Critical Business Drivers
- **Immediate:** Enable Salesforce CPQ/RCA integration via Azure Functions
- **Immediate:** Provide CRUD and search capabilities for pricing rules
- **Short-term:** Expose AI routing and semantic caching capabilities
- **Medium-term:** Enable real-time updates via WebSocket

### 1.3 Key Metrics
- **Target Capacity:** 100 concurrent users, 1000 requests/second peak
- **Current Volume:** ~24 customers/day (1 per hour)
- **Growth Factor:** 40x capacity for future scaling
- **Data Retention:** 90-day rolling window with file system archive

### 1.4 Success Criteria
- ✅ Salesforce can trigger pricing evaluation in real-time
- ✅ Bidirectional rule sync between RCA and our service
- ✅ Natural language + structured search for pricing rules
- ✅ AI-powered conflict detection and resolution
- ✅ Real-time pricing updates via WebSocket
- ✅ Comprehensive API documentation for integration teams

---

## 2. Current State Analysis

### 2.1 Existing APIs (Fully Exposed)

| API Path | Service | Purpose | Status |
|----------|---------|---------|--------|
| `/api/v1/financial-metrics` | FinancialMetricsService | ARR, TCV, ACV, CLV calculations | ✅ Complete |
| `/api/v1/drools` | DroolsIntegrationService | Rule deployment and execution | ✅ Complete |
| `/api/v1/rule-templates` | RuleTemplateService | Generate rules from templates | ✅ Complete |
| `/api/v1/rule-conflicts` | RuleConflictService | Detect and resolve conflicts | ✅ Complete |
| `/ai/rules` | RuleCreationService | AI-assisted rule generation | ✅ Complete |
| `/api/auth` | AuthController | JWT authentication | ✅ Complete |

### 2.2 Services Not Yet Exposed

| Service | Package | Priority | Impact |
|---------|---------|----------|--------|
| AIRoutingService | ai.service | **CRITICAL** | Intelligent pricing routing |
| SemanticCacheService | ai.cache | **HIGH** | Performance optimization |
| SequentialThinkingService | ai.service | **MEDIUM** | Complex AI analysis |
| EmbeddingService | ai.cache | **MEDIUM** | Vector operations |
| PricingService | service | **HIGH** | Direct pricing operations |

### 2.3 Gaps Identified
- ❌ No CRUD endpoints for pricing rules (only AI generation exists)
- ❌ No search/query capabilities for rules
- ❌ No Salesforce-specific integration endpoints
- ❌ No WebSocket support for real-time updates
- ❌ No dedicated endpoints for rule synchronization
- ❌ No bulk import/export capabilities

---

## 3. Requirements Summary

### 3.1 Integration Requirements

**Primary Integrators:**
- ✅ Internal web application
- ✅ Salesforce CPQ/RCA (via Azure Functions)
- ✅ Other microservices

**Integration Pattern:**
- Azure Functions → OAuth2 client credentials → Our REST APIs
- Real-time synchronous calls (no batch operations)
- Bidirectional data sync with Salesforce RCA

### 3.2 Salesforce CPQ/RCA Requirements

**Data Flow TO Salesforce:**
- Pricing decisions and history
- Rule execution results
- Financial metrics (ARR, TCV, ACV, CLV)
- Automatically updated quote prices

**Data Flow FROM Salesforce:**
- Quote objects and line items
- Product/Price Book entries
- Customer identifiers (via custom CPQ objects)
- Pricing rules, actions, and conditions from RCA

**Master Data Source:** Salesforce CPQ/RCA (rules created/edited there)

### 3.3 Functional Requirements

**Pricing Rules:**
- ✅ Full CRUD operations
- ✅ Natural language + structured search
- ✅ Fuzzy search capabilities
- ✅ Search within DRL code content
- ✅ Version history tracking
- ✅ Conflict detection with AI-suggested resolutions
- ✅ Auto-prevent conflicting rule saves

**AI Features (All High Priority):**
- ✅ Real-time pricing routing (AIRoutingService)
- ✅ Semantic caching for performance
- ✅ AI-assisted rule generation
- ✅ Sequential thinking for complex analysis

**Real-time Capabilities:**
- ✅ WebSocket for rule execution completion
- ✅ WebSocket for pricing calculation results
- ✅ WebSocket for AI analysis progress
- ✅ Long-lived WebSocket connections

**Async Operations:**
- ✅ Complex sequential thinking analysis
- ✅ Report generation
- ✅ Webhook callbacks for completion

### 3.4 Non-Functional Requirements

**Performance:**
- 100 concurrent users
- 1000 requests/second peak capacity
- Long-lived WebSocket connections
- Response time < 200ms for read operations
- Response time < 2s for AI-powered operations

**Security:**
- OAuth2 client credentials flow for Azure Functions
- JWT authentication for all other clients
- Role-based access control (existing roles)
- No multi-tenancy (simple authorization model)

**Data Management:**
- 90-day rolling retention in active database
- File system archive after 90 days (offline, no API access)
- Archive: rules, metrics, execution history, audit logs, AI results

**Error Handling:**
- Verbose error messages with suggested fixes
- RFC 7807 Problem Details standard
- No stack traces (even in dev/staging)

**Internationalization:**
- Support top 5 languages: English, Mandarin, Hindi, Spanish, French
- Error messages only (not API docs)
- Accept-Language header driven

**Monitoring:**
- Grafana dashboards
- Splunk logging integration
- Prometheus metrics export

---

## 4. API Design Principles

### 4.1 REST Standards

**URL Structure:**
```
/api/v{version}/{resource}[/{id}][/{sub-resource}]

Examples:
/api/v1/pricing-rules
/api/v1/pricing-rules/{ruleId}
/api/v1/pricing-rules/{ruleId}/versions
/api/v1/pricing-rules/search
```

**HTTP Methods:**
- `GET` - Retrieve resource(s)
- `POST` - Create new resource
- `PUT` - Replace entire resource
- `PATCH` - Partial update resource
- `DELETE` - Remove resource

**Status Codes:**
- `200 OK` - Successful read/update
- `201 Created` - Successful creation
- `202 Accepted` - Async operation accepted
- `204 No Content` - Successful delete
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Missing/invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (e.g., rule conflicts)
- `422 Unprocessable Entity` - Validation failed
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error
- `503 Service Unavailable` - Service temporarily unavailable

### 4.2 Versioning Strategy

**URL Versioning:**
- Current: `/api/v1/`
- Future: `/api/v2/`
- Support N-4 versions (keep 5 versions active)

**Version Support Lifecycle:**
```
Version  | Status        | Support Until
---------|---------------|---------------
v1       | Current       | Active
v2       | Future        | Not yet released
v-1      | Deprecated    | 12 months
v-2      | Deprecated    | 6 months
v-3      | Deprecated    | 3 months
v-4      | End of Life   | 1 month notice
```

**Breaking Changes:**
- Require new major version
- Examples: Field removal, type changes, endpoint removal
- Announced 3 months in advance

**Non-Breaking Changes:**
- Can be added to existing version
- Examples: New fields, new endpoints, new optional parameters

### 4.3 Pagination

**Request Parameters:**
```
?page=0           # Page number (0-indexed)
&size=20          # Items per page
&sort=createdDate,desc  # Sort field and direction
```

**Response Format:**
```json
{
  "content": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  },
  "links": {
    "first": "/api/v1/resource?page=0&size=20",
    "last": "/api/v1/resource?page=7&size=20",
    "next": "/api/v1/resource?page=1&size=20",
    "prev": null
  }
}
```

### 4.4 Filtering & Search

**Structured Filtering:**
```
?status=ACTIVE
&type=VOLUME_DISCOUNT
&createdAfter=2024-01-01
&createdBefore=2024-12-31
```

**Natural Language Search:**
```
POST /api/v1/pricing-rules/search
{
  "query": "show me all volume discount rules for enterprise customers",
  "fuzzy": true,
  "searchInContent": true,
  "limit": 20
}
```

### 4.5 Rate Limiting

**Per-User Limits:**
- Read operations: 1000 requests/minute
- Write operations: 100 requests/minute
- AI operations: 50 requests/minute
- Search operations: 200 requests/minute

**Rate Limit Headers:**
```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 847
X-RateLimit-Reset: 1642694400
```

**Burst Allowance:**
- 20% over limit for 10 seconds
- Then enforce strict limit

**429 Response:**
```json
{
  "timestamp": "2025-01-24T10:30:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Please retry after 60 seconds.",
  "path": "/api/v1/pricing-rules/search",
  "retryAfter": 60
}
```

### 4.6 Content Negotiation

**Request Headers:**
```
Content-Type: application/json
Accept: application/json
Accept-Language: en-US,en;q=0.9,es;q=0.8
```

**Supported Formats:**
- JSON (primary)
- CSV (exports only)
- Excel (exports only)

---

## 5. New API Specifications

### 5.A Salesforce Integration APIs (PHASE 1 - CRITICAL)

**Base Path:** `/api/v1/salesforce`

#### 5.A.1 Rule Synchronization

##### POST /api/v1/salesforce/rules/sync
**Purpose:** Sync pricing rules from Salesforce RCA to Drools

**Authentication:** OAuth2 client credentials (Azure Functions)

**Request:**
```json
{
  "sourceSystem": "SALESFORCE_RCA",
  "syncMode": "INCREMENTAL|FULL",
  "rules": [
    {
      "externalId": "SF_RULE_001",
      "name": "Enterprise Volume Discount",
      "description": "10% discount for orders > $100k",
      "ruleType": "VOLUME_DISCOUNT",
      "priority": 100,
      "status": "ACTIVE",
      "conditions": [
        {
          "field": "totalAmount",
          "operator": "GREATER_THAN",
          "value": 100000
        },
        {
          "field": "customerTier",
          "operator": "EQUALS",
          "value": "ENTERPRISE"
        }
      ],
      "actions": [
        {
          "type": "APPLY_DISCOUNT",
          "discountType": "PERCENTAGE",
          "discountValue": 10.0
        }
      ],
      "effectiveDate": "2025-01-01T00:00:00Z",
      "expirationDate": "2025-12-31T23:59:59Z",
      "metadata": {
        "salesforceRecordId": "a1B2C3D4E5F6",
        "lastModifiedBy": "admin@company.com",
        "lastModifiedDate": "2025-01-20T15:30:00Z"
      }
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "syncId": "sync-uuid-12345",
  "timestamp": "2025-01-24T10:30:00Z",
  "results": {
    "total": 1,
    "created": 0,
    "updated": 1,
    "failed": 0,
    "skipped": 0
  },
  "ruleMapping": [
    {
      "externalId": "SF_RULE_001",
      "internalId": "rule-uuid-67890",
      "status": "UPDATED",
      "droolsRuleId": "drools-rule-001",
      "validationResult": {
        "valid": true,
        "conflicts": []
      }
    }
  ],
  "errors": []
}
```

**Business Logic:**
1. Validate incoming rules for conflicts
2. Convert RCA rule format to Drools DRL
3. Deploy to Drools engine
4. Return mapping of external→internal IDs
5. If conflicts detected, include AI-suggested resolutions

**Error Scenarios:**
- `409 Conflict` - Rule conflicts detected, cannot auto-resolve
- `422 Unprocessable Entity` - Invalid rule format
- `500 Internal Server Error` - Drools deployment failed

---

##### POST /api/v1/salesforce/rules/push
**Purpose:** Push our rule changes back to Salesforce RCA

**Request:**
```json
{
  "rules": [
    {
      "internalId": "rule-uuid-67890",
      "externalId": "SF_RULE_001",
      "changeType": "UPDATED|CREATED|DELETED",
      "ruleContent": {
        "name": "Enterprise Volume Discount - Updated",
        "description": "Updated: 15% discount for orders > $100k",
        "conditions": [...],
        "actions": [...]
      }
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "pushId": "push-uuid-12345",
  "timestamp": "2025-01-24T10:35:00Z",
  "results": {
    "total": 1,
    "success": 1,
    "failed": 0
  },
  "details": [
    {
      "internalId": "rule-uuid-67890",
      "externalId": "SF_RULE_001",
      "status": "SUCCESS",
      "salesforceRecordId": "a1B2C3D4E5F6",
      "salesforceResponse": {
        "success": true,
        "recordId": "a1B2C3D4E5F6"
      }
    }
  ]
}
```

---

#### 5.A.2 Quote Pricing Evaluation

##### POST /api/v1/salesforce/quotes/evaluate
**Purpose:** Evaluate pricing for Salesforce CPQ quote

**Request:**
```json
{
  "quoteId": "Q-001234",
  "accountId": "ACC-5678",
  "opportunityId": "OPP-9012",
  "customerId": "CUST-3456",
  "quoteLineItems": [
    {
      "lineItemId": "QLI-001",
      "productId": "PROD-ABC",
      "productName": "Enterprise License",
      "quantity": 100,
      "listPrice": 1200.00,
      "discount": 0,
      "totalAmount": 120000.00
    }
  ],
  "customerData": {
    "tier": "ENTERPRISE",
    "industry": "Technology",
    "annualRevenue": 50000000,
    "relationshipDuration": 36
  },
  "preferences": {
    "applyAIRouting": true,
    "calculateMetrics": true,
    "returnRuleDetails": true
  }
}
```

**Response:** `200 OK`
```json
{
  "evaluationId": "eval-uuid-11111",
  "timestamp": "2025-01-24T10:40:00Z",
  "quoteId": "Q-001234",
  "routing": {
    "selectedStrategy": "VOLUME_DISCOUNT",
    "confidence": 0.92,
    "reasoning": "High-volume enterprise customer with strong relationship"
  },
  "pricing": {
    "originalTotal": 120000.00,
    "finalTotal": 102000.00,
    "totalDiscount": 18000.00,
    "discountPercentage": 15.0
  },
  "appliedRules": [
    {
      "ruleId": "rule-uuid-67890",
      "ruleName": "Enterprise Volume Discount",
      "ruleType": "VOLUME_DISCOUNT",
      "discountApplied": 10.0,
      "reasoning": "Order value exceeds $100k threshold"
    },
    {
      "ruleId": "rule-uuid-22222",
      "ruleName": "Long-term Customer Loyalty",
      "ruleType": "LOYALTY_DISCOUNT",
      "discountApplied": 5.0,
      "reasoning": "Customer relationship > 3 years"
    }
  ],
  "financialMetrics": {
    "arr": 120000.00,
    "tcv": 360000.00,
    "acv": 120000.00,
    "clv": 540000.00,
    "churnRisk": 12.5
  },
  "recommendations": [
    "Consider additional 2% discount for multi-year commitment",
    "Strong customer profile - low churn risk"
  ],
  "cacheHit": false
}
```

**Business Logic:**
1. Extract customer and quote data
2. Use AIRoutingService to determine optimal pricing strategy
3. Execute relevant Drools rules
4. Calculate financial metrics
5. Check semantic cache for similar quotes
6. Return comprehensive pricing analysis

---

##### POST /api/v1/salesforce/quotes/update
**Purpose:** Update Salesforce CPQ quote with calculated pricing

**Request:**
```json
{
  "quoteId": "Q-001234",
  "evaluationId": "eval-uuid-11111",
  "updateMode": "AUTO|PREVIEW",
  "pricing": {
    "finalTotal": 102000.00,
    "lineItems": [
      {
        "lineItemId": "QLI-001",
        "finalPrice": 1020.00,
        "discountPercent": 15.0,
        "discountAmount": 180.00
      }
    ]
  }
}
```

**Response:** `200 OK`
```json
{
  "updateId": "update-uuid-33333",
  "timestamp": "2025-01-24T10:45:00Z",
  "quoteId": "Q-001234",
  "status": "SUCCESS",
  "updateMode": "AUTO",
  "salesforceResponse": {
    "success": true,
    "quoteRecordId": "0Q02C000000abcdEAA",
    "updatedFields": [
      "TotalPrice",
      "DiscountPercent",
      "QuoteLineItems"
    ]
  }
}
```

---

#### 5.A.3 Financial Metrics Sync

##### POST /api/v1/salesforce/metrics/push-to-rca
**Purpose:** Push financial metrics and pricing decisions to RCA

**Request:**
```json
{
  "syncType": "REAL_TIME|BATCH",
  "metrics": [
    {
      "customerId": "CUST-3456",
      "quoteId": "Q-001234",
      "evaluationId": "eval-uuid-11111",
      "timestamp": "2025-01-24T10:40:00Z",
      "financialMetrics": {
        "arr": 120000.00,
        "tcv": 360000.00,
        "acv": 120000.00,
        "clv": 540000.00,
        "churnRisk": 12.5
      },
      "pricingDecision": {
        "originalPrice": 120000.00,
        "finalPrice": 102000.00,
        "discountApplied": 15.0,
        "strategy": "VOLUME_DISCOUNT"
      },
      "ruleExecutions": [
        {
          "ruleId": "rule-uuid-67890",
          "ruleName": "Enterprise Volume Discount",
          "executed": true,
          "result": "APPLIED",
          "impact": 10.0
        }
      ]
    }
  ]
}
```

**Response:** `202 Accepted`
```json
{
  "syncJobId": "sync-job-uuid-44444",
  "status": "PROCESSING",
  "recordsReceived": 1,
  "estimatedCompletionTime": "2025-01-24T10:42:00Z",
  "webhookUrl": "/api/v1/salesforce/metrics/sync-status/sync-job-uuid-44444"
}
```

---

##### GET /api/v1/salesforce/metrics/sync-status/{syncJobId}
**Purpose:** Check status of metrics sync to RCA

**Response:** `200 OK`
```json
{
  "syncJobId": "sync-job-uuid-44444",
  "status": "COMPLETED",
  "startTime": "2025-01-24T10:40:00Z",
  "completionTime": "2025-01-24T10:41:30Z",
  "results": {
    "total": 1,
    "success": 1,
    "failed": 0
  },
  "details": [
    {
      "customerId": "CUST-3456",
      "quoteId": "Q-001234",
      "status": "SUCCESS",
      "rcaRecordId": "RCA-METRIC-12345"
    }
  ]
}
```

---

#### 5.A.4 Product & Price Book Sync

##### POST /api/v1/salesforce/products/sync
**Purpose:** Sync product and price book data from Salesforce

**Request:**
```json
{
  "syncMode": "INCREMENTAL|FULL",
  "products": [
    {
      "productId": "PROD-ABC",
      "productCode": "ENT-LIC-001",
      "name": "Enterprise License",
      "family": "Licenses",
      "description": "Annual enterprise software license",
      "listPrice": 1200.00,
      "pricebooks": [
        {
          "pricebookId": "PB-STANDARD",
          "pricebookName": "Standard Price Book",
          "price": 1200.00,
          "isActive": true
        },
        {
          "pricebookId": "PB-ENTERPRISE",
          "pricebookName": "Enterprise Price Book",
          "price": 1000.00,
          "isActive": true
        }
      ],
      "customFields": {
        "minimumQuantity": 10,
        "maximumDiscount": 20.0
      }
    }
  ]
}
```

**Response:** `200 OK`
```json
{
  "syncId": "product-sync-uuid-55555",
  "timestamp": "2025-01-24T11:00:00Z",
  "results": {
    "total": 1,
    "created": 0,
    "updated": 1,
    "skipped": 0
  },
  "productMapping": [
    {
      "externalProductId": "PROD-ABC",
      "internalProductId": "product-uuid-66666",
      "status": "UPDATED"
    }
  ]
}
```

---

### 5.B Pricing Rules CRUD + Search APIs (PHASE 1 - CRITICAL)

**Base Path:** `/api/v1/pricing-rules`

#### 5.B.1 Create Rule

##### POST /api/v1/pricing-rules
**Purpose:** Create new pricing rule

**Request:**
```json
{
  "name": "Q4 Holiday Promotion",
  "description": "Special holiday discount for all customers",
  "ruleType": "PROMOTIONAL_DISCOUNT",
  "priority": 50,
  "status": "DRAFT",
  "conditions": {
    "all": [
      {
        "field": "quote.date",
        "operator": "BETWEEN",
        "value": ["2025-12-01", "2025-12-31"]
      },
      {
        "field": "quote.totalAmount",
        "operator": "GREATER_THAN",
        "value": 5000.00
      }
    ]
  },
  "actions": [
    {
      "type": "APPLY_DISCOUNT",
      "discountType": "PERCENTAGE",
      "discountValue": 12.0
    }
  ],
  "effectiveDate": "2025-12-01T00:00:00Z",
  "expirationDate": "2025-12-31T23:59:59Z",
  "tags": ["holiday", "promotion", "q4"],
  "checkConflicts": true,
  "autoResolveConflicts": true
}
```

**Response:** `201 Created`
```json
{
  "ruleId": "rule-uuid-77777",
  "name": "Q4 Holiday Promotion",
  "status": "DRAFT",
  "version": 1,
  "createdBy": "user@company.com",
  "createdDate": "2025-01-24T11:30:00Z",
  "conflictCheck": {
    "hasConflicts": true,
    "conflicts": [
      {
        "conflictingRuleId": "rule-uuid-88888",
        "conflictingRuleName": "Black Friday Sale",
        "conflictType": "OVERLAPPING_CONDITIONS",
        "severity": "MEDIUM",
        "aiSuggestedResolution": {
          "approach": "PRIORITY_BASED",
          "suggestion": "Increase new rule priority to 60 to take precedence",
          "alternativeApproach": "Adjust date range to avoid overlap"
        }
      }
    ],
    "autoResolved": false
  },
  "droolsRuleId": null,
  "deploymentStatus": "NOT_DEPLOYED"
}
```

**Business Logic:**
1. Validate rule syntax and conditions
2. Check for conflicts with existing rules (if requested)
3. Use AI to suggest conflict resolutions
4. Create rule in DRAFT status (not deployed)
5. Generate version 1
6. Return detailed conflict information

**Error Responses:**
- `409 Conflict` - Unresolvable rule conflicts
- `422 Unprocessable Entity` - Invalid rule syntax

---

#### 5.B.2 Get Rule

##### GET /api/v1/pricing-rules/{ruleId}
**Purpose:** Retrieve rule by ID

**Query Parameters:**
- `version` (optional) - Specific version number, defaults to latest
- `includeHistory` (optional) - Include version history, default false
- `includeDRL` (optional) - Include generated DRL code, default false

**Response:** `200 OK`
```json
{
  "ruleId": "rule-uuid-77777",
  "externalId": null,
  "name": "Q4 Holiday Promotion",
  "description": "Special holiday discount for all customers",
  "ruleType": "PROMOTIONAL_DISCOUNT",
  "priority": 50,
  "status": "ACTIVE",
  "version": 3,
  "conditions": {...},
  "actions": [...],
  "effectiveDate": "2025-12-01T00:00:00Z",
  "expirationDate": "2025-12-31T23:59:59Z",
  "tags": ["holiday", "promotion", "q4"],
  "metadata": {
    "createdBy": "user@company.com",
    "createdDate": "2025-01-24T11:30:00Z",
    "lastModifiedBy": "admin@company.com",
    "lastModifiedDate": "2025-01-24T14:00:00Z",
    "executionCount": 1247,
    "lastExecuted": "2025-01-24T15:30:00Z"
  },
  "droolsRuleId": "drools-rule-q4-holiday",
  "deploymentStatus": "DEPLOYED",
  "versionHistory": [
    {
      "version": 1,
      "status": "DRAFT",
      "modifiedDate": "2025-01-24T11:30:00Z",
      "modifiedBy": "user@company.com",
      "changeDescription": "Initial creation"
    },
    {
      "version": 2,
      "status": "ACTIVE",
      "modifiedDate": "2025-01-24T12:00:00Z",
      "modifiedBy": "user@company.com",
      "changeDescription": "Activated and deployed"
    },
    {
      "version": 3,
      "status": "ACTIVE",
      "modifiedDate": "2025-01-24T14:00:00Z",
      "modifiedBy": "admin@company.com",
      "changeDescription": "Updated discount from 10% to 12%"
    }
  ],
  "droolsCode": "package com.example.rules;\n\nrule \"Q4 Holiday Promotion\"\n..."
}
```

---

#### 5.B.3 Update Rule

##### PUT /api/v1/pricing-rules/{ruleId}
**Purpose:** Update existing rule (creates new version)

**Request:**
```json
{
  "name": "Q4 Holiday Promotion - Extended",
  "description": "Extended holiday discount period",
  "priority": 55,
  "expirationDate": "2026-01-15T23:59:59Z",
  "changeDescription": "Extended promotion through mid-January",
  "checkConflicts": true,
  "autoRedeploy": true
}
```

**Response:** `200 OK`
```json
{
  "ruleId": "rule-uuid-77777",
  "version": 4,
  "previousVersion": 3,
  "status": "ACTIVE",
  "lastModifiedBy": "admin@company.com",
  "lastModifiedDate": "2025-01-24T16:00:00Z",
  "changeDescription": "Extended promotion through mid-January",
  "conflictCheck": {
    "hasConflicts": false
  },
  "redeploymentStatus": "SUCCESS",
  "droolsRuleId": "drools-rule-q4-holiday"
}
```

**Business Logic:**
1. Create new version (increment version number)
2. Keep previous versions in history
3. Check for conflicts if requested
4. Auto-redeploy to Drools if requested and status=ACTIVE
5. Maintain audit trail

---

#### 5.B.4 Delete Rule

##### DELETE /api/v1/pricing-rules/{ruleId}
**Purpose:** Soft delete rule (mark inactive, undeploy from Drools)

**Query Parameters:**
- `hardDelete` (optional) - Permanently delete, default false
- `force` (optional) - Force delete even if in use, default false

**Response:** `204 No Content`

**Business Logic:**
1. Default: Soft delete (set status=INACTIVE, undeploy from Drools)
2. If `hardDelete=true`: Remove from database (only if not referenced)
3. If `force=true`: Delete even if rule has execution history

**Error Responses:**
- `409 Conflict` - Rule is referenced by other entities and cannot be hard deleted
- `422 Unprocessable Entity` - Rule is currently active and in use

---

#### 5.B.5 Search Rules

##### POST /api/v1/pricing-rules/search
**Purpose:** Search rules using natural language or structured queries

**Request:**
```json
{
  "query": "find all volume discount rules created in last 30 days",
  "searchMode": "NATURAL_LANGUAGE|STRUCTURED|HYBRID",
  "fuzzy": true,
  "searchInContent": true,
  "filters": {
    "ruleType": ["VOLUME_DISCOUNT", "LOYALTY_DISCOUNT"],
    "status": ["ACTIVE", "DRAFT"],
    "createdAfter": "2024-12-25T00:00:00Z",
    "tags": ["enterprise"],
    "priority": {
      "min": 50,
      "max": 100
    }
  },
  "pagination": {
    "page": 0,
    "size": 20
  },
  "sort": {
    "field": "priority",
    "direction": "DESC"
  },
  "includeMetrics": true
}
```

**Response:** `200 OK`
```json
{
  "searchId": "search-uuid-99999",
  "query": "find all volume discount rules created in last 30 days",
  "interpretedQuery": {
    "intent": "FIND_RULES",
    "detectedFilters": {
      "ruleType": "VOLUME_DISCOUNT",
      "dateRange": "LAST_30_DAYS"
    }
  },
  "results": {
    "total": 15,
    "page": 0,
    "size": 20,
    "totalPages": 1,
    "content": [
      {
        "ruleId": "rule-uuid-11111",
        "name": "Enterprise Volume Tier 1",
        "ruleType": "VOLUME_DISCOUNT",
        "status": "ACTIVE",
        "priority": 90,
        "createdDate": "2025-01-10T09:00:00Z",
        "relevanceScore": 0.95,
        "matchedFields": ["name", "ruleType", "conditions"],
        "snippet": "...applies 15% discount when order total exceeds $50,000...",
        "executionMetrics": {
          "executionCount": 342,
          "lastExecuted": "2025-01-24T15:45:00Z",
          "averageExecutionTime": 45,
          "successRate": 99.7
        }
      }
    ]
  },
  "suggestions": [
    "Refine by customer tier: enterprise, mid-market, smb",
    "Filter by effectiveness: high-impact rules only"
  ],
  "semanticCacheHit": false
}
```

**Business Logic:**
1. Use AI (SequentialThinkingService) to interpret natural language query
2. Convert to structured filters
3. Search rule metadata AND DRL content if requested
4. Apply fuzzy matching if enabled
5. Rank results by relevance score
6. Check semantic cache for similar queries
7. Include execution metrics if requested

---

#### 5.B.6 List Rules

##### GET /api/v1/pricing-rules
**Purpose:** List rules with filtering and pagination

**Query Parameters:**
- `status` - Filter by status (ACTIVE, DRAFT, INACTIVE)
- `ruleType` - Filter by type
- `tags` - Filter by tags (comma-separated)
- `createdAfter`, `createdBefore` - Date range filters
- `page`, `size`, `sort` - Pagination parameters

**Response:** `200 OK` (standard paginated response)

---

#### 5.B.7 Get Rule Versions

##### GET /api/v1/pricing-rules/{ruleId}/versions
**Purpose:** Get complete version history for a rule

**Response:** `200 OK`
```json
{
  "ruleId": "rule-uuid-77777",
  "currentVersion": 4,
  "versions": [
    {
      "version": 1,
      "status": "DRAFT",
      "createdDate": "2025-01-24T11:30:00Z",
      "createdBy": "user@company.com",
      "changeDescription": "Initial creation",
      "snapshot": {
        "name": "Q4 Holiday Promotion",
        "priority": 50,
        "conditions": {...}
      }
    },
    {
      "version": 2,
      "status": "ACTIVE",
      "createdDate": "2025-01-24T12:00:00Z",
      "createdBy": "user@company.com",
      "changeDescription": "Activated and deployed",
      "changes": [
        {
          "field": "status",
          "oldValue": "DRAFT",
          "newValue": "ACTIVE"
        }
      ]
    }
  ]
}
```

---

#### 5.B.8 Revert to Version

##### POST /api/v1/pricing-rules/{ruleId}/revert
**Purpose:** Revert rule to a previous version

**Request:**
```json
{
  "targetVersion": 2,
  "reason": "Rolling back problematic changes from v3",
  "autoRedeploy": true
}
```

**Response:** `200 OK`
```json
{
  "ruleId": "rule-uuid-77777",
  "newVersion": 5,
  "revertedFromVersion": 4,
  "revertedToVersion": 2,
  "status": "ACTIVE",
  "redeploymentStatus": "SUCCESS"
}
```

---

#### 5.B.9 Deploy/Undeploy Rule

##### POST /api/v1/pricing-rules/{ruleId}/deploy
**Purpose:** Deploy rule to Drools engine

**Request:**
```json
{
  "validateFirst": true,
  "checkConflicts": true
}
```

**Response:** `200 OK`
```json
{
  "ruleId": "rule-uuid-77777",
  "deploymentStatus": "SUCCESS",
  "droolsRuleId": "drools-rule-q4-holiday",
  "timestamp": "2025-01-24T17:00:00Z",
  "validation": {
    "valid": true,
    "warnings": [],
    "errors": []
  },
  "conflicts": {
    "hasConflicts": false
  }
}
```

##### POST /api/v1/pricing-rules/{ruleId}/undeploy
**Purpose:** Undeploy rule from Drools (set status to INACTIVE)

**Response:** `200 OK`

---

#### 5.B.10 Bulk Operations

##### POST /api/v1/pricing-rules/bulk/activate
**Purpose:** Activate multiple rules at once

**Request:**
```json
{
  "ruleIds": ["rule-uuid-1", "rule-uuid-2", "rule-uuid-3"],
  "deploy": true
}
```

**Response:** `200 OK`
```json
{
  "total": 3,
  "success": 2,
  "failed": 1,
  "results": [
    {
      "ruleId": "rule-uuid-1",
      "status": "SUCCESS"
    },
    {
      "ruleId": "rule-uuid-2",
      "status": "SUCCESS"
    },
    {
      "ruleId": "rule-uuid-3",
      "status": "FAILED",
      "error": "Rule has unresolved conflicts"
    }
  ]
}
```

##### POST /api/v1/pricing-rules/bulk/deactivate
**Purpose:** Deactivate multiple rules

##### POST /api/v1/pricing-rules/bulk/delete
**Purpose:** Delete multiple rules

---

### 5.C AI Routing API (PHASE 1)

**Base Path:** `/api/v1/routing`

#### 5.C.1 Route Single Request

##### POST /api/v1/routing/route
**Purpose:** Route pricing request to optimal strategy

**Request:**
```json
{
  "requestId": "req-uuid-12345",
  "customer": {
    "customerId": "CUST-001",
    "tier": "ENTERPRISE",
    "industry": "Technology",
    "relationshipDuration": 48,
    "totalLifetimeValue": 2500000.00
  },
  "quote": {
    "quoteId": "Q-001",
    "totalAmount": 150000.00,
    "lineItemCount": 5,
    "productCategories": ["Software", "Services"]
  },
  "context": {
    "seasonality": "Q4",
    "competitivePressure": "HIGH",
    "dealSize": "LARGE",
    "urgency": "MEDIUM"
  },
  "preferences": {
    "includeReasoning": true,
    "includeAlternatives": true,
    "minConfidence": 0.75
  }
}
```

**Response:** `200 OK`
```json
{
  "routingId": "routing-uuid-11111",
  "timestamp": "2025-01-24T18:00:00Z",
  "requestId": "req-uuid-12345",
  "selectedRoute": {
    "routeName": "VOLUME_DISCOUNT_STRATEGY",
    "confidence": 0.89,
    "reasoning": "High-value enterprise customer with strong relationship history. Volume threshold exceeded. Low churn risk.",
    "expectedOutcome": {
      "recommendedDiscount": "12-15%",
      "expectedCloseRate": 0.85,
      "riskLevel": "LOW"
    }
  },
  "alternatives": [
    {
      "routeName": "RISK_ADJUSTED_STRATEGY",
      "confidence": 0.72,
      "reasoning": "Conservative approach given competitive pressure"
    },
    {
      "routeName": "VALUE_BASED_STRATEGY",
      "confidence": 0.68,
      "reasoning": "Focus on long-term value rather than discount"
    }
  ],
  "factors": {
    "positive": [
      "Strong customer relationship (48 months)",
      "High lifetime value ($2.5M)",
      "Enterprise tier qualification"
    ],
    "negative": [
      "High competitive pressure in market",
      "Q4 timing (end-of-year budget constraints)"
    ],
    "neutral": [
      "Medium deal urgency"
    ]
  },
  "cacheHit": false,
  "processingTime": 342
}
```

---

#### 5.C.2 Evaluate All Strategies

##### POST /api/v1/routing/evaluate-all
**Purpose:** Evaluate all available strategies and rank them

**Request:** (Same as route request)

**Response:** `200 OK`
```json
{
  "evaluationId": "eval-uuid-22222",
  "timestamp": "2025-01-24T18:05:00Z",
  "strategies": [
    {
      "rank": 1,
      "routeName": "VOLUME_DISCOUNT_STRATEGY",
      "confidence": 0.89,
      "pros": ["Best fit for customer profile", "Highest success rate"],
      "cons": ["May reduce margin"],
      "metrics": {
        "historicalSuccessRate": 0.87,
        "averageCloseRate": 0.85,
        "averageDiscount": 13.5
      }
    },
    {
      "rank": 2,
      "routeName": "RISK_ADJUSTED_STRATEGY",
      "confidence": 0.72,
      "pros": ["Conservative approach", "Maintains margins"],
      "cons": ["May lose to competitors"],
      "metrics": {...}
    }
  ],
  "recommendation": "VOLUME_DISCOUNT_STRATEGY",
  "processingTime": 567
}
```

---

#### 5.C.3 Get Available Routes

##### GET /api/v1/routing/routes
**Purpose:** List all configured routing strategies

**Response:** `200 OK`
```json
{
  "routes": [
    {
      "routeName": "VOLUME_DISCOUNT_STRATEGY",
      "description": "Optimize for high-volume deals with tiered discounts",
      "category": "VOLUME_BASED",
      "capabilities": [
        "Handles enterprise customers",
        "Supports multi-tier discounting",
        "Considers purchase history"
      ],
      "requirements": {
        "minimumDealSize": 50000.00,
        "customerTier": ["ENTERPRISE", "MID_MARKET"]
      },
      "active": true
    },
    {
      "routeName": "VALUE_BASED_STRATEGY",
      "description": "Focus on long-term customer value over discounts",
      "category": "VALUE_BASED",
      "capabilities": [...],
      "active": true
    }
  ]
}
```

---

#### 5.C.4 Routing Statistics

##### GET /api/v1/routing/statistics
**Purpose:** Get routing performance metrics

**Query Parameters:**
- `startDate`, `endDate` - Date range
- `routeName` (optional) - Filter by specific route

**Response:** `200 OK`
```json
{
  "period": {
    "startDate": "2025-01-01T00:00:00Z",
    "endDate": "2025-01-24T23:59:59Z"
  },
  "overall": {
    "totalRequests": 5432,
    "averageConfidence": 0.84,
    "averageProcessingTime": 423,
    "cacheHitRate": 0.23
  },
  "byRoute": [
    {
      "routeName": "VOLUME_DISCOUNT_STRATEGY",
      "requestCount": 2145,
      "percentage": 39.5,
      "averageConfidence": 0.87,
      "successRate": 0.89
    },
    {
      "routeName": "VALUE_BASED_STRATEGY",
      "requestCount": 1678,
      "percentage": 30.9,
      "averageConfidence": 0.82,
      "successRate": 0.85
    }
  ],
  "trends": {
    "mostUsedRoute": "VOLUME_DISCOUNT_STRATEGY",
    "highestConfidenceRoute": "VOLUME_DISCOUNT_STRATEGY",
    "fastestRoute": "RISK_ADJUSTED_STRATEGY"
  }
}
```

---

##### POST /api/v1/routing/statistics/reset
**Purpose:** Reset routing statistics

**Response:** `204 No Content`

---

#### 5.C.5 Configuration

##### GET /api/v1/routing/config
**Purpose:** Get current routing configuration

**Response:** `200 OK`
```json
{
  "confidenceThreshold": 0.75,
  "fallbackRoute": "STANDARD_PRICING",
  "enableCaching": true,
  "cacheTTL": 3600,
  "enableAIAnalysis": true,
  "maxRoutingTime": 5000
}
```

##### PUT /api/v1/routing/config
**Purpose:** Update routing configuration

**Request:**
```json
{
  "confidenceThreshold": 0.80,
  "fallbackRoute": "STANDARD_PRICING",
  "enableCaching": true
}
```

**Response:** `200 OK` (returns updated config)

---

### 5.D WebSocket Real-time APIs (PHASE 1)

**WebSocket Base URL:** `ws://localhost:8080/ws` or `wss://api.company.com/ws`

#### 5.D.1 Connection Protocol

**Connection Endpoint:** `/ws/v1/events`

**Authentication:**
```javascript
// Connect with JWT token
const ws = new WebSocket('wss://api.company.com/ws/v1/events?token=<JWT_TOKEN>');

// Or use Sec-WebSocket-Protocol header
const ws = new WebSocket('wss://api.company.com/ws/v1/events', ['authorization', '<JWT_TOKEN>']);
```

**Connection Message:**
```json
{
  "type": "CONNECTION",
  "status": "CONNECTED",
  "connectionId": "conn-uuid-12345",
  "timestamp": "2025-01-24T19:00:00Z",
  "supportedEvents": [
    "RULE_EXECUTION_COMPLETE",
    "PRICING_CALCULATION_COMPLETE",
    "AI_ANALYSIS_PROGRESS",
    "RULE_DEPLOYED",
    "RULE_CONFLICT_DETECTED"
  ]
}
```

---

#### 5.D.2 Subscribe to Events

**Subscribe Message:**
```json
{
  "action": "SUBSCRIBE",
  "events": [
    "RULE_EXECUTION_COMPLETE",
    "PRICING_CALCULATION_COMPLETE",
    "AI_ANALYSIS_PROGRESS"
  ],
  "filters": {
    "customerId": "CUST-001",
    "quoteId": "Q-001"
  }
}
```

**Subscription Acknowledgment:**
```json
{
  "type": "SUBSCRIPTION_CONFIRMED",
  "subscriptionId": "sub-uuid-67890",
  "events": ["RULE_EXECUTION_COMPLETE", "PRICING_CALCULATION_COMPLETE", "AI_ANALYSIS_PROGRESS"],
  "timestamp": "2025-01-24T19:01:00Z"
}
```

---

#### 5.D.3 Event Messages

**Rule Execution Complete:**
```json
{
  "type": "RULE_EXECUTION_COMPLETE",
  "eventId": "event-uuid-11111",
  "timestamp": "2025-01-24T19:05:00Z",
  "data": {
    "executionId": "exec-uuid-22222",
    "ruleId": "rule-uuid-77777",
    "ruleName": "Q4 Holiday Promotion",
    "status": "SUCCESS",
    "result": {
      "discountApplied": 12.0,
      "originalPrice": 120000.00,
      "finalPrice": 105600.00
    },
    "executionTime": 145
  }
}
```

**Pricing Calculation Complete:**
```json
{
  "type": "PRICING_CALCULATION_COMPLETE",
  "eventId": "event-uuid-33333",
  "timestamp": "2025-01-24T19:05:30Z",
  "data": {
    "quoteId": "Q-001",
    "evaluationId": "eval-uuid-44444",
    "status": "COMPLETED",
    "pricing": {
      "originalTotal": 120000.00,
      "finalTotal": 102000.00,
      "totalDiscount": 18000.00
    },
    "rulesApplied": 3,
    "processingTime": 567
  }
}
```

**AI Analysis Progress:**
```json
{
  "type": "AI_ANALYSIS_PROGRESS",
  "eventId": "event-uuid-55555",
  "timestamp": "2025-01-24T19:06:00Z",
  "data": {
    "analysisId": "analysis-uuid-66666",
    "analysisType": "SEQUENTIAL_THINKING",
    "progress": {
      "currentStep": 5,
      "totalSteps": 10,
      "percentage": 50,
      "currentThought": "Analyzing customer lifetime value patterns...",
      "estimatedCompletion": "2025-01-24T19:08:00Z"
    },
    "status": "IN_PROGRESS"
  }
}
```

**Rule Conflict Detected:**
```json
{
  "type": "RULE_CONFLICT_DETECTED",
  "eventId": "event-uuid-77777",
  "timestamp": "2025-01-24T19:07:00Z",
  "data": {
    "ruleId": "rule-uuid-88888",
    "ruleName": "New Volume Discount",
    "conflictsWith": [
      {
        "ruleId": "rule-uuid-99999",
        "ruleName": "Existing Volume Discount",
        "conflictType": "OVERLAPPING_CONDITIONS",
        "severity": "HIGH"
      }
    ],
    "suggestedResolution": {
      "approach": "MERGE_RULES",
      "details": "Combine both rules with priority-based execution"
    }
  }
}
```

---

#### 5.D.4 Unsubscribe

**Unsubscribe Message:**
```json
{
  "action": "UNSUBSCRIBE",
  "subscriptionId": "sub-uuid-67890"
}
```

**Unsubscribe Acknowledgment:**
```json
{
  "type": "UNSUBSCRIPTION_CONFIRMED",
  "subscriptionId": "sub-uuid-67890",
  "timestamp": "2025-01-24T19:10:00Z"
}
```

---

#### 5.D.5 Heartbeat

**Client Ping:**
```json
{
  "action": "PING"
}
```

**Server Pong:**
```json
{
  "type": "PONG",
  "timestamp": "2025-01-24T19:11:00Z"
}
```

**Heartbeat Interval:** 30 seconds
**Connection Timeout:** 60 seconds of inactivity

---

#### 5.D.6 Error Handling

**Error Message:**
```json
{
  "type": "ERROR",
  "errorId": "error-uuid-12345",
  "timestamp": "2025-01-24T19:12:00Z",
  "error": {
    "code": "SUBSCRIPTION_FAILED",
    "message": "Unable to subscribe to event: INVALID_EVENT_TYPE",
    "details": {
      "invalidEvent": "UNKNOWN_EVENT"
    }
  }
}
```

**Connection Close:**
```json
{
  "type": "CONNECTION_CLOSING",
  "reason": "CLIENT_REQUESTED",
  "timestamp": "2025-01-24T19:15:00Z"
}
```

---

### 5.E Semantic Cache API (PHASE 2)

**Base Path:** `/api/v1/cache`

#### 5.E.1 Search Cache

##### GET /api/v1/cache/search
**Purpose:** Search semantic cache for similar queries

**Query Parameters:**
- `query` - Query text (required)
- `threshold` - Similarity threshold (0.0-1.0, default 0.80)
- `limit` - Max results (default 10)

**Response:** `200 OK`
```json
{
  "query": "enterprise volume discount for orders over 100k",
  "matches": [
    {
      "cacheEntryId": "cache-uuid-11111",
      "originalQuery": "volume discount for large enterprise orders",
      "similarityScore": 0.94,
      "cachedResponse": {
        "pricing": {
          "discountPercent": 15.0,
          "reasoning": "Standard enterprise volume tier"
        },
        "rulesApplied": ["rule-uuid-12345"]
      },
      "metadata": {
        "cachedAt": "2025-01-24T10:30:00Z",
        "hitCount": 47,
        "lastAccessed": "2025-01-24T18:45:00Z"
      }
    }
  ],
  "searchTime": 23
}
```

---

#### 5.E.2 Add to Cache

##### POST /api/v1/cache
**Purpose:** Cache a query-response pair

**Request:**
```json
{
  "query": "pricing for mid-market customer with 50k deal",
  "response": {
    "pricing": {...},
    "routing": {...}
  },
  "metadata": {
    "source": "PRICING_EVALUATION",
    "customerId": "CUST-001"
  },
  "ttl": 3600
}
```

**Response:** `201 Created`
```json
{
  "cacheEntryId": "cache-uuid-22222",
  "embedding": [0.123, -0.456, ...],
  "timestamp": "2025-01-24T19:30:00Z",
  "expiresAt": "2025-01-24T20:30:00Z"
}
```

---

#### 5.E.3 Statistics

##### GET /api/v1/cache/statistics
**Purpose:** Get cache performance metrics

**Response:** `200 OK`
```json
{
  "overall": {
    "totalEntries": 1547,
    "totalHits": 8923,
    "totalMisses": 3421,
    "hitRate": 0.723,
    "averageSimilarity": 0.87,
    "cacheSize": "245 MB",
    "maxSize": "1 GB"
  },
  "performance": {
    "averageSearchTime": 18,
    "averageEmbeddingTime": 54,
    "p95SearchTime": 35,
    "p99SearchTime": 67
  },
  "topQueries": [
    {
      "query": "enterprise volume discount",
      "hitCount": 342,
      "avgSimilarity": 0.89
    }
  ]
}
```

---

#### 5.E.4 Clear Cache

##### DELETE /api/v1/cache
**Purpose:** Clear entire cache

**Response:** `200 OK`
```json
{
  "cleared": true,
  "entriesRemoved": 1547,
  "timestamp": "2025-01-24T20:00:00Z"
}
```

##### DELETE /api/v1/cache/{cacheEntryId}
**Purpose:** Delete specific cache entry

**Response:** `204 No Content`

---

### 5.F AI Thinking API (PHASE 2)

**Base Path:** `/api/v1/ai/thinking`

#### 5.F.1 Process Thought

##### POST /api/v1/ai/thinking/process
**Purpose:** Process single sequential thought

**Request:**
```json
{
  "thought": "Analyze customer lifetime value for enterprise segment",
  "context": "Customer has 48-month relationship, $2.5M total revenue",
  "thoughtNumber": 1,
  "totalThoughts": 5,
  "previousThoughts": []
}
```

**Response:** `200 OK`
```json
{
  "thoughtId": "thought-uuid-11111",
  "thoughtNumber": 1,
  "result": "Customer shows strong value profile: Long relationship (4 years) with high LTV ($2.5M). Low churn risk indicators.",
  "nextThought": "Evaluate competitive positioning and price sensitivity",
  "completed": false,
  "confidence": 0.88,
  "processingTime": 1234
}
```

---

#### 5.F.2 Execute Thought Sequence

##### POST /api/v1/ai/thinking/sequence
**Purpose:** Execute complete sequential thinking analysis

**Request:**
```json
{
  "initialThought": "Should we offer volume discount or value-based pricing?",
  "totalThoughts": 10,
  "context": {
    "customer": {...},
    "deal": {...},
    "market": {...}
  }
}
```

**Response:** `200 OK`
```json
{
  "sequenceId": "seq-uuid-22222",
  "thoughts": [
    {
      "thoughtNumber": 1,
      "thought": "Analyzing customer profile...",
      "result": "Strong enterprise customer with 4-year relationship"
    },
    {
      "thoughtNumber": 2,
      "thought": "Evaluating deal size...",
      "result": "Large deal ($150K) exceeds volume discount threshold"
    }
  ],
  "finalConclusion": "Recommend volume discount strategy with 12-15% discount range. High confidence in customer acceptance.",
  "confidence": 0.91,
  "reasoning": [
    "Customer tier and relationship support higher discount",
    "Deal size justifies volume-based approach",
    "Low competitive threat in this segment"
  ],
  "totalProcessingTime": 8934
}
```

---

#### 5.F.3 Analyze Business Requirement

##### POST /api/v1/ai/thinking/analyze
**Purpose:** Analyze business requirement for complexity

**Request:**
```json
{
  "requirement": "Create a rule that applies tiered discounts based on annual contract value, with additional bonuses for multi-year commitments",
  "domain": "PRICING_RULES"
}
```

**Response:** `200 OK`
```json
{
  "analysisId": "analysis-uuid-33333",
  "requirement": "Create a rule that applies tiered discounts...",
  "breakdown": {
    "components": [
      "Tiered discount logic",
      "Annual contract value calculation",
      "Multi-year commitment detection",
      "Bonus calculation mechanism"
    ],
    "dependencies": [
      "Customer contract data",
      "Product pricing information",
      "Commitment term definitions"
    ],
    "complexity": "MEDIUM",
    "estimatedEffort": "4-6 hours"
  },
  "suggestions": [
    "Define specific discount tiers (e.g., <$50K: 5%, $50K-$100K: 10%, >$100K: 15%)",
    "Clarify multi-year bonus structure",
    "Consider edge cases: partial years, contract renewals"
  ],
  "risks": [
    "Overlapping discount rules may cause conflicts",
    "Multi-year calculations may need special handling"
  ],
  "recommendedApproach": "Start with basic tiered structure, then add multi-year logic as separate enhancement"
}
```

---

### 5.G Embeddings API (PHASE 2)

**Base Path:** `/api/v1/embeddings`

#### 5.G.1 Generate Embedding

##### POST /api/v1/embeddings/generate
**Purpose:** Generate embedding vector for text

**Request:**
```json
{
  "text": "enterprise volume discount for large orders",
  "model": "text-embedding-ada-002"
}
```

**Response:** `200 OK`
```json
{
  "embedding": [0.0123, -0.0456, 0.0789, ...],
  "dimensions": 1536,
  "model": "text-embedding-ada-002",
  "processingTime": 78
}
```

---

#### 5.G.2 Calculate Similarity

##### POST /api/v1/embeddings/similarity
**Purpose:** Calculate cosine similarity between texts

**Request:**
```json
{
  "text1": "enterprise volume discount",
  "text2": "large customer bulk pricing"
}
```

**Response:** `200 OK`
```json
{
  "similarity": 0.847,
  "interpretation": "HIGHLY_SIMILAR",
  "threshold": 0.80,
  "processingTime": 134
}
```

---

#### 5.G.3 Compare Multiple

##### POST /api/v1/embeddings/compare
**Purpose:** Compare one query against multiple candidates

**Request:**
```json
{
  "query": "volume discount for enterprise",
  "candidates": [
    "enterprise pricing tier",
    "bulk order discount",
    "customer loyalty program",
    "seasonal promotion"
  ]
}
```

**Response:** `200 OK`
```json
{
  "query": "volume discount for enterprise",
  "similarities": [
    {
      "text": "bulk order discount",
      "score": 0.91,
      "rank": 1
    },
    {
      "text": "enterprise pricing tier",
      "score": 0.85,
      "rank": 2
    },
    {
      "text": "customer loyalty program",
      "score": 0.62,
      "rank": 3
    },
    {
      "text": "seasonal promotion",
      "score": 0.43,
      "rank": 4
    }
  ]
}
```

---

### 5.H Composite APIs (PHASE 2)

**Base Path:** `/api/v1/composite`

#### 5.H.1 Complete Pricing Workflow

##### POST /api/v1/composite/pricing/complete
**Purpose:** End-to-end pricing workflow

**Request:**
```json
{
  "customer": {...},
  "quote": {...},
  "preferences": {
    "useAIRouting": true,
    "checkCache": true,
    "calculateMetrics": true,
    "returnRecommendations": true
  }
}
```

**Response:** `200 OK`
```json
{
  "workflowId": "workflow-uuid-11111",
  "steps": [
    {
      "step": "METRICS_CALCULATION",
      "status": "COMPLETED",
      "duration": 123,
      "result": {
        "arr": 120000.00,
        "tcv": 360000.00
      }
    },
    {
      "step": "AI_ROUTING",
      "status": "COMPLETED",
      "duration": 456,
      "result": {
        "strategy": "VOLUME_DISCOUNT"
      }
    },
    {
      "step": "RULE_EXECUTION",
      "status": "COMPLETED",
      "duration": 234,
      "result": {
        "rulesApplied": 3,
        "finalPrice": 102000.00
      }
    }
  ],
  "finalResult": {
    "metrics": {...},
    "routing": {...},
    "pricing": {...},
    "recommendations": [...]
  },
  "totalTime": 813,
  "cacheHit": false
}
```

---

### 5.I Admin & Monitoring APIs (PHASE 2)

**Base Path:** `/api/v1/admin`

#### 5.I.1 Health Check

##### GET /api/v1/admin/health
**Purpose:** Comprehensive health check

**Response:** `200 OK`
```json
{
  "status": "UP",
  "timestamp": "2025-01-24T21:00:00Z",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "connectionPool": "8/20 active",
        "responseTime": 3
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "memoryUsed": "124 MB",
        "connectedClients": 15
      }
    },
    "drools": {
      "status": "UP",
      "details": {
        "loadedRules": 47,
        "kieBaseStatus": "INITIALIZED"
      }
    },
    "ai": {
      "status": "UP",
      "details": {
        "embeddingService": "UP",
        "thinkingService": "UP",
        "cacheService": "UP"
      }
    }
  },
  "overallHealth": "HEALTHY"
}
```

---

#### 5.I.2 System Metrics

##### GET /api/v1/admin/metrics
**Purpose:** System-wide performance metrics

**Response:** `200 OK`
```json
{
  "requests": {
    "total": 15432,
    "perSecond": 25.7,
    "byEndpoint": {
      "/api/v1/pricing-rules": 5432,
      "/api/v1/routing/route": 3421,
      "/api/v1/salesforce/quotes/evaluate": 2145
    }
  },
  "performance": {
    "averageResponseTime": 234,
    "p95ResponseTime": 567,
    "p99ResponseTime": 1234
  },
  "errors": {
    "total": 127,
    "rate": 0.008,
    "byType": {
      "400": 45,
      "404": 32,
      "500": 50
    }
  },
  "cache": {
    "hitRate": 0.73,
    "size": "245 MB"
  }
}
```

---

#### 5.I.3 Service Status

##### GET /api/v1/admin/services/status
**Purpose:** Status of all services

**Response:** `200 OK`
```json
{
  "services": [
    {
      "name": "AIRoutingService",
      "status": "UP",
      "requestCount": 3421,
      "errorRate": 0.002,
      "averageLatency": 456
    },
    {
      "name": "SemanticCacheService",
      "status": "UP",
      "cacheSize": 1547,
      "hitRate": 0.73
    }
  ]
}
```

---

### 5.J Async Processing APIs (PHASE 2)

**Base Path:** `/api/v1/async`

#### 5.J.1 Submit Job

##### POST /api/v1/async/analysis/sequential
**Purpose:** Submit async sequential thinking job

**Request:**
```json
{
  "analysisType": "SEQUENTIAL_THINKING",
  "input": {
    "initialThought": "Analyze optimal pricing strategy",
    "context": {...}
  },
  "webhookUrl": "https://client.com/webhook",
  "callbackHeaders": {
    "Authorization": "Bearer xyz"
  }
}
```

**Response:** `202 Accepted`
```json
{
  "jobId": "job-uuid-11111",
  "status": "QUEUED",
  "estimatedCompletion": "2025-01-24T21:10:00Z",
  "statusUrl": "/api/v1/async/jobs/job-uuid-11111"
}
```

---

#### 5.J.2 Get Job Status

##### GET /api/v1/async/jobs/{jobId}
**Purpose:** Check async job status

**Response:** `200 OK`
```json
{
  "jobId": "job-uuid-11111",
  "status": "COMPLETED",
  "progress": 100,
  "startTime": "2025-01-24T21:00:00Z",
  "completionTime": "2025-01-24T21:09:00Z",
  "result": {
    "analysisId": "analysis-uuid-22222",
    "conclusion": "Recommend VOLUME_DISCOUNT strategy",
    "confidence": 0.91
  },
  "webhookDelivered": true
}
```

**Status Values:**
- `QUEUED` - Waiting to start
- `PROCESSING` - In progress
- `COMPLETED` - Finished successfully
- `FAILED` - Error occurred
- `CANCELLED` - User cancelled

---

### 5.K Archive Management APIs (PHASE 3)

**Base Path:** `/api/v1/archive`

#### 5.K.1 Trigger Archive

##### POST /api/v1/archive/trigger
**Purpose:** Manually trigger archive process

**Request:**
```json
{
  "dataTypes": ["RULES", "METRICS", "EXECUTION_HISTORY", "AUDIT_LOGS"],
  "olderThan": "2024-10-25T00:00:00Z",
  "archivePath": "/mnt/archives/pricing-service"
}
```

**Response:** `202 Accepted`
```json
{
  "archiveJobId": "archive-job-uuid-11111",
  "status": "PROCESSING",
  "estimatedCompletion": "2025-01-24T22:00:00Z"
}
```

---

#### 5.K.2 Archive Status

##### GET /api/v1/archive/jobs/{archiveJobId}
**Purpose:** Get archive job status

**Response:** `200 OK`
```json
{
  "archiveJobId": "archive-job-uuid-11111",
  "status": "COMPLETED",
  "startTime": "2025-01-24T21:30:00Z",
  "completionTime": "2025-01-24T21:45:00Z",
  "summary": {
    "rulesArchived": 234,
    "metricsArchived": 8923,
    "executionHistoryArchived": 45123,
    "auditLogsArchived": 12345,
    "totalSizeArchived": "1.2 GB",
    "archivePath": "/mnt/archives/pricing-service/2025-01-24.tar.gz"
  }
}
```

---

### 5.L Reporting & Export APIs (PHASE 3)

**Base Path:** `/api/v1/reports`

#### 5.L.1 Generate Report

##### POST /api/v1/reports/financial/generate
**Purpose:** Generate financial metrics report

**Request:**
```json
{
  "reportType": "FINANCIAL_METRICS",
  "customerId": "CUST-001",
  "startDate": "2025-01-01T00:00:00Z",
  "endDate": "2025-01-24T23:59:59Z",
  "format": "EXCEL",
  "includeCharts": true,
  "includeTrends": true
}
```

**Response:** `201 Created`
```json
{
  "reportId": "report-uuid-11111",
  "reportType": "FINANCIAL_METRICS",
  "status": "GENERATING",
  "estimatedCompletion": "2025-01-24T22:05:00Z",
  "downloadUrl": "/api/v1/reports/report-uuid-11111/download"
}
```

---

#### 5.L.2 Download Report

##### GET /api/v1/reports/{reportId}/download
**Purpose:** Download generated report

**Response:** `200 OK` (file stream)

**Headers:**
```
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
Content-Disposition: attachment; filename="financial-metrics-2025-01-24.xlsx"
```

---

#### 5.L.3 Export Data

##### POST /api/v1/export/rules
**Purpose:** Export rules in various formats

**Request:**
```json
{
  "ruleIds": ["rule-uuid-1", "rule-uuid-2"],
  "format": "DRL",
  "includeMetadata": true
}
```

**Response:** `200 OK`
```
package com.example.rules;

rule "Q4 Holiday Promotion"
    when
        $quote : Quote(totalAmount > 5000,
                      date >= "2025-12-01",
                      date <= "2025-12-31")
    then
        $quote.applyDiscount(0.12);
end
```

---

## 6. Implementation Roadmap

### 6.1 Phase 1 - Critical (Weeks 1-4)

**Priority: IMMEDIATE - Salesforce Integration**

#### Week 1: Foundation
- ✅ Set up OAuth2 client credentials authentication
- ✅ Create Salesforce Integration API base structure (`/api/v1/salesforce`)
- ✅ Implement rule sync endpoints (sync, push)
- ✅ Test bidirectional sync with mock Salesforce data

#### Week 2: Pricing Rules CRUD
- ✅ Implement complete CRUD endpoints (`/api/v1/pricing-rules`)
- ✅ Add version history tracking
- ✅ Integrate conflict detection with AI suggestions
- ✅ Build natural language + structured search

#### Week 3: Quote & Metrics Integration
- ✅ Implement quote evaluation endpoint
- ✅ Add quote update functionality
- ✅ Create metrics push to RCA endpoint
- ✅ Implement product/pricebook sync

#### Week 4: AI Routing & WebSocket
- ✅ Expose AIRoutingService via REST API
- ✅ Implement WebSocket real-time events
- ✅ Add routing statistics and configuration
- ✅ End-to-end testing with Azure Functions

**Deliverables:**
- ✅ Salesforce Integration APIs (complete)
- ✅ Pricing Rules CRUD + Search (complete)
- ✅ AI Routing API (complete)
- ✅ WebSocket support (complete)
- ✅ OAuth2 authentication (complete)
- ✅ API documentation (OpenAPI spec)

---

### 6.2 Phase 2 - Enhanced Capabilities (Weeks 5-8)

**Priority: HIGH - Performance & Advanced Features**

#### Week 5: Semantic Cache
- ✅ Expose SemanticCacheService via REST API
- ✅ Implement cache search, add, clear endpoints
- ✅ Add cache statistics and monitoring
- ✅ Integrate with existing pricing flows

#### Week 6: AI Thinking & Embeddings
- ✅ Expose SequentialThinkingService
- ✅ Implement thought processing endpoints
- ✅ Expose EmbeddingService
- ✅ Add similarity calculation endpoints

#### Week 7: Composite & Async
- ✅ Build composite pricing workflow endpoints
- ✅ Implement async job processing
- ✅ Add webhook support for long-running operations
- ✅ Create job status tracking

#### Week 8: Admin & Monitoring
- ✅ Implement comprehensive health checks
- ✅ Add system metrics endpoints
- ✅ Create service status monitoring
- ✅ Integrate with Grafana/Splunk

**Deliverables:**
- ✅ Semantic Cache API (complete)
- ✅ AI Thinking API (complete)
- ✅ Embeddings API (complete)
- ✅ Composite APIs (complete)
- ✅ Async Processing (complete)
- ✅ Admin/Monitoring APIs (complete)

---

### 6.3 Phase 3 - Advanced Features (Weeks 9-12)

**Priority: MEDIUM - Operational Excellence**

#### Week 9: Archive Management
- ✅ Implement automated archival jobs
- ✅ Create archive trigger endpoints
- ✅ Add archive status tracking
- ✅ Test 90-day retention policy

#### Week 10: Reporting & Export
- ✅ Build report generation endpoints
- ✅ Add Excel/CSV export capabilities
- ✅ Create financial metrics reports
- ✅ Implement rule performance reports

#### Week 11: Bulk Operations
- ✅ Add bulk rule import/export
- ✅ Implement bulk activation/deactivation
- ✅ Create data sync utilities
- ✅ Add validation for bulk operations

#### Week 12: Polish & Documentation
- ✅ Complete API documentation
- ✅ Create integration guides
- ✅ Prepare Postman collections
- ✅ Conduct performance testing

**Deliverables:**
- ✅ Archive Management (complete)
- ✅ Reporting & Export APIs (complete)
- ✅ Bulk Operations (complete)
- ✅ Complete documentation (complete)

---

### 6.4 Implementation Guidelines

**Controller Structure:**
```java
@RestController
@RequestMapping("/api/v1/pricing-rules")
@Tag(name = "Pricing Rules", description = "CRUD and search operations for pricing rules")
@Validated
public class PricingRuleController {

    private final PricingRuleService pricingRuleService;
    private final RuleConflictService conflictService;

    @PostMapping
    @Operation(summary = "Create new pricing rule")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rule created successfully"),
        @ApiResponse(responseCode = "409", description = "Rule conflicts detected")
    })
    public ResponseEntity<RuleResponse> createRule(
            @Valid @RequestBody CreateRuleRequest request) {
        // Implementation
    }
}
```

**Service Layer:**
```java
@Service
@RequiredArgsConstructor
@Transactional
public class PricingRuleServiceImpl implements PricingRuleService {

    private final RuleRepository ruleRepository;
    private final DroolsIntegrationService droolsService;
    private final RuleConflictService conflictService;
    private final AIService aiService;

    @Override
    public RuleResponse createRule(CreateRuleRequest request) {
        // 1. Validate rule syntax
        // 2. Check conflicts
        // 3. Create rule entity
        // 4. Generate Drools DRL
        // 5. Return response
    }
}
```

**DTO Example:**
```java
@Data
@Builder
@Schema(description = "Request to create a new pricing rule")
public class CreateRuleRequest {

    @NotBlank
    @Schema(description = "Rule name", example = "Q4 Holiday Promotion")
    private String name;

    @NotBlank
    @Schema(description = "Rule description")
    private String description;

    @NotNull
    @Schema(description = "Rule type")
    private RuleType ruleType;

    @Min(1) @Max(100)
    @Schema(description = "Rule priority (1-100)")
    private Integer priority;

    @Valid
    @Schema(description = "Rule conditions")
    private RuleConditions conditions;

    @Valid
    @Schema(description = "Rule actions")
    private List<RuleAction> actions;

    @Schema(description = "Check for conflicts before creating")
    private boolean checkConflicts = true;
}
```

---

## 7. Security & Authentication

### 7.1 Authentication Methods

**JWT Authentication (Primary):**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**OAuth2 Client Credentials (Azure Functions):**
```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=client_credentials
&client_id=azure-function-client
&client_secret=secret
&scope=pricing.read pricing.write
```

**Token Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "scope": "pricing.read pricing.write"
}
```

### 7.2 Authorization Roles

| Role | Permissions | Use Case |
|------|-------------|----------|
| `ROLE_ADMIN` | Full access to all APIs | System administrators |
| `ROLE_PRICING_MANAGER` | Pricing rules, metrics, routing | Pricing team |
| `ROLE_RULE_CREATOR` | Create/edit rules, templates | Business analysts |
| `ROLE_ANALYST` | Read-only access | Data analysts, reporting |
| `ROLE_API_USER` | Basic integration access | External systems |
| `ROLE_SALESFORCE_SYNC` | Salesforce integration only | Azure Functions |

### 7.3 Endpoint Security Matrix

| Endpoint | Required Role | Notes |
|----------|---------------|-------|
| `POST /api/v1/pricing-rules` | ROLE_RULE_CREATOR | Create rules |
| `PUT /api/v1/pricing-rules/{id}` | ROLE_RULE_CREATOR | Update rules |
| `DELETE /api/v1/pricing-rules/{id}` | ROLE_PRICING_MANAGER | Delete rules |
| `POST /api/v1/pricing-rules/search` | ROLE_ANALYST | Search rules |
| `POST /api/v1/salesforce/*` | ROLE_SALESFORCE_SYNC | Salesforce only |
| `POST /api/v1/routing/route` | ROLE_API_USER | Routing |
| `GET /api/v1/admin/health` | PUBLIC | No auth required |
| `POST /api/v1/admin/cache/clear-all` | ROLE_ADMIN | Admin only |

### 7.4 Rate Limiting

**Implementation:**
```java
@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiter apiRateLimiter() {
        return RateLimiter.of("api", RateLimiterConfig.custom()
            .limitForPeriod(1000)  // 1000 requests
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .timeoutDuration(Duration.ofSeconds(5))
            .build());
    }
}
```

**Rate Limit Response:**
```json
{
  "timestamp": "2025-01-24T22:30:00Z",
  "status": 429,
  "error": "Too Many Requests",
  "message": "Rate limit exceeded. Maximum 1000 requests per minute.",
  "retryAfter": 45,
  "limit": 1000,
  "remaining": 0,
  "resetAt": "2025-01-24T22:31:00Z"
}
```

---

## 8. Error Handling & Internationalization

### 8.1 Error Response Format (RFC 7807)

**Standard Error Response:**
```json
{
  "type": "https://api.company.com/errors/rule-conflict",
  "title": "Rule Conflict Detected",
  "status": 409,
  "detail": "The rule 'Q4 Holiday Promotion' conflicts with existing rule 'Black Friday Sale'",
  "instance": "/api/v1/pricing-rules",
  "timestamp": "2025-01-24T22:35:00Z",
  "requestId": "req-uuid-12345",
  "suggestedAction": "Review conflict details and adjust rule conditions or priority",
  "conflictDetails": {
    "conflictingRuleId": "rule-uuid-88888",
    "conflictType": "OVERLAPPING_CONDITIONS",
    "severity": "HIGH"
  },
  "supportUrl": "https://docs.company.com/errors/rule-conflict"
}
```

**Validation Error:**
```json
{
  "type": "https://api.company.com/errors/validation",
  "title": "Validation Failed",
  "status": 422,
  "detail": "Request validation failed with 2 errors",
  "timestamp": "2025-01-24T22:36:00Z",
  "requestId": "req-uuid-67890",
  "errors": [
    {
      "field": "priority",
      "value": 150,
      "message": "Priority must be between 1 and 100",
      "code": "PRIORITY_OUT_OF_RANGE"
    },
    {
      "field": "effectiveDate",
      "value": "2024-01-01",
      "message": "Effective date cannot be in the past",
      "code": "INVALID_EFFECTIVE_DATE"
    }
  ]
}
```

### 8.2 Internationalization

**Supported Languages:**
1. English (en-US) - Default
2. Mandarin Chinese (zh-CN)
3. Hindi (hi-IN)
4. Spanish (es-ES)
5. French (fr-FR)

**Language Selection:**
```http
Accept-Language: es-ES,en-US;q=0.9
```

**Localized Error (Spanish):**
```json
{
  "type": "https://api.company.com/errors/rule-conflict",
  "title": "Conflicto de Regla Detectado",
  "status": 409,
  "detail": "La regla 'Q4 Holiday Promotion' entra en conflicto con la regla existente 'Black Friday Sale'",
  "instance": "/api/v1/pricing-rules",
  "timestamp": "2025-01-24T22:37:00Z",
  "requestId": "req-uuid-12345",
  "suggestedAction": "Revise los detalles del conflicto y ajuste las condiciones o prioridad de la regla",
  "language": "es-ES"
}
```

**Implementation:**
```java
@Component
public class LocalizedErrorHandler {

    @Autowired
    private MessageSource messageSource;

    public ErrorResponse createError(Exception ex, Locale locale) {
        String message = messageSource.getMessage(
            "error.rule.conflict",
            new Object[]{ruleName},
            locale
        );

        return ErrorResponse.builder()
            .title(message)
            .language(locale.toLanguageTag())
            .build();
    }
}
```

**Message Properties:**
```properties
# messages_en_US.properties
error.rule.conflict=The rule ''{0}'' conflicts with existing rule ''{1}''
error.validation.priority=Priority must be between 1 and 100
error.auth.unauthorized=Authentication required

# messages_es_ES.properties
error.rule.conflict=La regla ''{0}'' entra en conflicto con la regla existente ''{1}''
error.validation.priority=La prioridad debe estar entre 1 y 100
error.auth.unauthorized=Autenticación requerida
```

---

## 9. Testing Strategy

### 9.1 Unit Tests

**Controller Tests:**
```java
@WebMvcTest(PricingRuleController.class)
@AutoConfigureMockMvc
class PricingRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PricingRuleService pricingRuleService;

    @Test
    void createRule_Success() throws Exception {
        CreateRuleRequest request = CreateRuleRequest.builder()
            .name("Test Rule")
            .ruleType(RuleType.VOLUME_DISCOUNT)
            .build();

        mockMvc.perform(post("/api/v1/pricing-rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.ruleId").exists());
    }

    @Test
    void createRule_Conflict() throws Exception {
        when(pricingRuleService.createRule(any()))
            .thenThrow(new RuleConflictException("Conflict detected"));

        mockMvc.perform(post("/api/v1/pricing-rules")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Rule Conflict Detected"));
    }
}
```

### 9.2 Integration Tests

**Salesforce Integration Test:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestContainers
class SalesforceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void syncRulesFromSalesforce_Success() {
        SalesforceRuleSyncRequest request = createSyncRequest();

        ResponseEntity<SalesforceRuleSyncResponse> response =
            restTemplate.postForEntity(
                "/api/v1/salesforce/rules/sync",
                request,
                SalesforceRuleSyncResponse.class
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getResults().getCreated()).isGreaterThan(0);
    }
}
```

### 9.3 Performance Tests

**JMeter Test Plan:**
```xml
<jmeterTestPlan>
  <hashTree>
    <TestPlan>
      <ThreadGroup numThreads="100" rampUp="10">
        <HTTPSampler domain="localhost" port="8080"
                    path="/api/v1/routing/route" method="POST">
          <stringProp name="HTTPSampler.postBody">
            ${__FileToString(routing-request.json)}
          </stringProp>
        </HTTPSampler>
      </ThreadGroup>
    </TestPlan>
  </hashTree>
</jmeterTestPlan>
```

**Performance Targets:**
- Read operations: < 200ms p95
- Write operations: < 500ms p95
- AI operations: < 2000ms p95
- Search operations: < 300ms p95
- WebSocket message delivery: < 100ms

### 9.4 Security Tests

**Authentication Test:**
```java
@Test
void accessProtectedEndpoint_WithoutAuth_Returns401() {
    mockMvc.perform(post("/api/v1/pricing-rules")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
}

@Test
void accessProtectedEndpoint_WithInvalidRole_Returns403() {
    mockMvc.perform(post("/api/v1/pricing-rules")
        .header("Authorization", "Bearer " + analystToken)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
}
```

---

## 10. Monitoring & Observability

### 10.1 Grafana Dashboards

**Key Metrics:**
- Request rate (req/sec)
- Response time (p50, p95, p99)
- Error rate (%)
- AI service usage (calls, cost)
- Cache hit rate (%)
- WebSocket connections (active)
- Rule execution count
- Database connection pool

**Dashboard Panels:**
```yaml
- title: "API Request Rate"
  target: "rate(http_server_requests_seconds_count[5m])"

- title: "Response Time p95"
  target: "histogram_quantile(0.95, http_server_requests_seconds_bucket)"

- title: "Error Rate"
  target: "rate(http_server_requests_seconds_count{status=~\"5..\"}[5m])"

- title: "Cache Hit Rate"
  target: "rate(cache_hits_total[5m]) / rate(cache_requests_total[5m])"
```

### 10.2 Splunk Logging

**Log Format:**
```json
{
  "timestamp": "2025-01-24T23:00:00.123Z",
  "level": "INFO",
  "logger": "com.example.controller.PricingRuleController",
  "thread": "http-nio-8080-exec-1",
  "message": "Creating pricing rule",
  "context": {
    "requestId": "req-uuid-12345",
    "userId": "user@company.com",
    "endpoint": "POST /api/v1/pricing-rules",
    "ruleName": "Q4 Holiday Promotion",
    "ruleType": "PROMOTIONAL_DISCOUNT"
  },
  "duration": 234,
  "status": "SUCCESS"
}
```

**Splunk Queries:**
```spl
# Error rate by endpoint
index=pricing-service level=ERROR
| stats count by endpoint
| sort -count

# Slow requests (> 1 second)
index=pricing-service duration>1000
| stats avg(duration) p95(duration) by endpoint

# WebSocket connection issues
index=pricing-service websocket_event=CONNECTION_CLOSED reason!=CLIENT_REQUESTED
| timechart count by reason
```

### 10.3 Prometheus Metrics

**Exposed Metrics:**
```yaml
# HTTP metrics
http_server_requests_seconds_count{endpoint, method, status}
http_server_requests_seconds_sum{endpoint, method, status}
http_server_requests_seconds_bucket{endpoint, method, status, le}

# Business metrics
pricing_rules_created_total
pricing_rules_executed_total{rule_type}
pricing_evaluations_total{strategy}
cache_requests_total{cache_type, result}

# System metrics
jvm_memory_used_bytes{area, id}
jvm_gc_pause_seconds_count{action, cause}
hikaricp_connections_active{pool}
```

### 10.4 Alerting

**Critical Alerts:**
```yaml
# High error rate
- alert: HighErrorRate
  expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
  for: 5m
  severity: critical
  message: "Error rate > 5% for 5 minutes"

# Slow response time
- alert: SlowResponseTime
  expr: histogram_quantile(0.95, http_server_requests_seconds_bucket) > 2
  for: 10m
  severity: warning
  message: "p95 response time > 2s for 10 minutes"

# Cache hit rate degradation
- alert: LowCacheHitRate
  expr: rate(cache_hits_total[5m]) / rate(cache_requests_total[5m]) < 0.5
  for: 15m
  severity: warning
  message: "Cache hit rate < 50% for 15 minutes"

# WebSocket connection issues
- alert: WebSocketConnectionFailures
  expr: rate(websocket_connection_failures_total[5m]) > 0.1
  for: 5m
  severity: critical
  message: "WebSocket connection failure rate > 10%"
```

---

## 11. Salesforce Integration Architecture

### 11.1 Integration Flow

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│  Salesforce CPQ │◄───────►│ Azure Functions  │◄───────►│  Our REST APIs  │
│      RCA        │   HTTP  │  (OAuth2 Client) │   HTTP  │  (Spring Boot)  │
└─────────────────┘         └──────────────────┘         └─────────────────┘
        │                            │                             │
        │                            │                             │
        ▼                            ▼                             ▼
  ┌──────────┐              ┌──────────────┐             ┌──────────────┐
  │ Platform │              │ Event Grid   │             │  PostgreSQL  │
  │  Events  │              │  (Optional)  │             │     Redis    │
  └──────────┘              └──────────────┘             │   Drools     │
                                                          └──────────────┘
```

### 11.2 Data Sync Patterns

**1. Real-time Quote Pricing:**
```
Salesforce CPQ → Azure Function → POST /api/v1/salesforce/quotes/evaluate
                                  ← Pricing Result
                 ← Update Quote   ← POST /api/v1/salesforce/quotes/update
```

**2. Bidirectional Rule Sync:**
```
Salesforce RCA → Azure Function → POST /api/v1/salesforce/rules/sync
                                  ← Rule Mapping

Our Service    → Azure Function → Salesforce RCA API
               ← POST /api/v1/salesforce/rules/push
```

**3. Metrics Push to RCA:**
```
Pricing Evaluation → POST /api/v1/salesforce/metrics/push-to-rca (async)
                   ← Job ID

Azure Function polls: GET /api/v1/salesforce/metrics/sync-status/{jobId}
```

### 11.3 Azure Function Example

**Quote Evaluation Function:**
```javascript
const axios = require('axios');

module.exports = async function (context, req) {
    try {
        // 1. Get OAuth2 token
        const token = await getAccessToken();

        // 2. Call our API
        const response = await axios.post(
            'https://api.company.com/api/v1/salesforce/quotes/evaluate',
            req.body,
            {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            }
        );

        // 3. Return result to Salesforce
        context.res = {
            status: 200,
            body: response.data
        };

    } catch (error) {
        context.log.error('Quote evaluation failed', error);
        context.res = {
            status: 500,
            body: { error: error.message }
        };
    }
};

async function getAccessToken() {
    const tokenResponse = await axios.post(
        'https://api.company.com/oauth/token',
        'grant_type=client_credentials&client_id=azure-func&client_secret=secret',
        { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
    );
    return tokenResponse.data.access_token;
}
```

### 11.4 Salesforce Setup

**Custom Apex Class:**
```apex
public class PricingServiceIntegration {

    @InvocableMethod(label='Evaluate Quote Pricing')
    public static List<PricingResult> evaluateQuotePricing(List<QuoteRequest> requests) {
        List<PricingResult> results = new List<PricingResult>();

        for (QuoteRequest req : requests) {
            // Call Azure Function
            HttpRequest httpReq = new HttpRequest();
            httpReq.setEndpoint('callout:AzureFunction_EvaluateQuote');
            httpReq.setMethod('POST');
            httpReq.setHeader('Content-Type', 'application/json');
            httpReq.setBody(JSON.serialize(req));

            Http http = new Http();
            HttpResponse response = http.send(httpReq);

            if (response.getStatusCode() == 200) {
                PricingResult result = (PricingResult) JSON.deserialize(
                    response.getBody(),
                    PricingResult.class
                );
                results.add(result);
            }
        }

        return results;
    }
}
```

**Flow Integration:**
1. Create Process Builder or Flow
2. Trigger on Quote object change
3. Call Apex action: `PricingServiceIntegration.evaluateQuotePricing`
4. Update Quote with returned pricing

---

## 12. Appendices

### 12.A Sample DTOs

**CreateRuleRequest.java:**
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRuleRequest {

    @NotBlank(message = "Rule name is required")
    @Size(min = 3, max = 100)
    private String name;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotNull
    private RuleType ruleType;

    @Min(1) @Max(100)
    private Integer priority;

    @NotNull
    private RuleStatus status;

    @Valid
    private RuleConditions conditions;

    @Valid
    @Size(min = 1)
    private List<RuleAction> actions;

    @Future
    private LocalDateTime effectiveDate;

    @Future
    private LocalDateTime expirationDate;

    private List<String> tags;

    private boolean checkConflicts = true;
    private boolean autoResolveConflicts = false;
}
```

**RuleResponse.java:**
```java
@Data
@Builder
public class RuleResponse {
    private String ruleId;
    private String externalId;
    private String name;
    private String description;
    private RuleType ruleType;
    private Integer priority;
    private RuleStatus status;
    private Integer version;
    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
    private ConflictCheck conflictCheck;
    private String droolsRuleId;
    private DeploymentStatus deploymentStatus;
    private List<VersionHistory> versionHistory;
}
```

### 12.B OpenAPI Snippet

```yaml
openapi: 3.0.3
info:
  title: Price Rules AI Drools Service API
  version: 1.0.0
  description: REST APIs for pricing rule management and evaluation
  contact:
    name: API Support
    email: api-support@company.com

servers:
  - url: https://api.company.com/api/v1
    description: Production
  - url: https://staging-api.company.com/api/v1
    description: Staging

security:
  - bearerAuth: []

paths:
  /pricing-rules:
    post:
      tags:
        - Pricing Rules
      summary: Create new pricing rule
      operationId: createRule
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateRuleRequest'
      responses:
        '201':
          description: Rule created successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/RuleResponse'
        '409':
          description: Rule conflict detected
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

    get:
      tags:
        - Pricing Rules
      summary: List pricing rules
      operationId: listRules
      parameters:
        - name: status
          in: query
          schema:
            type: string
            enum: [ACTIVE, DRAFT, INACTIVE]
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
      responses:
        '200':
          description: Rules retrieved successfully
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/PagedRuleResponse'

components:
  securitySchemes:
    bearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT

  schemas:
    CreateRuleRequest:
      type: object
      required:
        - name
        - ruleType
        - priority
        - conditions
        - actions
      properties:
        name:
          type: string
          minLength: 3
          maxLength: 100
        description:
          type: string
          maxLength: 500
        ruleType:
          type: string
          enum: [VOLUME_DISCOUNT, PROMOTIONAL_DISCOUNT, LOYALTY_DISCOUNT]
        priority:
          type: integer
          minimum: 1
          maximum: 100

    RuleResponse:
      type: object
      properties:
        ruleId:
          type: string
          format: uuid
        name:
          type: string
        status:
          type: string
          enum: [ACTIVE, DRAFT, INACTIVE]
        version:
          type: integer
        createdDate:
          type: string
          format: date-time

    ErrorResponse:
      type: object
      properties:
        type:
          type: string
          format: uri
        title:
          type: string
        status:
          type: integer
        detail:
          type: string
        timestamp:
          type: string
          format: date-time
```

### 12.C Postman Collection Structure

```
Price Rules AI Drools Service/
├── Authentication/
│   ├── Get OAuth2 Token
│   └── Login (JWT)
├── Salesforce Integration/
│   ├── Sync Rules from RCA
│   ├── Push Rules to RCA
│   ├── Evaluate Quote
│   ├── Update Quote
│   └── Push Metrics to RCA
├── Pricing Rules/
│   ├── Create Rule
│   ├── Get Rule
│   ├── Update Rule
│   ├── Delete Rule
│   ├── Search Rules (Natural Language)
│   ├── Search Rules (Structured)
│   ├── List Rules
│   ├── Get Versions
│   ├── Revert to Version
│   ├── Deploy Rule
│   └── Undeploy Rule
├── AI Routing/
│   ├── Route Request
│   ├── Evaluate All Strategies
│   ├── Get Available Routes
│   ├── Get Statistics
│   └── Update Configuration
├── WebSocket/
│   ├── Connect
│   ├── Subscribe to Events
│   └── Unsubscribe
├── Semantic Cache/
│   ├── Search Cache
│   ├── Add to Cache
│   ├── Get Statistics
│   └── Clear Cache
└── Admin/
    ├── Health Check
    ├── Get Metrics
    └── Service Status
```

---

## Summary

This comprehensive REST API plan provides:

✅ **Complete API Coverage** - All services exposed via REST endpoints
✅ **Salesforce Integration** - Bidirectional sync with CPQ/RCA via Azure Functions
✅ **Pricing Rules CRUD** - Full lifecycle management with AI-powered search
✅ **Real-time Updates** - WebSocket support for live events
✅ **AI Capabilities** - Routing, semantic caching, sequential thinking
✅ **Enterprise Ready** - Security, monitoring, i18n, error handling
✅ **Phased Implementation** - 12-week roadmap with clear priorities
✅ **Production Standards** - Testing, documentation, observability

**Next Steps:**
1. Review and approve this plan
2. Set up development environment
3. Begin Phase 1 implementation (Weeks 1-4)
4. Coordinate with Salesforce team for Azure Functions setup
5. Schedule weekly progress reviews

**Questions or Feedback:** Contact API team at api-support@company.com
