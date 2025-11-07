package com.example.pricerulesaidrools.controller;

import com.example.pricerulesaidrools.dto.FinancialMetricsRequest;
import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.service.FinancialMetricsService;
import com.example.pricerulesaidrools.service.MetricsHistoryService;
import com.example.pricerulesaidrools.service.PricingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialMetricsController.class)
class FinancialMetricsControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private FinancialMetricsService metricsService;

        @MockitoBean
        private PricingService pricingService;

        @MockitoBean
        private MetricsHistoryService historyService;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        @WithMockUser(roles = "ADMIN")
        void calculateMetrics_ShouldReturnMetricsResponse() throws Exception {
                // Given
                FinancialMetricsRequest request = FinancialMetricsRequest.builder()
                                .quoteId("Q123")
                                .customerId("C123")
                                .monthlyPrice(new BigDecimal("1000"))
                                .durationInMonths(12)
                                .basePrice(new BigDecimal("12000"))
                                .customerType("ENTERPRISE")
                                .subscriptionType("ANNUAL")
                                .build();

                FinancialMetrics metrics = FinancialMetrics.builder()
                                .customerId("C123")
                                .arr(new BigDecimal("12000"))
                                .tcv(new BigDecimal("12000"))
                                .acv(new BigDecimal("12000"))
                                .clv(new BigDecimal("240000"))
                                .churnRiskScore(new BigDecimal("0.02"))
                                .build();

                when(metricsService.calculateMetrics(any(Quote.class))).thenReturn(metrics);

                // When & Then
                mockMvc.perform(post("/api/v1/financial-metrics/calculate")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.quoteId").value("Q123"))
                                .andExpect(jsonPath("$.customerId").value("C123"))
                                .andExpect(jsonPath("$.metrics.arr").value(12000))
                                .andExpect(jsonPath("$.metrics.tcv").value(12000))
                                .andExpect(jsonPath("$.metrics.acv").value(12000))
                                .andExpect(jsonPath("$.metrics.clv").value(240000))
                                .andExpect(jsonPath("$.pricing.basePrice").value(12000))
                                .andExpect(jsonPath("$.pricing.finalPrice").value(12000))
                                .andExpect(jsonPath("$.pricing.currency").value("USD"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void calculateMetrics_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
                // Given - Invalid request with missing required fields
                FinancialMetricsRequest request = FinancialMetricsRequest.builder()
                                .quoteId("") // Empty quote ID should fail validation
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/financial-metrics/calculate")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getHistoricalMetrics_ShouldReturnHistoricalData() throws Exception {
                // Given
                FinancialMetrics metrics = FinancialMetrics.builder()
                                .customerId("C123")
                                .arr(new BigDecimal("12000"))
                                .tcv(new BigDecimal("12000"))
                                .acv(new BigDecimal("12000"))
                                .clv(new BigDecimal("240000"))
                                .churnRiskScore(new BigDecimal("0.02"))
                                .build();

                when(historyService.getHistoricalMetrics("C123", Duration.ofDays(365)))
                                .thenReturn(Collections.singletonList(metrics));

                // When & Then
                mockMvc.perform(get("/api/v1/financial-metrics/history/C123")
                                .param("days", "365"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].customerId").value("C123"))
                                .andExpect(jsonPath("$[0].metrics.arr").value(12000));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void getHistoricalMetrics_WhenNoDataFound_ShouldReturnNotFound() throws Exception {
                // Given
                when(historyService.getHistoricalMetrics("C123", Duration.ofDays(365)))
                                .thenReturn(Collections.emptyList());

                // When & Then
                mockMvc.perform(get("/api/v1/financial-metrics/history/C123")
                                .param("days", "365"))
                                .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        void calculateChurnRisk_ShouldReturnRiskScore() throws Exception {
                // Given
                when(metricsService.calculateChurnRisk("C123")).thenReturn(25);

                // When & Then
                mockMvc.perform(get("/api/v1/financial-metrics/churn-risk/C123"))
                                .andExpect(status().isOk())
                                .andExpect(content().string("25"));
        }

        @Test
        void calculateMetrics_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
                // Given
                FinancialMetricsRequest request = FinancialMetricsRequest.builder()
                                .quoteId("Q123")
                                .customerId("C123")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/financial-metrics/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "USER") // User without proper role
        void calculateMetrics_WithInsufficientRole_ShouldReturnForbidden() throws Exception {
                // Given
                FinancialMetricsRequest request = FinancialMetricsRequest.builder()
                                .quoteId("Q123")
                                .customerId("C123")
                                .build();

                // When & Then
                mockMvc.perform(post("/api/v1/financial-metrics/calculate")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }
}