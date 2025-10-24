package com.example.pricerulesaidrools.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global WebDataBinder configuration to prevent mass assignment vulnerabilities.
 *
 * This class implements field whitelisting for all controllers to prevent attackers
 * from injecting unexpected fields into request objects. Each controller has specific
 * allowed fields based on their DTOs and use cases.
 *
 * OWASP Reference: A5:2021 – Security Misconfiguration
 * CWE-915: Improperly Controlled Modification of Dynamically-Determined Object Attributes
 *
 * @author Security Team
 * @since 1.0.0
 */
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class WebDataBinderConfig {

    /**
     * Global @InitBinder method that configures field whitelisting based on the controller.
     * This method is called before each request binding to ensure only allowed fields
     * are bound to the request objects.
     *
     * @param binder The WebDataBinder to configure
     * @param webRequest The current web request (for logging purposes)
     * @param handlerMethod The handler method being invoked
     */
    @InitBinder
    public void initBinder(WebDataBinder binder, NativeWebRequest webRequest, HandlerMethod handlerMethod) {
        if (handlerMethod == null) {
            // Fallback to most restrictive if we can't determine the handler
            setDefaultWhitelist(binder);
            return;
        }

        String controllerName = handlerMethod.getBeanType().getSimpleName();
        String methodName = handlerMethod.getMethod().getName();

        // Log the binding configuration for audit purposes
        log.debug("Configuring field whitelist for controller: {}, method: {}",
                 controllerName, methodName);

        // Configure whitelist based on controller and method
        switch (controllerName) {
            case "RuleController":
                configureRuleControllerWhitelist(binder, methodName);
                break;
            case "AuthController":
                configureAuthControllerWhitelist(binder, methodName);
                break;
            case "FinancialMetricsController":
                configureFinancialMetricsControllerWhitelist(binder, methodName);
                break;
            case "AIRuleController":
                configureAIRuleControllerWhitelist(binder, methodName);
                break;
            case "RuleTemplateController":
                configureRuleTemplateControllerWhitelist(binder, methodName);
                break;
            case "RuleConflictController":
                configureRuleConflictControllerWhitelist(binder, methodName);
                break;
            case "FinancialMetricsHistoryController":
                configureFinancialMetricsHistoryControllerWhitelist(binder, methodName);
                break;
            case "RuleTestController":
                configureRuleTestControllerWhitelist(binder, methodName);
                break;
            case "HealthController":
                // Health endpoint typically doesn't accept request bodies
                binder.setAllowedFields(); // No fields allowed
                break;
            default:
                // For unknown controllers, use the most restrictive whitelist
                setDefaultWhitelist(binder);
                log.warn("Unknown controller encountered: {}. Using default restrictive whitelist.",
                        controllerName);
                break;
        }

        // Set additional security configurations
        binder.setAutoGrowNestedPaths(false); // Prevent nested path injection

        // Log potential security issues
        if (binder.getDisallowedFields() != null && binder.getDisallowedFields().length > 0) {
            log.info("Disallowed fields configured for {}.{}: {}",
                    controllerName, methodName, String.join(", ", binder.getDisallowedFields()));
        }
    }

    /**
     * Configure whitelist for RuleController endpoints
     */
    private void configureRuleControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "deployRules":
            case "updateRules":
            case "validateRules":
                // RuleRequest fields
                binder.setAllowedFields(
                    "name",
                    "description",
                    "content",
                    "version",
                    "metadata"
                );
                break;
            case "executeRules":
                // RuleExecutionRequest fields
                binder.setAllowedFields(
                    "ruleSetId",
                    "facts"
                );
                break;
            case "executeBatchRules":
                // List of facts - handled differently
                binder.setAllowedFields("*"); // Lists are handled by Jackson
                break;
            default:
                // For GET/DELETE operations, no body expected
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Configure whitelist for AuthController endpoints
     */
    private void configureAuthControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "authenticateUser":
                // LoginRequest fields
                binder.setAllowedFields(
                    "username",
                    "password"
                );
                break;
            case "registerUser":
                // SignupRequest fields
                binder.setAllowedFields(
                    "username",
                    "email",
                    "password",
                    "firstName",
                    "lastName",
                    "roles"
                );
                break;
            default:
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Configure whitelist for FinancialMetricsController endpoints
     */
    private void configureFinancialMetricsControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "calculateMetrics":
                // FinancialMetricsRequest fields
                binder.setAllowedFields(
                    "quoteId",
                    "customerId",
                    "monthlyPrice",
                    "durationInMonths",
                    "expectedDuration",
                    "customerType",
                    "subscriptionType",
                    "basePrice",
                    "customerTenureMonths",
                    "productId"
                );
                break;
            case "applyPricingStrategy":
                // PricingStrategyRequest fields
                binder.setAllowedFields(
                    "quoteId",
                    "strategy",
                    "parameters"
                );
                break;
            default:
                // GET operations - no body
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Configure whitelist for AIRuleController endpoints
     */
    private void configureAIRuleControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "createRule":
            case "createRuleAsync":
            case "createDocumentationEnhancedRule":
                // RuleCreationRequest fields
                binder.setAllowedFields(
                    "businessRequirement",
                    "ruleType",
                    "ruleName",
                    "testFacts",
                    "includeDocumentation",
                    "generateTestCases",
                    "tags"
                );
                break;
            case "analyzeRequirement":
            case "improveRule":
                // String body - handled differently
                binder.setAllowedFields("*");
                break;
            case "enhanceWithDocumentation":
                // DocumentationEnhancementRequest fields
                binder.setAllowedFields(
                    "topic",
                    "rulePattern",
                    "includeCodeExamples",
                    "includeBestPractices",
                    "context"
                );
                break;
            default:
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Configure whitelist for RuleTemplateController endpoints
     */
    private void configureRuleTemplateControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "createTemplate":
            case "updateTemplate":
                // RuleTemplate fields
                binder.setAllowedFields(
                    "name",
                    "description",
                    "category",
                    "templateContent",
                    "variables",
                    "tags",
                    "version"
                );
                break;
            case "generateFromTemplate":
                // Template generation request
                binder.setAllowedFields(
                    "templateId",
                    "variables",
                    "name",
                    "description"
                );
                break;
            default:
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Configure whitelist for RuleConflictController endpoints
     */
    private void configureRuleConflictControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "detectConflicts":
                // Conflict detection request
                binder.setAllowedFields(
                    "ruleSetIds",
                    "checkLevel",
                    "includeResolutions"
                );
                break;
            case "resolveConflict":
                // Conflict resolution request
                binder.setAllowedFields(
                    "conflictId",
                    "resolutionStrategy",
                    "parameters"
                );
                break;
            default:
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Configure whitelist for FinancialMetricsHistoryController endpoints
     */
    private void configureFinancialMetricsHistoryControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "recordMetrics":
                // Metrics recording request
                binder.setAllowedFields(
                    "customerId",
                    "quoteId",
                    "metrics",
                    "timestamp",
                    "source"
                );
                break;
            default:
                // Mostly GET operations
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Configure whitelist for RuleTestController endpoints
     */
    private void configureRuleTestControllerWhitelist(WebDataBinder binder, String methodName) {
        switch (methodName) {
            case "runTest":
            case "runTestSuite":
                // Test execution request
                binder.setAllowedFields(
                    "ruleSetId",
                    "testCases",
                    "testData",
                    "expectedResults",
                    "options"
                );
                break;
            case "createTestCase":
                // Test case creation
                binder.setAllowedFields(
                    "name",
                    "description",
                    "ruleSetId",
                    "inputData",
                    "expectedOutput",
                    "tags"
                );
                break;
            default:
                binder.setAllowedFields();
                break;
        }
    }

    /**
     * Sets the default restrictive whitelist for unknown or untrusted contexts.
     * This is the most restrictive configuration and allows no fields by default.
     */
    private void setDefaultWhitelist(WebDataBinder binder) {
        // By default, allow no fields for maximum security
        binder.setAllowedFields();

        // Log this restrictive action
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            log.warn("Default restrictive whitelist applied for request: {} {}",
                    request.getMethod(), request.getRequestURI());
        }
    }

    /**
     * Explicitly disallow dangerous fields that should never be bound.
     * These fields are commonly used in attacks and should be blocked globally.
     */
    private void setGlobalDisallowedFields(WebDataBinder binder) {
        binder.setDisallowedFields(
            "class",
            "Class",
            "*.class",
            "*.Class",
            "classLoader",
            "*.classLoader"
        );
    }
}