package com.example.pricerulesaidrools.controller;

import com.example.pricerulesaidrools.dto.FinancialMetricsRequest;
import com.example.pricerulesaidrools.dto.FinancialMetricsResponse;
import com.example.pricerulesaidrools.dto.PricingStrategyRequest;
import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.service.FinancialMetricsService;
import com.example.pricerulesaidrools.service.MetricsHistoryService;
import com.example.pricerulesaidrools.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/financial-metrics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Financial Metrics API", description = "APIs for financial metrics calculations and pricing")
public class FinancialMetricsController {
    
    private final FinancialMetricsService metricsService;
    private final PricingService pricingService;
    private final MetricsHistoryService historyService;
    
    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR')")
    @Operation(summary = "Calculate financial metrics", description = "Calculate financial metrics for a quote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics calculated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FinancialMetricsResponse> calculateMetrics(
            @Valid @RequestBody FinancialMetricsRequest request) {
        
        log.info("Calculating financial metrics for quote ID: {}", request.getQuoteId());
        
        // Create Quote from request
        Quote quote = Quote.builder()
                .quoteId(request.getQuoteId())
                .customerId(request.getCustomerId())
                .monthlyPrice(request.getMonthlyPrice())
                .durationInMonths(request.getDurationInMonths())
                .expectedDuration(request.getExpectedDuration())
                .customerType(request.getCustomerType())
                .subscriptionType(request.getSubscriptionType())
                .basePrice(request.getBasePrice())
                .customerTenureMonths(request.getCustomerTenureMonths())
                .productId(request.getProductId())
                .build();
        
        // Calculate metrics
        FinancialMetrics metrics = metricsService.calculateMetrics(quote);
        
        // Build response
        FinancialMetricsResponse response = FinancialMetricsResponse.builder()
                .quoteId(request.getQuoteId())
                .customerId(request.getCustomerId())
                .metrics(FinancialMetricsResponse.Metrics.builder()
                        .arr(metrics.getArr())
                        .tcv(metrics.getTcv())
                        .acv(metrics.getAcv())
                        .clv(metrics.getClv())
                        .churnScore(metrics.getChurnRiskScore())
                        .build())
                .pricing(FinancialMetricsResponse.Pricing.builder()
                        .basePrice(request.getBasePrice())
                        .finalPrice(request.getBasePrice()) // No pricing strategy applied yet
                        .currency("USD")
                        .appliedRules(new String[0])
                        .build())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/apply-strategy")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR')")
    @Operation(summary = "Apply pricing strategy", description = "Apply a pricing strategy to a quote")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Strategy applied successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Quote not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<FinancialMetricsResponse> applyPricingStrategy(
            @Valid @RequestBody PricingStrategyRequest request) {
        
        log.info("Applying pricing strategy {} to quote ID: {}", 
                request.getStrategy(), request.getQuoteId());
        
        try {
            // Apply pricing strategy
            Quote pricedQuote = pricingService.applyPricingStrategy(
                    request.getQuoteId(), request.getStrategy());
            
            // Get metrics for the quote
            FinancialMetrics metrics = metricsService.calculateMetrics(pricedQuote);
            
            // Build response
            FinancialMetricsResponse response = FinancialMetricsResponse.builder()
                    .quoteId(pricedQuote.getQuoteId())
                    .customerId(pricedQuote.getCustomerId())
                    .metrics(FinancialMetricsResponse.Metrics.builder()
                            .arr(metrics.getArr())
                            .tcv(metrics.getTcv())
                            .acv(metrics.getAcv())
                            .clv(metrics.getClv())
                            .churnScore(metrics.getChurnRiskScore())
                            .build())
                    .pricing(FinancialMetricsResponse.Pricing.builder()
                            .basePrice(pricedQuote.getBasePrice())
                            .finalPrice(pricedQuote.getFinalPrice())
                            .currency("USD")
                            .appliedRules(new String[]{request.getStrategy()})
                            .build())
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.error("Error applying pricing strategy", e);
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/history/{customerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_MONITOR')")
    @Operation(summary = "Get historical metrics", description = "Get historical financial metrics for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historical metrics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<FinancialMetricsResponse>> getHistoricalMetrics(
            @Parameter(description = "Customer ID") @PathVariable String customerId,
            @Parameter(description = "Time period in days") @RequestParam(defaultValue = "365") int days) {
        
        log.info("Getting historical metrics for customer ID: {} over {} days", customerId, days);
        
        // Get historical metrics
        List<FinancialMetrics> historicalMetrics = 
                historyService.getHistoricalMetrics(customerId, Duration.ofDays(days));
        
        if (historicalMetrics.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Map to response DTOs
        List<FinancialMetricsResponse> responses = historicalMetrics.stream()
                .map(metrics -> FinancialMetricsResponse.builder()
                        .customerId(customerId)
                        .metrics(FinancialMetricsResponse.Metrics.builder()
                                .arr(metrics.getArr())
                                .tcv(metrics.getTcv())
                                .acv(metrics.getAcv())
                                .clv(metrics.getClv())
                                .churnScore(metrics.getChurnRiskScore())
                                .build())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/churn-risk/{customerId}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER') or hasRole('ROLE_RULE_EXECUTOR') or hasRole('ROLE_MONITOR')")
    @Operation(summary = "Calculate churn risk", description = "Calculate churn risk for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Churn risk calculated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Integer> calculateChurnRisk(
            @Parameter(description = "Customer ID") @PathVariable String customerId) {
        
        log.info("Calculating churn risk for customer ID: {}", customerId);
        
        Integer churnRisk = metricsService.calculateChurnRisk(customerId);
        return ResponseEntity.ok(churnRisk);
    }
}