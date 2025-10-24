package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.config.RoutingConfiguration;
import com.example.pricerulesaidrools.ai.dto.EnhancedPricingRequest;
import com.example.pricerulesaidrools.ai.dto.RoutingDecision;
import com.example.pricerulesaidrools.model.*;
import com.example.pricerulesaidrools.repository.CustomerRepository;
import com.example.pricerulesaidrools.repository.DealRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AIRoutingServiceImpl.
 * Tests routing logic for billing, technical, and risk review paths.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AI Routing Service Tests")
class AIRoutingServiceTest {

    @Mock
    private RoutingConfiguration routingConfiguration;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DealRepository dealRepository;

    @InjectMocks
    private AIRoutingServiceImpl aiRoutingService;

    private PricingRequest pricingRequest;
    private Customer customer;
    private Deal deal;

    @BeforeEach
    void setUp() {
        // Setup default test data
        pricingRequest = PricingRequest.builder()
                .customerId("CUST-001")
                .basePrice(50000.0)
                .quantity(10)
                .productId("PROD-001")
                .pricingStrategy("STANDARD")
                .customerTenureMonths(24)
                .contractLengthMonths(12)
                .build();

        customer = Customer.builder()
                .id(1L)
                .customerId("CUST-001")
                .name("Test Customer")
                .customerType("ENTERPRISE")
                .segment("Large")
                .churnRiskScore(new BigDecimal("50"))
                .paymentIssuesCount(0)
                .supportTicketsCount(5)
                .build();

        deal = Deal.builder()
                .id(1L)
                .dealId("DEAL-001")
                .customerId("CUST-001")
                .type(Deal.DealType.STANDARD)
                .value(new BigDecimal("50000"))
                .complexity(Deal.DealComplexity.MEDIUM)
                .status(Deal.DealStatus.UNDER_REVIEW)
                .riskScore(30.0)
                .build();

        // Setup default routing configuration
        setupDefaultRoutingConfiguration();
    }

    private void setupDefaultRoutingConfiguration() {
        when(routingConfiguration.getAvailableRoutes())
                .thenReturn(Arrays.asList("billing-review", "technical-review", "risk-review", "general-review"));
        when(routingConfiguration.getConfidenceThreshold()).thenReturn(0.7);
        when(routingConfiguration.getFallbackRoute()).thenReturn("general-review");

        List<RoutingConfiguration.RoutingStrategy> strategies = new ArrayList<>();

        RoutingConfiguration.RoutingStrategy billingStrategy = new RoutingConfiguration.RoutingStrategy();
        billingStrategy.setName("billing-review");
        billingStrategy.setCondition("deal.type == 'ENTERPRISE' && deal.value > 100000");
        billingStrategy.setPriority(10);
        billingStrategy.setEnabled(true);
        strategies.add(billingStrategy);

        RoutingConfiguration.RoutingStrategy technicalStrategy = new RoutingConfiguration.RoutingStrategy();
        technicalStrategy.setName("technical-review");
        technicalStrategy.setCondition("deal.complexity == 'HIGH'");
        technicalStrategy.setPriority(8);
        technicalStrategy.setEnabled(true);
        strategies.add(technicalStrategy);

        RoutingConfiguration.RoutingStrategy riskStrategy = new RoutingConfiguration.RoutingStrategy();
        riskStrategy.setName("risk-review");
        riskStrategy.setCondition("customer.riskScore > 70");
        riskStrategy.setPriority(9);
        riskStrategy.setEnabled(true);
        strategies.add(riskStrategy);

        when(routingConfiguration.getStrategies()).thenReturn(strategies);
    }

