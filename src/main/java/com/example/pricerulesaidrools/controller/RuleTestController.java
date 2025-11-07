package com.example.pricerulesaidrools.controller;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.PricingRequest;
import com.example.pricerulesaidrools.testing.RuleTestRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for rule testing.
 * Provides endpoints for running rule tests and analyzing results.
 */
@RestController
@RequestMapping("/api/v1/rule-tests")
@RequiredArgsConstructor
@Slf4j
public class RuleTestController {

    private final RuleTestRunner ruleTestRunner;
    
    /**
     * Runs a single test case.
     *
     * @param testCase the test case to run
     * @return the test case result
     */
    @PostMapping("/run")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<Map<String, Object>> runTest(@RequestBody RuleTestRunner.RuleTestCase testCase) {
        try {
            RuleTestRunner.RuleTestCase result = ruleTestRunner.runTestCase(testCase);
            
            Map<String, Object> response = new HashMap<>();
            response.put("testName", result.getName());
            response.put("passed", result.isPassed());
            response.put("expectedResults", result.getExpectedResults());
            response.put("actualResults", result.getActualResults());
            
            if (!result.isPassed()) {
                response.put("errorMessage", result.getErrorMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error running test case", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Runs a predefined test suite.
     *
     * @param suiteName the name of the test suite to run
     * @return the test suite results
     */
    @GetMapping("/run-suite/{suiteName}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<Map<String, Object>> runTestSuite(@PathVariable String suiteName) {
        try {
            RuleTestRunner.RuleTestSuite suite;
            
            switch (suiteName.toLowerCase()) {
                case "volume-discount":
                    suite = ruleTestRunner.createVolumeDiscountTestSuite();
                    break;
                case "min-commitment":
                    suite = ruleTestRunner.createMinCommitmentTestSuite();
                    break;
                case "churn-risk":
                    suite = ruleTestRunner.createChurnRiskTestSuite();
                    break;
                case "all":
                    return runAllTestSuites();
                default:
                    return ResponseEntity.badRequest().body(Map.of("error", "Unknown test suite: " + suiteName));
            }
            
            RuleTestRunner.RuleTestSuite result = ruleTestRunner.runTestSuite(suite);
            
            Map<String, Object> response = new HashMap<>();
            response.put("suiteName", result.getName());
            response.put("passedTests", result.getPassedTests());
            response.put("totalTests", result.getTotalTests());
            response.put("passRate", result.getPassRate());
            
            List<Map<String, Object>> testResults = new ArrayList<>();
            for (RuleTestRunner.RuleTestCase testCase : result.getTestCases()) {
                Map<String, Object> testResult = new HashMap<>();
                testResult.put("testName", testCase.getName());
                testResult.put("passed", testCase.isPassed());
                
                if (!testCase.isPassed()) {
                    testResult.put("errorMessage", testCase.getErrorMessage());
                    testResult.put("expectedResults", testCase.getExpectedResults());
                    testResult.put("actualResults", testCase.getActualResults());
                }
                
                testResults.add(testResult);
            }
            
            response.put("testResults", testResults);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error running test suite", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Runs all predefined test suites.
     *
     * @return the combined test suite results
     */
    @GetMapping("/run-all")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<Map<String, Object>> runAllTestSuites() {
        try {
            List<RuleTestRunner.RuleTestSuite> suites = new ArrayList<>();
            suites.add(ruleTestRunner.createVolumeDiscountTestSuite());
            suites.add(ruleTestRunner.createMinCommitmentTestSuite());
            suites.add(ruleTestRunner.createChurnRiskTestSuite());
            
            int totalTests = 0;
            int passedTests = 0;
            List<Map<String, Object>> suiteResults = new ArrayList<>();
            
            for (RuleTestRunner.RuleTestSuite suite : suites) {
                RuleTestRunner.RuleTestSuite result = ruleTestRunner.runTestSuite(suite);
                totalTests += result.getTotalTests();
                passedTests += result.getPassedTests();
                
                Map<String, Object> suiteResult = new HashMap<>();
                suiteResult.put("suiteName", result.getName());
                suiteResult.put("passedTests", result.getPassedTests());
                suiteResult.put("totalTests", result.getTotalTests());
                suiteResult.put("passRate", result.getPassRate());
                
                List<Map<String, Object>> testResults = result.getTestCases().stream()
                        .filter(testCase -> !testCase.isPassed())
                        .map(testCase -> {
                            Map<String, Object> testResult = new HashMap<>();
                            testResult.put("testName", testCase.getName());
                            testResult.put("passed", false);
                            testResult.put("errorMessage", testCase.getErrorMessage());
                            return testResult;
                        })
                        .collect(Collectors.toList());
                
                suiteResult.put("failedTests", testResults);
                suiteResults.add(suiteResult);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalTests", totalTests);
            response.put("passedTests", passedTests);
            response.put("passRate", totalTests > 0 ? (double) passedTests / totalTests : 0.0);
            response.put("suiteResults", suiteResults);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error running all test suites", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Creates a custom test case with the specified parameters.
     *
     * @param params the parameters for the test case
     * @return the created test case
     */
    @PostMapping("/create-test")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_RULE_MANAGER')")
    public ResponseEntity<RuleTestRunner.RuleTestCase> createCustomTest(@RequestBody Map<String, Object> params) {
        try {
            String testName = (String) params.getOrDefault("testName", "Custom Test");
            double basePrice = Double.parseDouble(params.getOrDefault("basePrice", "10000").toString());
            
            // Extract financial metrics
            @SuppressWarnings("unchecked")
            Map<String, Object> metricsParams = (Map<String, Object>) params.getOrDefault("financialMetrics", new HashMap<>());
            
            FinancialMetrics metrics = FinancialMetrics.builder()
                    .customerId("test-customer")
                    .arr(new BigDecimal(metricsParams.getOrDefault("arr", basePrice).toString()))
                    .tcv(new BigDecimal(metricsParams.getOrDefault("tcv", basePrice * 3).toString()))
                    .acv(new BigDecimal(metricsParams.getOrDefault("acv", basePrice).toString()))
                    .clv(new BigDecimal(metricsParams.getOrDefault("clv", basePrice * 5).toString()))
                    .churnRiskScore(new BigDecimal(metricsParams.getOrDefault("churnRiskScore", 10).toString()))
                    .growthRate(new BigDecimal(metricsParams.getOrDefault("growthRate", 5).toString()))
                    .contractMonths(Integer.parseInt(metricsParams.getOrDefault("contractMonths", 12).toString()))
                    .build();
            
            // Create pricing request
            PricingRequest request = PricingRequest.builder()
                    .customerId("test-customer")
                    .basePrice(basePrice)
                    .quantity(Integer.parseInt(params.getOrDefault("quantity", 1).toString()))
                    .productId(params.getOrDefault("productId", "test-product").toString())
                    .pricingStrategy(params.getOrDefault("pricingStrategy", "value-based").toString())
                    .customerTenureMonths(Integer.parseInt(params.getOrDefault("customerTenureMonths", 12).toString()))
                    .contractLengthMonths(Integer.parseInt(params.getOrDefault("contractLengthMonths", 12).toString()))
                    .financialMetrics(metrics)
                    .build();
            
            // Extract expected results
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedResults = (Map<String, Object>) params.getOrDefault("expectedResults", new HashMap<>());
            
            // Create and return the test case
            RuleTestRunner.RuleTestCase testCase = new RuleTestRunner.RuleTestCase(testName, request, expectedResults);
            return ResponseEntity.ok(testCase);
        } catch (Exception e) {
            log.error("Error creating custom test", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}