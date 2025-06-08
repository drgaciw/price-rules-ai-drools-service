package com.example.pricerulesaidrools.testing;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.PricingRequest;
import com.example.pricerulesaidrools.model.PricingResult;
import com.example.pricerulesaidrools.service.DroolsIntegrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility for testing Drools pricing rules with various scenarios.
 * This component allows simulation of different customer scenarios and validation of rule outcomes.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RuleTestRunner {

    private final DroolsIntegrationService droolsIntegrationService;
    
    /**
     * Represents a test case for rule testing.
     */
    public static class RuleTestCase {
        private String name;
        private PricingRequest request;
        private Map<String, Object> expectedResults;
        private Map<String, Object> actualResults;
        private boolean passed;
        private String errorMessage;
        
        // Getters, setters, and constructors
        public RuleTestCase(String name, PricingRequest request, Map<String, Object> expectedResults) {
            this.name = name;
            this.request = request;
            this.expectedResults = expectedResults;
            this.actualResults = new HashMap<>();
            this.passed = false;
            this.errorMessage = null;
        }
        
        public String getName() {
            return name;
        }
        
        public PricingRequest getRequest() {
            return request;
        }
        
        public Map<String, Object> getExpectedResults() {
            return expectedResults;
        }
        
        public Map<String, Object> getActualResults() {
            return actualResults;
        }
        
        public boolean isPassed() {
            return passed;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public void setActualResults(Map<String, Object> actualResults) {
            this.actualResults = actualResults;
        }
        
        public void setPassed(boolean passed) {
            this.passed = passed;
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * Represents a test suite for rule testing.
     */
    public static class RuleTestSuite {
        private String name;
        private List<RuleTestCase> testCases;
        private int passedTests;
        private int totalTests;
        
        // Getters, setters, and constructors
        public RuleTestSuite(String name) {
            this.name = name;
            this.testCases = new ArrayList<>();
            this.passedTests = 0;
            this.totalTests = 0;
        }
        
        public String getName() {
            return name;
        }
        
        public List<RuleTestCase> getTestCases() {
            return testCases;
        }
        
        public int getPassedTests() {
            return passedTests;
        }
        
        public int getTotalTests() {
            return totalTests;
        }
        
        public void addTestCase(RuleTestCase testCase) {
            this.testCases.add(testCase);
            this.totalTests++;
        }
        
        public void incrementPassedTests() {
            this.passedTests++;
        }
        
        public double getPassRate() {
            return totalTests > 0 ? (double) passedTests / totalTests : 0.0;
        }
    }
    
    /**
     * Runs a test case and compares actual results with expected results.
     *
     * @param testCase the test case to run
     * @return the updated test case with results
     */
    public RuleTestCase runTestCase(RuleTestCase testCase) {
        try {
            KieSession kieSession = droolsIntegrationService.getKieSession();
            
            // Set up the test objects
            PricingRequest request = testCase.getRequest();
            PricingResult result = new PricingResult();
            result.setFinalPrice(request.getBasePrice()); // Initialize with base price
            
            // Set up logging
            Logger testLogger = Logger.getLogger("RuleTestLogger");
            
            // Insert facts into session
            kieSession.setGlobal("logger", testLogger);
            kieSession.insert(request);
            kieSession.insert(request.getFinancialMetrics());
            kieSession.insert(result);
            
            // Fire rules
            kieSession.fireAllRules();
            
            // Extract actual results
            Map<String, Object> actualResults = new HashMap<>();
            actualResults.put("finalPrice", result.getFinalPrice());
            actualResults.put("discount", result.getDiscount());
            actualResults.put("discountDescription", result.getDiscountDescription());
            actualResults.put("appliedRules", result.getAppliedRules());
            actualResults.put("minimumCommitment", result.getMinimumCommitment());
            actualResults.put("commitmentTier", result.getCommitmentTier());
            actualResults.put("includedServices", result.getIncludedServices());
            
            testCase.setActualResults(actualResults);
            
            // Compare expected vs. actual results
            boolean passed = validateResults(testCase.getExpectedResults(), actualResults);
            testCase.setPassed(passed);
            
            if (!passed) {
                testCase.setErrorMessage(generateErrorMessage(testCase.getExpectedResults(), actualResults));
            }
            
            // Dispose of the test session
            kieSession.dispose();
            
            return testCase;
        } catch (Exception e) {
            log.error("Error running test case: " + testCase.getName(), e);
            testCase.setPassed(false);
            testCase.setErrorMessage("Test execution error: " + e.getMessage());
            return testCase;
        }
    }
    
    /**
     * Runs a test suite containing multiple test cases.
     *
     * @param testSuite the test suite to run
     * @return the updated test suite with results
     */
    public RuleTestSuite runTestSuite(RuleTestSuite testSuite) {
        for (RuleTestCase testCase : testSuite.getTestCases()) {
            RuleTestCase result = runTestCase(testCase);
            if (result.isPassed()) {
                testSuite.incrementPassedTests();
            }
        }
        
        return testSuite;
    }
    
    /**
     * Validates actual results against expected results.
     *
     * @param expected the expected results
     * @param actual the actual results
     * @return true if all expected results match actual results
     */
    private boolean validateResults(Map<String, Object> expected, Map<String, Object> actual) {
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            
            if (!actual.containsKey(key)) {
                return false;
            }
            
            Object actualValue = actual.get(key);
            
            // Handle null values
            if (expectedValue == null) {
                if (actualValue != null) {
                    return false;
                }
                continue;
            }
            
            // Compare values based on type
            if (expectedValue instanceof Number && actualValue instanceof Number) {
                double expectedNum = ((Number) expectedValue).doubleValue();
                double actualNum = ((Number) actualValue).doubleValue();
                
                // Use a small epsilon for floating-point comparisons
                if (Math.abs(expectedNum - actualNum) > 0.001) {
                    return false;
                }
            } else if (expectedValue instanceof List && actualValue instanceof List) {
                // For lists, check if actual contains all expected items
                List<?> expectedList = (List<?>) expectedValue;
                List<?> actualList = (List<?>) actualValue;
                
                for (Object item : expectedList) {
                    if (!actualList.contains(item)) {
                        return false;
                    }
                }
            } else if (!expectedValue.equals(actualValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Generates an error message describing differences between expected and actual results.
     *
     * @param expected the expected results
     * @param actual the actual results
     * @return a detailed error message
     */
    private String generateErrorMessage(Map<String, Object> expected, Map<String, Object> actual) {
        StringBuilder builder = new StringBuilder("Test failed. Differences:");
        
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            String key = entry.getKey();
            Object expectedValue = entry.getValue();
            Object actualValue = actual.getOrDefault(key, "MISSING");
            
            if (!actualValue.equals(expectedValue)) {
                builder.append("\n- ")
                       .append(key)
                       .append(": expected ")
                       .append(expectedValue)
                       .append(", got ")
                       .append(actualValue);
            }
        }
        
        return builder.toString();
    }
    
    /**
     * Creates a standard set of test cases for volume discount rules.
     *
     * @return a test suite for volume discount rules
     */
    public RuleTestSuite createVolumeDiscountTestSuite() {
        RuleTestSuite suite = new RuleTestSuite("Volume Discount Rules");
        
        // Test case 1: No discount (below threshold)
        PricingRequest request1 = createBasicRequest(5000.0);
        Map<String, Object> expected1 = new HashMap<>();
        expected1.put("finalPrice", 5000.0);
        expected1.put("discount", 0.0);
        suite.addTestCase(new RuleTestCase("Below Discount Threshold", request1, expected1));
        
        // Test case 2: Tier 1 discount
        PricingRequest request2 = createBasicRequest(15000.0);
        request2.getFinancialMetrics().setArr(BigDecimal.valueOf(15000.0));
        Map<String, Object> expected2 = new HashMap<>();
        expected2.put("finalPrice", 14250.0); // 5% discount
        expected2.put("discount", 0.05);
        suite.addTestCase(new RuleTestCase("Tier 1 Discount", request2, expected2));
        
        // Test case 3: Tier 2 discount
        PricingRequest request3 = createBasicRequest(60000.0);
        request3.getFinancialMetrics().setArr(BigDecimal.valueOf(60000.0));
        Map<String, Object> expected3 = new HashMap<>();
        expected3.put("finalPrice", 54000.0); // 10% discount
        expected3.put("discount", 0.1);
        suite.addTestCase(new RuleTestCase("Tier 2 Discount", request3, expected3));
        
        // Test case 4: Tier 3 discount
        PricingRequest request4 = createBasicRequest(150000.0);
        request4.getFinancialMetrics().setArr(BigDecimal.valueOf(150000.0));
        Map<String, Object> expected4 = new HashMap<>();
        expected4.put("finalPrice", 127500.0); // 15% discount
        expected4.put("discount", 0.15);
        suite.addTestCase(new RuleTestCase("Tier 3 Discount", request4, expected4));
        
        // Test case 5: Tier 4 discount
        PricingRequest request5 = createBasicRequest(600000.0);
        request5.getFinancialMetrics().setArr(BigDecimal.valueOf(600000.0));
        Map<String, Object> expected5 = new HashMap<>();
        expected5.put("finalPrice", 480000.0); // 20% discount
        expected5.put("discount", 0.2);
        suite.addTestCase(new RuleTestCase("Tier 4 Discount", request5, expected5));
        
        return suite;
    }
    
    /**
     * Creates a standard set of test cases for minimum commitment rules.
     *
     * @return a test suite for minimum commitment rules
     */
    public RuleTestSuite createMinCommitmentTestSuite() {
        RuleTestSuite suite = new RuleTestSuite("Minimum Commitment Rules");
        
        // Test case 1: Small tier commitment
        PricingRequest request1 = createBasicRequest(5000.0);
        request1.getFinancialMetrics().setAcv(BigDecimal.valueOf(8000.0));
        Map<String, Object> expected1 = new HashMap<>();
        expected1.put("minimumCommitment", 4000.0); // 50% of ACV
        expected1.put("commitmentTier", "Small");
        suite.addTestCase(new RuleTestCase("Small Tier Commitment", request1, expected1));
        
        // Test case 2: Standard tier commitment
        PricingRequest request2 = createBasicRequest(20000.0);
        request2.getFinancialMetrics().setAcv(BigDecimal.valueOf(30000.0));
        Map<String, Object> expected2 = new HashMap<>();
        expected2.put("minimumCommitment", 12000.0); // 40% of ACV
        expected2.put("commitmentTier", "Standard");
        suite.addTestCase(new RuleTestCase("Standard Tier Commitment", request2, expected2));
        
        // Test case 3: Premium tier commitment
        PricingRequest request3 = createBasicRequest(50000.0);
        request3.getFinancialMetrics().setAcv(BigDecimal.valueOf(70000.0));
        Map<String, Object> expected3 = new HashMap<>();
        expected3.put("minimumCommitment", 21000.0); // 30% of ACV
        expected3.put("commitmentTier", "Premium");
        expected3.put("includedServices", List.of("Basic Support"));
        suite.addTestCase(new RuleTestCase("Premium Tier Commitment", request3, expected3));
        
        // Test case 4: Enterprise tier commitment
        PricingRequest request4 = createBasicRequest(200000.0);
        request4.getFinancialMetrics().setAcv(BigDecimal.valueOf(250000.0));
        Map<String, Object> expected4 = new HashMap<>();
        expected4.put("minimumCommitment", 62500.0); // 25% of ACV
        expected4.put("commitmentTier", "Enterprise");
        expected4.put("includedServices", List.of("Premium Support"));
        suite.addTestCase(new RuleTestCase("Enterprise Tier Commitment", request4, expected4));
        
        // Test case 5: Strategic tier commitment
        PricingRequest request5 = createBasicRequest(500000.0);
        request5.getFinancialMetrics().setAcv(BigDecimal.valueOf(600000.0));
        Map<String, Object> expected5 = new HashMap<>();
        expected5.put("minimumCommitment", 120000.0); // 20% of ACV
        expected5.put("commitmentTier", "Strategic");
        expected5.put("includedServices", List.of("Premium Support", "Dedicated Account Manager"));
        suite.addTestCase(new RuleTestCase("Strategic Tier Commitment", request5, expected5));
        
        return suite;
    }
    
    /**
     * Creates a standard set of test cases for churn risk adjustment rules.
     *
     * @return a test suite for churn risk adjustment rules
     */
    public RuleTestSuite createChurnRiskTestSuite() {
        RuleTestSuite suite = new RuleTestSuite("Churn Risk Adjustment Rules");
        
        // Test case 1: Low churn risk (no adjustment)
        PricingRequest request1 = createBasicRequest(10000.0);
        request1.getFinancialMetrics().setChurnRiskScore(BigDecimal.valueOf(20.0));
        Map<String, Object> expected1 = new HashMap<>();
        expected1.put("finalPrice", 10000.0); // No discount
        suite.addTestCase(new RuleTestCase("Low Churn Risk", request1, expected1));
        
        // Test case 2: Moderate churn risk
        PricingRequest request2 = createBasicRequest(10000.0);
        request2.getFinancialMetrics().setChurnRiskScore(BigDecimal.valueOf(35.0));
        Map<String, Object> expected2 = new HashMap<>();
        expected2.put("finalPrice", 9500.0); // 5% discount
        expected2.put("appliedRules", List.of("Churn Risk Adjustment"));
        suite.addTestCase(new RuleTestCase("Moderate Churn Risk", request2, expected2));
        
        // Test case 3: High churn risk
        PricingRequest request3 = createBasicRequest(10000.0);
        request3.getFinancialMetrics().setChurnRiskScore(BigDecimal.valueOf(50.0));
        Map<String, Object> expected3 = new HashMap<>();
        expected3.put("finalPrice", 9000.0); // 10% discount
        expected3.put("appliedRules", List.of("Churn Risk Adjustment"));
        suite.addTestCase(new RuleTestCase("High Churn Risk", request3, expected3));
        
        // Test case 4: Very high churn risk
        PricingRequest request4 = createBasicRequest(10000.0);
        request4.getFinancialMetrics().setChurnRiskScore(BigDecimal.valueOf(70.0));
        Map<String, Object> expected4 = new HashMap<>();
        expected4.put("finalPrice", 8500.0); // 15% discount
        expected4.put("appliedRules", List.of("Churn Risk Adjustment", "High Risk Support Package"));
        expected4.put("includedServices", List.of("Premium Support"));
        suite.addTestCase(new RuleTestCase("Very High Churn Risk", request4, expected4));
        
        // Test case 5: Extreme churn risk
        PricingRequest request5 = createBasicRequest(10000.0);
        request5.getFinancialMetrics().setChurnRiskScore(BigDecimal.valueOf(90.0));
        Map<String, Object> expected5 = new HashMap<>();
        expected5.put("finalPrice", 8000.0); // 20% discount
        expected5.put("appliedRules", List.of("Churn Risk Adjustment", "High Risk Support Package"));
        expected5.put("includedServices", List.of("Premium Support"));
        suite.addTestCase(new RuleTestCase("Extreme Churn Risk", request5, expected5));
        
        return suite;
    }
    
    /**
     * Creates a basic pricing request with default values.
     *
     * @param basePrice the base price for the request
     * @return a pricing request with default values
     */
    private PricingRequest createBasicRequest(double basePrice) {
        FinancialMetrics metrics = FinancialMetrics.builder()
                .customerId("test-customer")
                .arr(BigDecimal.valueOf(basePrice))
                .tcv(BigDecimal.valueOf(basePrice * 3)) // Assume 3-year contract
                .acv(BigDecimal.valueOf(basePrice))
                .clv(BigDecimal.valueOf(basePrice * 5)) // Assume 5-year CLV
                .churnRiskScore(BigDecimal.valueOf(10.0)) // Low risk by default
                .growthRate(BigDecimal.valueOf(5.0)) // Modest growth by default
                .contractMonths(12) // 1-year contract by default
                .build();
        
        return PricingRequest.builder()
                .customerId("test-customer")
                .basePrice(basePrice)
                .quantity(1)
                .productId("test-product")
                .pricingStrategy("value-based")
                .customerTenureMonths(12) // 1-year tenure by default
                .contractLengthMonths(12) // 1-year contract by default
                .financialMetrics(metrics)
                .build();
    }
}