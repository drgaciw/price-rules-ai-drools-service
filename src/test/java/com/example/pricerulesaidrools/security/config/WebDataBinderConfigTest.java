package com.example.pricerulesaidrools.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;
import org.mockito.Mockito;

/**
 * Unit tests for WebDataBinder field whitelisting configuration.
 *
 * These tests verify that the WebDataBinder correctly configures field
 * whitelisting
 * for each controller to prevent mass assignment vulnerabilities.
 *
 * @author Security Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WebDataBinder Configuration Tests")
public class WebDataBinderConfigTest {

    private WebDataBinderConfig webDataBinderConfig;

    @Mock
    private WebDataBinder binder;

    @Mock
    private NativeWebRequest webRequest;

    @Mock
    private HandlerMethod handlerMethod;

    @BeforeEach
    void setUp() {
        webDataBinderConfig = new WebDataBinderConfig();
    }

    // ==================== RuleController Tests ====================

    @Test
    @DisplayName("RuleController deployRules should whitelist correct fields")
    void testRuleControllerDeployRulesWhitelist() {
        // Arrange
        configureHandlerMethod("RuleController", "deployRules");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("name", "description", "content", "version", "metadata");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    @Test
    @DisplayName("RuleController executeRules should whitelist only execution fields")
    void testRuleControllerExecuteRulesWhitelist() {
        // Arrange
        configureHandlerMethod("RuleController", "executeRules");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("ruleSetId", "facts");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== AuthController Tests ====================

    @Test
    @DisplayName("AuthController login should whitelist only username and password")
    void testAuthControllerLoginWhitelist() {
        // Arrange
        configureHandlerMethod("AuthController", "authenticateUser");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("username", "password");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    @Test
    @DisplayName("AuthController signup should whitelist user registration fields")
    void testAuthControllerSignupWhitelist() {
        // Arrange
        configureHandlerMethod("AuthController", "registerUser");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("username", "email", "password", "firstName", "lastName", "roles");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== FinancialMetricsController Tests ====================

    @Test
    @DisplayName("FinancialMetricsController calculateMetrics should whitelist metrics fields")
    void testFinancialMetricsControllerCalculateWhitelist() {
        // Arrange
        configureHandlerMethod("FinancialMetricsController", "calculateMetrics");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(
                "quoteId", "customerId", "monthlyPrice", "durationInMonths",
                "expectedDuration", "customerType", "subscriptionType", "basePrice",
                "customerTenureMonths", "productId");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== AIRuleController Tests ====================

    @Test
    @DisplayName("AIRuleController createRule should whitelist AI rule creation fields")
    void testAIRuleControllerCreateRuleWhitelist() {
        // Arrange
        configureHandlerMethod("AIRuleController", "createRule");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(
                "businessRequirement", "ruleType", "ruleName", "testFacts",
                "includeDocumentation", "generateTestCases", "tags");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== Security Tests ====================

    @Test
    @DisplayName("Should set autoGrowNestedPaths to false for all controllers")
    void testAutoGrowNestedPathsDisabled() {
        // Test RuleController
        configureHandlerMethod("RuleController", "deployRules");
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);
        verify(binder).setAutoGrowNestedPaths(false);

        // Reset and test AuthController
        reset(binder);
        configureHandlerMethod("AuthController", "authenticateUser");
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);
        verify(binder).setAutoGrowNestedPaths(false);

        // Reset and test FinancialMetricsController
        reset(binder);
        configureHandlerMethod("FinancialMetricsController", "calculateMetrics");
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);
        verify(binder).setAutoGrowNestedPaths(false);
    }

    @Test
    @DisplayName("Unknown controller should use default restrictive whitelist")
    void testUnknownControllerUsesDefaultWhitelist() {
        // Arrange
        configureHandlerMethod("UnknownController", "someMethod");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(); // Empty whitelist - no fields allowed
        verify(binder).setAutoGrowNestedPaths(false);
    }

    @Test
    @DisplayName("Null HandlerMethod should use default restrictive whitelist")
    void testNullHandlerMethodUsesDefaultWhitelist() {
        // Act
        webDataBinderConfig.initBinder(binder, webRequest, null);

        // Assert
        verify(binder).setAllowedFields(); // Empty whitelist - no fields allowed
    }

    @Test
    @DisplayName("GET endpoints should have empty whitelist")
    void testGetEndpointsHaveEmptyWhitelist() {
        // Arrange - RuleController GET method
        configureHandlerMethod("RuleController", "getRuleSetMetadata");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(); // Empty whitelist for GET
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== Helper Methods ====================

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void configureHandlerMethod(String controllerName, String methodName) {
        // Use real controller classes instead of mocking Class.class
        // Mockito cannot mock final classes like Class.class
        Class controllerClass = switch (controllerName) {
            case "RuleController" -> com.example.pricerulesaidrools.drools.controller.RuleController.class;
            case "AuthController" -> com.example.pricerulesaidrools.security.controller.AuthController.class;
            case "FinancialMetricsController" ->
                com.example.pricerulesaidrools.controller.FinancialMetricsController.class;
            case "AIRuleController" -> com.example.pricerulesaidrools.ai.controller.AIRuleController.class;
            case "HealthController" -> Object.class; // Use Object.class for non-existent controllers
            default -> Object.class; // For UnknownController and other test cases
        };

        when(handlerMethod.getBeanType()).thenReturn(controllerClass);
        // Mock Method is still acceptable since Method itself isn't final
        Method mockMethod = Mockito.mock(Method.class, Mockito.withSettings().strictness(Strictness.LENIENT));
        when(mockMethod.getName()).thenReturn(methodName);
        when(handlerMethod.getMethod()).thenReturn(mockMethod);
    }
}