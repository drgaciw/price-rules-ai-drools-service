package com.example.pricerulesaidrools.security;

import com.example.pricerulesaidrools.security.config.WebSecurityConfig;
import com.example.pricerulesaidrools.security.jwt.JwtAuthenticationEntryPoint;
import com.example.pricerulesaidrools.security.jwt.JwtAuthenticationFilter;
import com.example.pricerulesaidrools.security.jwt.JwtUtils;
import com.example.pricerulesaidrools.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security Tests for CVE-2024-38821 Prevention
 *
 * This test suite verifies that the application is protected against:
 * - CVE-2024-38821: Static resource bypass vulnerability
 * - Path traversal attacks
 * - Unauthorized access to static resources
 * - Common security misconfigurations
 *
 * @author Security Team
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.security.debug=true",
    "logging.level.org.springframework.security=DEBUG"
})
@DisplayName("Web Security Tests - CVE-2024-38821 Prevention")
public class WebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    /**
     * Test that static resource paths are explicitly denied
     * This is the primary defense against CVE-2024-38821
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "/static/test.js",
        "/static/css/style.css",
        "/static/../etc/passwd",
        "/resources/test.html",
        "/resources/images/logo.png",
        "/public/index.html",
        "/public/js/app.js",
        "/webjars/jquery/jquery.min.js"
    })
    @DisplayName("Should deny access to static resource paths")
    void testStaticResourcesAreDenied(String path) throws Exception {
        mockMvc.perform(get(path))
            .andExpect(status().isForbidden());
    }

    /**
     * Test path traversal attack prevention
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "/api/../static/test.js",
        "/api/../../etc/passwd",
        "/api/%2e%2e/static/test.js",
        "/api/%2e%2e%2f%2e%2e%2fetc%2fpasswd",
        "/api/./././static/test.js",
        "/api//static/test.js",
        "/api/\\/static/test.js",
        "/api/static\\test.js"
    })
    @DisplayName("Should block path traversal attempts")
    void testPathTraversalBlocked(String path) throws Exception {
        mockMvc.perform(get(path))
            .andExpect(status().is4xxClientError());
    }

    /**
     * Test that file extensions are blocked when accessed directly
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "/test.js",
        "/style.css",
        "/index.html",
        "/config.json",
        "/api/test.js",
        "/api/style.css"
    })
    @DisplayName("Should block direct access to files with common extensions")
    void testFileExtensionsBlocked(String path) throws Exception {
        mockMvc.perform(get(path))
            .andExpect(status().isForbidden());
    }

    /**
     * Test that authentication endpoints remain accessible
     */
    @Test
    @DisplayName("Should allow access to authentication endpoints")
    void testAuthEndpointsAccessible() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\"}"))
            .andExpect(status().isOk().or(status().isUnauthorized()));

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test\",\"password\":\"test\",\"email\":\"test@test.com\"}"))
            .andExpect(status().isOk().or(status().isBadRequest()));
    }

    /**
     * Test that health endpoints remain accessible
     */
    @Test
    @DisplayName("Should allow access to health endpoints")
    void testHealthEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
    }

    /**
     * Test that protected actuator endpoints require authentication
     */
    @Test
    @DisplayName("Should require authentication for protected actuator endpoints")
    void testProtectedActuatorEndpoints() throws Exception {
        mockMvc.perform(get("/actuator/env"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/beans"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/actuator/configprops"))
            .andExpect(status().isUnauthorized());
    }

    /**
     * Test that protected actuator endpoints require ADMIN role
     */
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should require ADMIN role for actuator management endpoints")
    void testActuatorRequiresAdminRole() throws Exception {
        mockMvc.perform(get("/actuator/env"))
            .andExpect(status().isForbidden());
    }

    /**
     * Test that admin users can access actuator endpoints
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should allow ADMIN users to access actuator endpoints")
    void testAdminCanAccessActuator() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
            .andExpect(status().isOk());
    }

    /**
     * Test security headers are properly set
     */
    @Test
    @DisplayName("Should set security headers on responses")
    void testSecurityHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(header().exists("X-Content-Type-Options"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().exists("X-Frame-Options"))
            .andExpect(header().string("X-Frame-Options", "DENY"))
            .andExpect(header().exists("X-XSS-Protection"))
            .andExpect(header().exists("Content-Security-Policy"))
            .andExpect(header().exists("Referrer-Policy"))
            .andExpect(header().exists("Permissions-Policy"));
    }

    /**
     * Test HTTPS-only headers in production
     */
    @Test
    @DisplayName("Should set HSTS header")
    void testHSTSHeader() throws Exception {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(header().exists("Strict-Transport-Security"));
    }

    /**
     * Test that API endpoints require authentication
     */
    @Test
    @DisplayName("Should require authentication for API endpoints")
    void testApiEndpointsRequireAuth() throws Exception {
        mockMvc.perform(get("/api/rules"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/pricing"))
            .andExpect(status().isUnauthorized());
    }

    /**
     * Test null byte injection prevention
     */
    @Test
    @DisplayName("Should block null byte injection attempts")
    void testNullByteInjectionBlocked() throws Exception {
        mockMvc.perform(get("/api/test%00.js"))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test URL encoding attacks are blocked
     */
    @ParameterizedTest
    @ValueSource(strings = {
        "/api/%252e%252e/static/test.js",
        "/api/%25%32%65%25%32%65/static/test.js",
        "/api/%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd"
    })
    @DisplayName("Should block URL encoding attacks")
    void testUrlEncodingAttacksBlocked(String path) throws Exception {
        mockMvc.perform(get(path))
            .andExpect(status().isBadRequest());
    }

    /**
     * Test that only allowed HTTP methods are permitted
     */
    @Test
    @DisplayName("Should block non-allowed HTTP methods")
    void testBlockedHttpMethods() throws Exception {
        mockMvc.perform(request("TRACE", "/api/test"))
            .andExpect(status().isMethodNotAllowed());

        mockMvc.perform(request("PATCH", "/api/test"))
            .andExpect(status().isMethodNotAllowed());
    }

    /**
     * Test CORS configuration
     */
    @Test
    @WithMockUser
    @DisplayName("Should handle CORS preflight requests")
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/rules")
                .header("Origin", "https://app.yourdomain.com")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
            .andExpect(status().isOk())
            .andExpect(header().exists("Access-Control-Allow-Origin"))
            .andExpect(header().exists("Access-Control-Allow-Methods"))
            .andExpect(header().exists("Access-Control-Allow-Headers"));
    }
}