package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.config.RoutingConfiguration;
import com.example.pricerulesaidrools.ai.dto.EnhancedPricingRequest;
import com.example.pricerulesaidrools.ai.dto.RoutingDecision;
import com.example.pricerulesaidrools.model.Customer;
import com.example.pricerulesaidrools.model.Deal;
import com.example.pricerulesaidrools.model.PricingRequest;
import com.example.pricerulesaidrools.repository.CustomerRepository;
import com.example.pricerulesaidrools.repository.DealRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of AI-orchestrated routing service for deal evaluations.
 * Routes pricing requests to specialized review paths based on configurable
 * strategies.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIRoutingServiceImpl implements AIRoutingService {

    private final RoutingConfiguration routingConfiguration;
    private final CustomerRepository customerRepository;
    private final DealRepository dealRepository;

    // Statistics tracking
    private final Map<String, AtomicLong> routeUsageCount = new ConcurrentHashMap<>();
    private final AtomicLong totalRoutingDecisions = new AtomicLong(0);
    private final AtomicLong fallbackCount = new AtomicLong(0);

    @Override
    public RoutingDecision route(PricingRequest request) {
        try {
            // Fetch customer and deal information
            Customer customer = customerRepository.findByCustomerId(request.getCustomerId())
                    .orElse(null);

            Deal deal = dealRepository.findByCustomerId(request.getCustomerId())
                    .stream()
                    .findFirst()
                    .orElse(createDefaultDeal(request));

            EnhancedPricingRequest enhancedRequest = EnhancedPricingRequest.from(request, deal, customer);

            return routeEnhanced(enhancedRequest);
        } catch (Exception e) {
            log.error("Error during routing decision for request: {}", request.getCustomerId(), e);
            return createErrorFallback(request.getCustomerId(), e.getMessage());
        }
    }

    @Override
    public RoutingDecision routeEnhanced(EnhancedPricingRequest request) {
        long startTime = System.currentTimeMillis();
        totalRoutingDecisions.incrementAndGet();

        try {
            // Evaluate all routing strategies
            Map<String, Double> strategyScores = evaluateAllStrategies(request);

            // Find the highest scoring route
            Map.Entry<String, Double> bestRoute = strategyScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (bestRoute == null || bestRoute.getValue() < getConfidenceThreshold()) {
                // Use fallback route if confidence is too low
                fallbackCount.incrementAndGet();
                return createFallbackDecision(request, bestRoute, strategyScores, startTime);
            }

            // Create successful routing decision
            return createRoutingDecision(request, bestRoute, strategyScores, startTime, false);

        } catch (Exception e) {
            log.error("Error in enhanced routing for request: {}", request.getRequestId(), e);
            return createErrorFallback(request.getRequestId(), e.getMessage());
        }
    }

    @Override
    public Map<String, Double> evaluateAllStrategies(EnhancedPricingRequest request) {
        Map<String, Double> scores = new HashMap<>();

        // Evaluate billing-review route
        double billingScore = evaluateBillingReview(request);
        scores.put("billing-review", billingScore);

        // Evaluate technical-review route
        double technicalScore = evaluateTechnicalReview(request);
        scores.put("technical-review", technicalScore);

        // Evaluate risk-review route
        double riskScore = evaluateRiskReview(request);
        scores.put("risk-review", riskScore);

        // Always include general-review as an option with base confidence
        scores.put("general-review", 0.5);

        log.debug("Strategy evaluation scores for request {}: {}", request.getRequestId(), scores);

        return scores;
    }

    /**
     * Evaluates if the request should go to billing review.
     * Condition: deal.type == 'ENTERPRISE' && deal.value > 100000
     */
    private double evaluateBillingReview(EnhancedPricingRequest request) {
        Deal deal = request.getDeal();
        if (deal == null) {
            return 0.0;
        }

        double score = 0.0;

        // Check deal type
        boolean isEnterprise = Deal.DealType.ENTERPRISE.equals(deal.getType());
        if (isEnterprise) {
            score += 0.5;
        }

        // Check deal value
        BigDecimal dealValue = deal.getValue();
        if (dealValue != null) {
            if (dealValue.compareTo(new BigDecimal("100000")) > 0) {
                score += 0.5;
                // Add bonus confidence for very large deals
                if (dealValue.compareTo(new BigDecimal("500000")) > 0) {
                    score += 0.2;
                }
            }
        }

        // Additional factors for billing review
        if (deal.getBillingTerms() != null && deal.getBillingTerms().contains("custom")) {
            score += 0.1;
        }

        return Math.min(1.0, score);
    }

    /**
     * Evaluates if the request should go to technical review.
     * Condition: deal.complexity == 'HIGH'
     */
    private double evaluateTechnicalReview(EnhancedPricingRequest request) {
        Deal deal = request.getDeal();
        if (deal == null) {
            return 0.0;
        }

        double score = 0.0;

        // Check deal complexity
        if (Deal.DealComplexity.HIGH.equals(deal.getComplexity())) {
            score = 0.9;
        } else if (Deal.DealComplexity.VERY_HIGH.equals(deal.getComplexity())) {
            score = 1.0;
        } else if (Deal.DealComplexity.MEDIUM.equals(deal.getComplexity())) {
            score = 0.4;
        }

        // Check for technical requirements
        if (deal.getTechnicalRequirements() != null && !deal.getTechnicalRequirements().isEmpty()) {
            score += 0.2;
        }

        return Math.min(1.0, score);
    }

    /**
     * Evaluates if the request should go to risk review.
     * Condition: customer.riskScore > 70
     */
    private double evaluateRiskReview(EnhancedPricingRequest request) {
        Customer customer = request.getCustomer();
        Deal deal = request.getDeal();

        double score = 0.0;

        // Check customer risk score (churn risk)
        if (customer != null && customer.getChurnRiskScore() != null) {
            double riskScore = customer.getChurnRiskScore().doubleValue();
            if (riskScore > 70) {
                score = 0.9;
            } else if (riskScore > 50) {
                score = 0.6;
            } else if (riskScore > 30) {
                score = 0.3;
            }
        }

        // Check deal risk score
        if (deal != null && deal.getRiskScore() != null) {
            if (deal.getRiskScore() > 70) {
                score = Math.max(score, 0.85);
            }
        }

        // Additional risk factors
        if (customer != null) {
            // Payment issues increase risk review need
            if (customer.getPaymentIssuesCount() != null && customer.getPaymentIssuesCount() > 2) {
                score += 0.2;
            }
            // High support tickets might indicate risk
            if (customer.getSupportTicketsCount() != null && customer.getSupportTicketsCount() > 10) {
                score += 0.1;
            }
        }

        return Math.min(1.0, score);
    }

    private RoutingDecision createRoutingDecision(EnhancedPricingRequest request,
            Map.Entry<String, Double> bestRoute,
            Map<String, Double> allScores,
            long startTime,
            boolean isFallback) {
        String route = bestRoute.getKey();
        routeUsageCount.computeIfAbsent(route, k -> new AtomicLong()).incrementAndGet();

        Deal deal = request.getDeal();
        Customer customer = request.getCustomer();

        return RoutingDecision.builder()
                .route(route)
                .confidence(bestRoute.getValue())
                .reason(generateRoutingReason(route, request))
                .timestamp(LocalDateTime.now())
                .requestId(request.getRequestId())
                .customerId(request.getPricingRequest().getCustomerId())
                .dealId(deal != null ? deal.getDealId() : null)
                .dealValue(deal != null && deal.getValue() != null ? deal.getValue().doubleValue() : null)
                .dealType(deal != null && deal.getType() != null ? deal.getType().toString() : null)
                .complexity(deal != null && deal.getComplexity() != null ? deal.getComplexity().toString() : null)
                .customerRiskScore(customer != null && customer.getChurnRiskScore() != null
                        ? customer.getChurnRiskScore().doubleValue()
                        : null)
                .isFallback(isFallback)
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .ruleScores(allScores)
                .metadata(createMetadata(request))
                .build();
    }

    private RoutingDecision createFallbackDecision(EnhancedPricingRequest request,
            Map.Entry<String, Double> bestRoute,
            Map<String, Double> allScores,
            long startTime) {
        String fallbackRoute = getFallbackRoute();
        routeUsageCount.computeIfAbsent(fallbackRoute, k -> new AtomicLong()).incrementAndGet();

        String reason = String.format("Confidence below threshold (%.2f < %.2f). Best route was %s with score %.2f",
                bestRoute != null ? bestRoute.getValue() : 0.0,
                getConfidenceThreshold(),
                bestRoute != null ? bestRoute.getKey() : "none",
                bestRoute != null ? bestRoute.getValue() : 0.0);

        Deal deal = request.getDeal();
        Customer customer = request.getCustomer();

        return RoutingDecision.builder()
                .route(fallbackRoute)
                .confidence(1.0) // Fallback always has full confidence
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .requestId(request.getRequestId())
                .customerId(request.getPricingRequest().getCustomerId())
                .dealId(deal != null ? deal.getDealId() : null)
                .dealValue(deal != null && deal.getValue() != null ? deal.getValue().doubleValue() : null)
                .dealType(deal != null && deal.getType() != null ? deal.getType().toString() : null)
                .complexity(deal != null && deal.getComplexity() != null ? deal.getComplexity().toString() : null)
                .customerRiskScore(customer != null && customer.getChurnRiskScore() != null
                        ? customer.getChurnRiskScore().doubleValue()
                        : null)
                .isFallback(true)
                .processingTimeMs(System.currentTimeMillis() - startTime)
                .ruleScores(allScores)
                .metadata(createMetadata(request))
                .build();
    }

    private String generateRoutingReason(String route, EnhancedPricingRequest request) {
        Deal deal = request.getDeal();
        Customer customer = request.getCustomer();

        switch (route) {
            case "billing-review":
                return String.format("Enterprise deal with value $%.2f requires billing review",
                        deal != null && deal.getValue() != null ? deal.getValue() : 0);
            case "technical-review":
                return String.format("Deal complexity '%s' requires technical review",
                        deal != null && deal.getComplexity() != null ? deal.getComplexity() : "UNKNOWN");
            case "risk-review":
                return String.format("Customer risk score %.1f requires risk review",
                        customer != null && customer.getChurnRiskScore() != null ? customer.getChurnRiskScore() : 0);
            case "general-review":
                return "Standard review process for general deal evaluation";
            default:
                return "Routing decision based on configured strategies";
        }
    }

    private Map<String, Object> createMetadata(EnhancedPricingRequest request) {
        Map<String, Object> metadata = new HashMap<>();

        if (request.getPricingRequest() != null) {
            metadata.put("pricingStrategy", request.getPricingRequest().getPricingStrategy());
            metadata.put("quantity", request.getPricingRequest().getQuantity());
            metadata.put("contractLengthMonths", request.getPricingRequest().getContractLengthMonths());
        }

        if (request.getCustomer() != null) {
            metadata.put("customerType", request.getCustomer().getCustomerType());
            metadata.put("customerSegment", request.getCustomer().getSegment());
        }

        return metadata;
    }

    private Deal createDefaultDeal(PricingRequest request) {
        // Create a basic deal from pricing request for routing evaluation
        return Deal.builder()
                .dealId(UUID.randomUUID().toString())
                .customerId(request.getCustomerId())
                .value(BigDecimal.valueOf(request.getBasePrice() * request.getQuantity()))
                .type(Deal.DealType.STANDARD)
                .complexity(Deal.DealComplexity.LOW)
                .quantity(request.getQuantity())
                .contractLengthMonths(request.getContractLengthMonths())
                .status(Deal.DealStatus.UNDER_REVIEW)
                .build();
    }

    private RoutingDecision createErrorFallback(String requestId, String errorMessage) {
        return RoutingDecision.builder()
                .route(getFallbackRoute())
                .confidence(1.0)
                .reason("Error during routing evaluation: " + errorMessage)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .isFallback(true)
                .processingTimeMs(0L)
                .build();
    }

    @Override
    public List<String> getAvailableRoutes() {
        return routingConfiguration.getAvailableRoutes();
    }

    @Override
    public double getConfidenceThreshold() {
        return routingConfiguration.getConfidenceThreshold();
    }

    @Override
    public void setConfidenceThreshold(double threshold) {
        if (threshold < 0.0 || threshold > 1.0) {
            throw new IllegalArgumentException("Confidence threshold must be between 0.0 and 1.0");
        }
        routingConfiguration.setConfidenceThreshold(threshold);
    }

    @Override
    public String getFallbackRoute() {
        return routingConfiguration.getFallbackRoute();
    }

    @Override
    public boolean isValidRoute(String routeName) {
        return getAvailableRoutes().contains(routeName);
    }

    @Override
    public Map<String, Object> getRoutingStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDecisions", totalRoutingDecisions.get());
        stats.put("fallbackCount", fallbackCount.get());
        stats.put("fallbackRate",
                totalRoutingDecisions.get() > 0 ? (double) fallbackCount.get() / totalRoutingDecisions.get() : 0.0);

        Map<String, Long> usageByRoute = new HashMap<>();
        routeUsageCount.forEach((route, count) -> usageByRoute.put(route, count.get()));
        stats.put("usageByRoute", usageByRoute);

        return stats;
    }

    @Override
    public void resetStatistics() {
        totalRoutingDecisions.set(0);
        fallbackCount.set(0);
        routeUsageCount.clear();
        log.info("Routing statistics have been reset");
    }
}