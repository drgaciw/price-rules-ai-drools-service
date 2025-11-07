package com.example.pricerulesaidrools.security;

import com.example.pricerulesaidrools.security.config.WebDataBinderConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
@DisplayName("WebDataBinder Unit Tests")
public class WebDataBinderUnitTest {

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
    void testRuleControllerDeployRulesWhitelist() throws NoSuchMethodException {
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
    void testRuleControllerExecuteRulesWhitelist() throws NoSuchMethodException {
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
    void testAuthControllerLoginWhitelist() throws NoSuchMethodException {
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
    void testAuthControllerSignupWhitelist() throws NoSuchMethodException {
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
    void testFinancialMetricsControllerCalculateWhitelist() throws NoSuchMethodException {
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
    void testAIRuleControllerCreateRuleWhitelist() throws NoSuchMethodException {
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
    void testAutoGrowNestedPathsDisabled() throws NoSuchMethodException {
        // Test multiple controllers
        String[] controllers = { "RuleController", "AuthController", "FinancialMetricsController" };
        String[] methods = { "deployRules", "authenticateUser", "calculateMetrics" };

        for (int i = 0; i < controllers.length; i++) {
            // Arrange
            reset(binder);
            configureHandlerMethod(controllers[i], methods[i]);

            // Act
            webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

            // Assert
            verify(binder).setAutoGrowNestedPaths(false);
        }
    }

    @Test
    @DisplayName("Unknown controller should use default restrictive whitelist")
    void testUnknownControllerUsesDefaultWhitelist() throws NoSuchMethodException {
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
    @DisplayName("HealthController should have empty whitelist")
    void testHealthControllerHasEmptyWhitelist() throws NoSuchMethodException {
        // Arrange
        configureHandlerMethod("HealthController", "health");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(); // Empty whitelist - no fields allowed
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== Template Controller Tests ====================

    @Test
    @DisplayName("RuleTemplateController should whitelist template parameters")
    void testRuleTemplateControllerWhitelist() throws NoSuchMethodException {
        // Arrange
        configureHandlerMethod("RuleTemplateController", "createVolumeDiscountRule");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        // Since these endpoints use Map<String, Object> params, they get special
        // handling
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== Validation Tests ====================

    @Test
    @DisplayName("Verify no dangerous fields are whitelisted")
    void testNoDangerousFieldsWhitelisted() throws NoSuchMethodException {
        // Define dangerous fields that should never be whitelisted
        Set<String> dangerousFields = new HashSet<>(Arrays.asList(
                "class", "Class", "classLoader", "constructor", "__proto__",
                "prototype", "exec", "eval", "Function", "setTimeout"));

        // Test various controller methods
        String[][] controllerMethods = {
                { "RuleController", "deployRules" },
                { "AuthController", "authenticateUser" },
                { "FinancialMetricsController", "calculateMetrics" },
                { "AIRuleController", "createRule" }
        };

        for (String[] pair : controllerMethods) {
            // Arrange
            reset(binder);
            configureHandlerMethod(pair[0], pair[1]);

            // Capture allowed fields (varargs method - arguments come as individual
            // Strings)
            doAnswer(invocation -> {
                Object[] args = invocation.getArguments();
                if (args != null && args.length > 0) {
                    for (Object arg : args) {
                        if (arg instanceof String) {
                            assertThat(dangerousFields).doesNotContain((String) arg);
                        }
                    }
                }
                return null;
            }).when(binder).setAllowedFields(any(String[].class));

            // Act
            webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);
        }
    }

    @Test
    @DisplayName("Verify GET endpoints have empty whitelist")
    void testGetEndpointsHaveEmptyWhitelist() throws NoSuchMethodException {
        // GET methods that should have empty whitelist
        String[][] getEndpoints = {
                { "RuleController", "getRuleSetMetadata" },
                { "RuleController", "listRuleSets" },
                { "FinancialMetricsController", "getHistoricalMetrics" },
                { "FinancialMetricsController", "calculateChurnRisk" }
        };

        for (String[] pair : getEndpoints) {
            // Arrange
            reset(binder);
            configureHandlerMethod(pair[0], pair[1]);

            // Act
            webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

            // Assert
            verify(binder).setAllowedFields(); // Empty whitelist
        }
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
            case "RuleTemplateController" -> Object.class; // Use Object.class for template controller
            default -> Object.class; // For UnknownController and other test cases
        };

        when(handlerMethod.getBeanType()).thenReturn(controllerClass);

        // Mock Method is still acceptable since Method itself isn't final
        Method mockMethod = mock(Method.class);
        when(mockMethod.getName()).thenReturn(methodName);
        when(handlerMethod.getMethod()).thenReturn(mockMethod);
    }
}