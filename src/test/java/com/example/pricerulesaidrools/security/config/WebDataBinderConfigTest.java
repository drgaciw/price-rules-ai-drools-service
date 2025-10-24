package com.example.pricerulesaidrools.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import com.example.pricerulesaidrools.drools.controller.RuleController;
import com.example.pricerulesaidrools.security.controller.AuthController;
import com.example.pricerulesaidrools.controller.FinancialMetricsController;
import com.example.pricerulesaidrools.ai.controller.AIRuleController;

import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

/**
 * Unit tests for WebDataBinder field whitelisting configuration.
 *
 * These tests verify that the WebDataBinder correctly configures field whitelisting
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
    void testRuleControllerDeployRulesWhitelist() throws Exception {
        // Arrange
        when(handlerMethod.getBeanType()).thenReturn((Class) RuleController.class);
        Method method = RuleController.class.getDeclaredMethod("deployRules", Object.class);
        when(handlerMethod.getMethod()).thenReturn(method);

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("name", "description", "content", "version", "metadata");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    @Test
    @DisplayName("RuleController executeRules should whitelist only execution fields")
    void testRuleControllerExecuteRulesWhitelist() throws Exception {
        // Arrange
        when(handlerMethod.getBeanType()).thenReturn((Class) RuleController.class);
        Method method = Object.class.getDeclaredMethod("toString"); // Mock method
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("executeRules");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("ruleSetId", "facts");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== AuthController Tests ====================

    @Test
    @DisplayName("AuthController login should whitelist only username and password")
    void testAuthControllerLoginWhitelist() throws Exception {
        // Arrange
        when(handlerMethod.getBeanType()).thenReturn((Class) AuthController.class);
        Method method = Object.class.getDeclaredMethod("toString");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("authenticateUser");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("username", "password");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    @Test
    @DisplayName("AuthController signup should whitelist user registration fields")
    void testAuthControllerSignupWhitelist() throws Exception {
        // Arrange
        when(handlerMethod.getBeanType()).thenReturn((Class) AuthController.class);
        Method method = Object.class.getDeclaredMethod("toString");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("registerUser");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields("username", "email", "password", "firstName", "lastName", "roles");
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== FinancialMetricsController Tests ====================

    @Test
    @DisplayName("FinancialMetricsController calculateMetrics should whitelist metrics fields")
    void testFinancialMetricsControllerCalculateWhitelist() throws Exception {
        // Arrange
        when(handlerMethod.getBeanType()).thenReturn((Class) FinancialMetricsController.class);
        Method method = Object.class.getDeclaredMethod("toString");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("calculateMetrics");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(
            "quoteId", "customerId", "monthlyPrice", "durationInMonths",
            "expectedDuration", "customerType", "subscriptionType", "basePrice",
            "customerTenureMonths", "productId"
        );
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== AIRuleController Tests ====================

    @Test
    @DisplayName("AIRuleController createRule should whitelist AI rule creation fields")
    void testAIRuleControllerCreateRuleWhitelist() throws Exception {
        // Arrange
        when(handlerMethod.getBeanType()).thenReturn((Class) AIRuleController.class);
        Method method = Object.class.getDeclaredMethod("toString");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("createRule");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(
            "businessRequirement", "ruleType", "ruleName", "testFacts",
            "includeDocumentation", "generateTestCases", "tags"
        );
        verify(binder).setAutoGrowNestedPaths(false);
    }

    // ==================== Security Tests ====================

    @Test
    @DisplayName("Should set autoGrowNestedPaths to false for all controllers")
    void testAutoGrowNestedPathsDisabled() throws Exception {
        // Test RuleController
        when(handlerMethod.getBeanType()).thenReturn((Class) RuleController.class);
        Method method = Object.class.getDeclaredMethod("toString");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("deployRules");

        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);
        verify(binder).setAutoGrowNestedPaths(false);

        // Reset and test AuthController
        reset(binder);
        when(handlerMethod.getBeanType()).thenReturn((Class) AuthController.class);
        when(method.getName()).thenReturn("authenticateUser");

        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);
        verify(binder).setAutoGrowNestedPaths(false);

        // Reset and test FinancialMetricsController
        reset(binder);
        when(handlerMethod.getBeanType()).thenReturn((Class) FinancialMetricsController.class);
        when(method.getName()).thenReturn("calculateMetrics");

        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);
        verify(binder).setAutoGrowNestedPaths(false);
    }

    @Test
    @DisplayName("Unknown controller should use default restrictive whitelist")
    void testUnknownControllerUsesDefaultWhitelist() throws Exception {
        // Create a mock class that doesn't match any known controller
        class UnknownController {}

        when(handlerMethod.getBeanType()).thenReturn((Class) UnknownController.class);
        Method method = Object.class.getDeclaredMethod("toString");
        when(handlerMethod.getMethod()).thenReturn(method);

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
    void testGetEndpointsHaveEmptyWhitelist() throws Exception {
        // Arrange - RuleController GET method
        when(handlerMethod.getBeanType()).thenReturn((Class) RuleController.class);
        Method method = Object.class.getDeclaredMethod("toString");
        when(handlerMethod.getMethod()).thenReturn(method);
        when(method.getName()).thenReturn("getRuleSetMetadata");

        // Act
        webDataBinderConfig.initBinder(binder, webRequest, handlerMethod);

        // Assert
        verify(binder).setAllowedFields(); // Empty whitelist for GET
        verify(binder).setAutoGrowNestedPaths(false);
    }
}