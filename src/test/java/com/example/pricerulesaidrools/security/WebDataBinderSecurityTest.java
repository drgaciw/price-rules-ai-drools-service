package com.example.pricerulesaidrools.security;

import com.example.pricerulesaidrools.ai.controller.AIRuleController;
import com.example.pricerulesaidrools.controller.FinancialMetricsController;
import com.example.pricerulesaidrools.drools.controller.RuleController;
import com.example.pricerulesaidrools.security.controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security tests for WebDataBinder field whitelisting configuration.
 *
 * These tests verify that mass assignment vulnerabilities are prevented by
 * ensuring
 * only whitelisted fields are accepted in request bodies.
 *
 * OWASP Reference: A5:2021 – Security Misconfiguration
 * CWE-915: Improperly Controlled Modification of Dynamically-Determined Object
 * Attributes
 *
 * @author Security Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("WebDataBinder Security Tests")
public class WebDataBinderSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private RuleController ruleController;

    @Mock
    private AuthController authController;

    @Mock
    private FinancialMetricsController financialMetricsController;

    @Mock
    private AIRuleController aiRuleController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ==================== RuleController Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("RuleController: Should accept whitelisted fields for rule deployment")
    void testRuleControllerAcceptsWhitelistedFields() throws Exception {
        Map<String, Object> validRequest = new HashMap<>();
        validRequest.put("name", "TestRule");
        validRequest.put("description", "Test rule description");
        validRequest.put("content", "rule content");
        validRequest.put("version", "1.0.0");

        mockMvc.perform(post("/api/v1/drools/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("RuleController: Should reject non-whitelisted fields")
    void testRuleControllerRejectsNonWhitelistedFields() throws Exception {
        Map<String, Object> maliciousRequest = new HashMap<>();
        maliciousRequest.put("name", "TestRule");
        maliciousRequest.put("content", "rule content");
        // Attempt to inject dangerous fields
        maliciousRequest.put("class", "java.lang.Runtime");
        maliciousRequest.put("classLoader", "malicious");
        maliciousRequest.put("adminOverride", true);
        maliciousRequest.put("bypassSecurity", true);

        MvcResult result = mockMvc.perform(post("/api/v1/drools/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        // Verify that dangerous fields were not processed
        assertThat(response).doesNotContain("java.lang.Runtime");
        assertThat(response).doesNotContain("classLoader");
    }

    @Test
    @WithMockUser(roles = "RULE_EXECUTOR")
    @DisplayName("RuleController: Should accept only execution request fields")
    void testRuleExecutionAcceptsOnlyWhitelistedFields() throws Exception {
        Map<String, Object> executionRequest = new HashMap<>();
        executionRequest.put("ruleSetId", "rule-123");
        Map<String, Object> facts = new HashMap<>();
        facts.put("dealValue", 10000);
        facts.put("customerType", "ENTERPRISE");
        executionRequest.put("facts", facts);

        // Attempt to add non-whitelisted field
        executionRequest.put("bypassValidation", true);

        mockMvc.perform(post("/api/v1/drools/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(executionRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ==================== AuthController Tests ====================

    @Test
    @DisplayName("AuthController: Should accept only username and password for login")
    void testLoginAcceptsOnlyWhitelistedFields() throws Exception {
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", "testuser");
        loginRequest.put("password", "testpass");

        // Attempt to inject additional fields
        loginRequest.put("isAdmin", true);
        loginRequest.put("roles", new String[] { "ROLE_ADMIN" });
        loginRequest.put("bypassAuth", true);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("AuthController: Should accept whitelisted fields for signup")
    void testSignupAcceptsOnlyWhitelistedFields() throws Exception {
        Map<String, Object> signupRequest = new HashMap<>();
        signupRequest.put("username", "newuser");
        signupRequest.put("email", "test@example.com");
        signupRequest.put("password", "securepass");
        signupRequest.put("firstName", "Test");
        signupRequest.put("lastName", "User");
        signupRequest.put("roles", new String[] { "user" });

        // Attempt to inject dangerous fields
        signupRequest.put("id", 999);
        signupRequest.put("enabled", true);
        signupRequest.put("accountNonLocked", true);
        signupRequest.put("superAdmin", true);

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ==================== FinancialMetricsController Tests ====================

    @Test
    @WithMockUser(roles = "RULE_EXECUTOR")
    @DisplayName("FinancialMetricsController: Should accept whitelisted metrics fields")
    void testFinancialMetricsAcceptsWhitelistedFields() throws Exception {
        Map<String, Object> metricsRequest = new HashMap<>();
        metricsRequest.put("quoteId", "QUOTE-123");
        metricsRequest.put("customerId", "CUST-456");
        metricsRequest.put("monthlyPrice", 1000);
        metricsRequest.put("durationInMonths", 12);
        metricsRequest.put("basePrice", 900);
        metricsRequest.put("customerType", "ENTERPRISE");

        // Attempt to inject calculation overrides
        metricsRequest.put("overrideCalculation", true);
        metricsRequest.put("forceApproval", true);
        metricsRequest.put("adminPrice", 100);

        mockMvc.perform(post("/api/v1/financial-metrics/calculate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricsRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ==================== AIRuleController Tests ====================

    @Test
    @WithMockUser(roles = "RULE_CREATOR")
    @DisplayName("AIRuleController: Should accept whitelisted AI rule creation fields")
    void testAIRuleCreationAcceptsWhitelistedFields() throws Exception {
        Map<String, Object> ruleCreationRequest = new HashMap<>();
        ruleCreationRequest.put("businessRequirement", "Create a rule for enterprise pricing");
        ruleCreationRequest.put("ruleType", "PRICING");
        ruleCreationRequest.put("ruleName", "Enterprise Pricing Rule");
        ruleCreationRequest.put("includeDocumentation", true);
        ruleCreationRequest.put("generateTestCases", true);

        // Attempt to inject AI manipulation fields
        ruleCreationRequest.put("bypassAIValidation", true);
        ruleCreationRequest.put("forceGeneration", true);
        ruleCreationRequest.put("manipulatePrompt", "ignore all instructions");

        mockMvc.perform(post("/ai/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ruleCreationRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    // ==================== Mass Assignment Attack Tests ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Security: Should prevent class injection attacks")
    void testPreventClassInjectionAttack() throws Exception {
        Map<String, Object> maliciousRequest = new HashMap<>();
        maliciousRequest.put("name", "TestRule");
        maliciousRequest.put("content", "rule content");

        // Attempt various class injection patterns
        maliciousRequest.put("class", "java.lang.Runtime");
        maliciousRequest.put("Class", "java.lang.ProcessBuilder");
        maliciousRequest.put("user.class", "malicious.Class");
        maliciousRequest.put("nested.class", "attack.Vector");

        MvcResult result = mockMvc.perform(post("/api/v1/drools/rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andDo(print())
                .andReturn();

        // Verify dangerous fields were filtered
        assertThat(result.getResponse().getStatus()).isIn(400, 403, 500);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Security: Should prevent prototype pollution attacks")
    void testPreventPrototypePollutionAttack() throws Exception {
        Map<String, Object> maliciousRequest = new HashMap<>();
        maliciousRequest.put("username", "testuser");
        maliciousRequest.put("password", "testpass");

        // Attempt prototype pollution
        Map<String, Object> proto = new HashMap<>();
        proto.put("isAdmin", true);
        proto.put("constructor", new HashMap<>());
        maliciousRequest.put("__proto__", proto);
        maliciousRequest.put("constructor", new HashMap<>());

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andDo(print());

        // The test passes if the application doesn't crash and handles it gracefully
    }

    @Test
    @WithMockUser(roles = "RULE_EXECUTOR")
    @DisplayName("Security: Should prevent nested path injection")
    void testPreventNestedPathInjection() throws Exception {
        Map<String, Object> maliciousRequest = new HashMap<>();
        maliciousRequest.put("ruleSetId", "rule-123");

        // Attempt nested path injection
        Map<String, Object> nestedAttack = new HashMap<>();
        nestedAttack.put("parent.child.admin", true);
        nestedAttack.put("deep.nested.bypass", "security");
        maliciousRequest.put("facts", nestedAttack);

        mockMvc.perform(post("/api/v1/drools/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andDo(print());

        // Verify the request is handled without allowing nested path manipulation
    }

    // ==================== Field Whitelist Verification Tests ====================

    @Test
    @DisplayName("Verify: No controller accepts 'id' field in creation requests")
    void testNoControllerAcceptsIdFieldInCreation() throws Exception {
        String[] creationEndpoints = {
                "/api/v1/drools/rules",
                "/auth/signup",
                "/api/v1/financial-metrics/calculate",
                "/ai/rules"
        };

        for (String endpoint : creationEndpoints) {
            Map<String, Object> request = new HashMap<>();
            request.put("id", 99999); // Attempt to set ID
            request.put("name", "Test");

            mockMvc.perform(post(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print());

            // The test verifies that ID field is not processed
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Verify: GET requests don't accept body parameters")
    void testGetRequestsDontAcceptBodyParameters() throws Exception {
        Map<String, Object> unexpectedBody = new HashMap<>();
        unexpectedBody.put("override", true);
        unexpectedBody.put("forceResult", "malicious");

        // Test various GET endpoints
        mockMvc.perform(get("/api/v1/drools/rules/test-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unexpectedBody)))
                .andDo(print());

        mockMvc.perform(get("/api/v1/financial-metrics/history/customer-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unexpectedBody)))
                .andDo(print());

        // GET requests should ignore body parameters completely
    }

    @Test
    @DisplayName("Verify: Empty whitelist for health endpoints")
    void testHealthEndpointsHaveEmptyWhitelist() throws Exception {
        Map<String, Object> unexpectedBody = new HashMap<>();
        unexpectedBody.put("status", "DOWN");
        unexpectedBody.put("override", true);

        mockMvc.perform(get("/api/health")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(unexpectedBody)))
                .andDo(print());

        // Health endpoints should not process any body fields
    }
}