    @Test
    @DisplayName("Should route enterprise deal above $100K to billing review")
    void testBillingReviewRouting() {
        // Arrange
        deal.setType(Deal.DealType.ENTERPRISE);
        deal.setValue(new BigDecimal("150000"));

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision);
        assertEquals("billing-review", decision.getRoute());
        assertTrue(decision.getConfidence() >= 0.7);
        assertFalse(decision.isFallback());
        assertEquals("CUST-001", decision.getCustomerId());
        assertEquals("DEAL-001", decision.getDealId());
        assertEquals(150000.0, decision.getDealValue());
        assertThat(decision.getReason()).contains("Enterprise deal");
        assertNotNull(decision.getTimestamp());
        assertNotNull(decision.getProcessingTimeMs());
    }

    @Test
    @DisplayName("Should route high complexity deal to technical review")
    void testTechnicalReviewRouting() {
        // Arrange
        deal.setComplexity(Deal.DealComplexity.HIGH);
        deal.setTechnicalRequirements("Custom integration required");

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision);
        assertEquals("technical-review", decision.getRoute());
        assertTrue(decision.getConfidence() >= 0.7);
        assertFalse(decision.isFallback());
        assertEquals("HIGH", decision.getComplexity());
        assertThat(decision.getReason()).contains("complexity");
    }

    @Test
    @DisplayName("Should route high risk customer to risk review")
    void testRiskReviewRouting() {
        // Arrange
        customer.setChurnRiskScore(new BigDecimal("80"));
        customer.setPaymentIssuesCount(3);

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision);
        assertEquals("risk-review", decision.getRoute());
        assertTrue(decision.getConfidence() >= 0.7);
        assertFalse(decision.isFallback());
        assertEquals(80.0, decision.getCustomerRiskScore());
        assertThat(decision.getReason()).contains("risk");
    }

    @Test
    @DisplayName("Should use fallback route when confidence is below threshold")
    void testFallbackRoutingLowConfidence() {
        // Arrange
        deal.setType(Deal.DealType.STANDARD);
        deal.setValue(new BigDecimal("10000"));
        deal.setComplexity(Deal.DealComplexity.LOW);
        customer.setChurnRiskScore(new BigDecimal("20"));

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision);
        assertEquals("general-review", decision.getRoute());
        assertTrue(decision.isFallback());
        assertThat(decision.getReason()).containsAnyOf("Confidence below threshold", "Standard review");
    }

    @Test
    @DisplayName("Should handle missing customer gracefully")
    void testRoutingWithMissingCustomer() {
        // Arrange
        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.empty());
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision);
        assertNull(decision.getCustomerRiskScore());
        // Should still make a routing decision based on deal info
        assertNotNull(decision.getRoute());
    }

    @Test
    @DisplayName("Should handle missing deal by creating default")
    void testRoutingWithMissingDeal() {
        // Arrange
        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.emptyList());

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision);
        assertNotNull(decision.getDealValue());
        // Default deal should result in general review
        assertEquals("general-review", decision.getRoute());
    }

    @Test
    @DisplayName("Should evaluate all strategies and return scores")
    void testEvaluateAllStrategies() {
        // Arrange
        deal.setType(Deal.DealType.ENTERPRISE);
        deal.setValue(new BigDecimal("150000"));
        deal.setComplexity(Deal.DealComplexity.HIGH);
        customer.setChurnRiskScore(new BigDecimal("75"));

        EnhancedPricingRequest request = EnhancedPricingRequest.from(pricingRequest, deal, customer);

        // Act
        Map<String, Double> scores = aiRoutingService.evaluateAllStrategies(request);

        // Assert
        assertNotNull(scores);
        assertTrue(scores.containsKey("billing-review"));
        assertTrue(scores.containsKey("technical-review"));
        assertTrue(scores.containsKey("risk-review"));
        assertTrue(scores.containsKey("general-review"));

        // All specialized routes should have high scores
        assertTrue(scores.get("billing-review") > 0.7);
        assertTrue(scores.get("technical-review") > 0.7);
        assertTrue(scores.get("risk-review") > 0.7);
    }

    @Test
    @DisplayName("Should route very high complexity to technical review with maximum confidence")
    void testVeryHighComplexityRouting() {
        // Arrange
        deal.setComplexity(Deal.DealComplexity.VERY_HIGH);

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertEquals("technical-review", decision.getRoute());
        assertEquals(1.0, decision.getConfidence(), 0.01);
    }

    @Test
    @DisplayName("Should include rule scores in routing decision")
    void testRoutingDecisionIncludesRuleScores() {
        // Arrange
        deal.setType(Deal.DealType.ENTERPRISE);
        deal.setValue(new BigDecimal("200000"));

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision.getRuleScores());
        assertFalse(decision.getRuleScores().isEmpty());
        assertTrue(decision.getRuleScores().containsKey("billing-review"));
    }

    @Test
    @DisplayName("Should handle exception during routing gracefully")
    void testRoutingWithException() {
        // Arrange
        when(customerRepository.findByCustomerId(any()))
                .thenThrow(new RuntimeException("Database connection error"));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision);
        assertEquals("general-review", decision.getRoute());
        assertTrue(decision.isFallback());
        assertThat(decision.getReason()).contains("Error during routing");
    }

    @Test
    @DisplayName("Should validate route names correctly")
    void testIsValidRoute() {
        // Act & Assert
        assertTrue(aiRoutingService.isValidRoute("billing-review"));
        assertTrue(aiRoutingService.isValidRoute("technical-review"));
        assertTrue(aiRoutingService.isValidRoute("risk-review"));
        assertTrue(aiRoutingService.isValidRoute("general-review"));
        assertFalse(aiRoutingService.isValidRoute("invalid-route"));
    }

    @Test
    @DisplayName("Should track routing statistics")
    void testRoutingStatistics() {
        // Arrange
        deal.setType(Deal.DealType.ENTERPRISE);
        deal.setValue(new BigDecimal("150000"));

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act - Make multiple routing decisions
        aiRoutingService.route(pricingRequest);
        aiRoutingService.route(pricingRequest);

        Map<String, Object> stats = aiRoutingService.getRoutingStatistics();

        // Assert
        assertNotNull(stats);
        assertEquals(2L, stats.get("totalDecisions"));
        assertNotNull(stats.get("fallbackRate"));
        assertNotNull(stats.get("usageByRoute"));

        @SuppressWarnings("unchecked")
        Map<String, Long> usageByRoute = (Map<String, Long>) stats.get("usageByRoute");
        assertTrue(usageByRoute.containsKey("billing-review"));
        assertEquals(2L, usageByRoute.get("billing-review"));
    }

    @Test
    @DisplayName("Should reset statistics correctly")
    void testResetStatistics() {
        // Arrange & Act - Make some routing decisions first
        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        aiRoutingService.route(pricingRequest);
        aiRoutingService.resetStatistics();
        Map<String, Object> stats = aiRoutingService.getRoutingStatistics();

        // Assert
        assertEquals(0L, stats.get("totalDecisions"));
        assertEquals(0L, stats.get("fallbackCount"));

        @SuppressWarnings("unchecked")
        Map<String, Long> usageByRoute = (Map<String, Long>) stats.get("usageByRoute");
        assertTrue(usageByRoute.isEmpty());
    }

    @Test
    @DisplayName("Should set confidence threshold within valid range")
    void testSetConfidenceThreshold() {
        // Act & Assert
        aiRoutingService.setConfidenceThreshold(0.8);
        verify(routingConfiguration).setConfidenceThreshold(0.8);

        // Test invalid thresholds
        assertThrows(IllegalArgumentException.class, () ->
                aiRoutingService.setConfidenceThreshold(1.5));
        assertThrows(IllegalArgumentException.class, () ->
                aiRoutingService.setConfidenceThreshold(-0.1));
    }

    @Test
    @DisplayName("Should boost confidence for very large enterprise deals")
    void testVeryLargeEnterpriseDealRouting() {
        // Arrange
        deal.setType(Deal.DealType.ENTERPRISE);
        deal.setValue(new BigDecimal("600000")); // Very large deal

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertEquals("billing-review", decision.getRoute());
        assertTrue(decision.getConfidence() > 1.0); // Boosted confidence (capped at 1.0 internally)
    }

    @Test
    @DisplayName("Should consider custom billing terms for billing review")
    void testCustomBillingTermsRouting() {
        // Arrange
        deal.setType(Deal.DealType.ENTERPRISE);
        deal.setValue(new BigDecimal("120000"));
        deal.setBillingTerms("custom payment schedule required");

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertEquals("billing-review", decision.getRoute());
        assertTrue(decision.getConfidence() > 0.7);
    }

    @Test
    @DisplayName("Should consider payment issues for risk review")
    void testPaymentIssuesRiskRouting() {
        // Arrange
        customer.setChurnRiskScore(new BigDecimal("60"));
        customer.setPaymentIssuesCount(5); // Many payment issues

        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertEquals("risk-review", decision.getRoute());
        assertTrue(decision.getConfidence() > 0.6);
    }

    @Test
    @DisplayName("Should include metadata in routing decision")
    void testRoutingDecisionMetadata() {
        // Arrange
        when(customerRepository.findByCustomerId("CUST-001"))
                .thenReturn(Optional.of(customer));
        when(dealRepository.findByCustomerId("CUST-001"))
                .thenReturn(Collections.singletonList(deal));

        // Act
        RoutingDecision decision = aiRoutingService.route(pricingRequest);

        // Assert
        assertNotNull(decision.getMetadata());
        assertEquals("STANDARD", decision.getMetadata().get("pricingStrategy"));
        assertEquals(10, decision.getMetadata().get("quantity"));
        assertEquals(12, decision.getMetadata().get("contractLengthMonths"));
        assertEquals("ENTERPRISE", decision.getMetadata().get("customerType"));
    }
}