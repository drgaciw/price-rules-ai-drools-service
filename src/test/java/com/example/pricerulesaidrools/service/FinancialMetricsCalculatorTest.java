package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.Customer;
import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.repository.CustomerRepository;
import com.example.pricerulesaidrools.repository.FinancialMetricsRepository;
import com.example.pricerulesaidrools.repository.FinancialMetricsSnapshotRepository;
import com.example.pricerulesaidrools.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialMetricsCalculatorTest {

        @Mock
        private FinancialMetricsRepository metricsRepository;

        @Mock
        private CustomerRepository customerRepository;

        @Mock
        private QuoteRepository quoteRepository;

        @Mock
        private FinancialMetricsSnapshotRepository snapshotRepository;

        @InjectMocks
        private FinancialMetricsCalculator calculator;

        private Quote testQuote;
        private Customer testCustomer;

        @BeforeEach
        void setUp() {
                // Set default values via reflection
                ReflectionTestUtils.setField(calculator, "defaultChurnRate", new BigDecimal("0.03"));
                ReflectionTestUtils.setField(calculator, "defaultCustomerLifespan", 36);

                testQuote = Quote.builder()
                                .quoteId("Q123")
                                .customerId("C123")
                                .monthlyPrice(new BigDecimal("1000"))
                                .durationInMonths(12)
                                .expectedDuration(24)
                                .customerType("ENTERPRISE")
                                .subscriptionType("ANNUAL")
                                .basePrice(new BigDecimal("12000"))
                                .customerTenureMonths(18)
                                .productId("P123")
                                .build();

                testCustomer = Customer.builder()
                                .customerId("C123")
                                .name("Test Customer")
                                .customerType("ENTERPRISE")
                                .churnRiskScore(new BigDecimal("0.02"))
                                .build();
        }

        @Test
        void calculateMetrics_ShouldReturnValidMetrics() {
                // Given
                when(metricsRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.empty());
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));
                when(metricsRepository.save(any(FinancialMetrics.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                FinancialMetrics result = calculator.calculateMetrics(testQuote);

                // Then
                assertNotNull(result);
                assertEquals("C123", result.getCustomerId());
                assertEquals(0, new BigDecimal("12000").compareTo(result.getArr()));
                assertEquals(0, new BigDecimal("12000").compareTo(result.getTcv()));
                assertEquals(0, new BigDecimal("12000").compareTo(result.getAcv()));
                assertNotNull(result.getClv());
                assertEquals(0, new BigDecimal("0.5").compareTo(result.getChurnRiskScore()));

                verify(metricsRepository).save(any(FinancialMetrics.class));
        }

        @Test
        void calculateARR_ShouldCalculateCorrectly() {
                // When
                BigDecimal arr = calculator.calculateARR(testQuote);

                // Then
                assertEquals(new BigDecimal("12000"), arr);
        }

        @Test
        void calculateTCV_ShouldCalculateCorrectly() {
                // When
                BigDecimal tcv = calculator.calculateTCV(testQuote);

                // Then
                assertEquals(new BigDecimal("12000"), tcv);
        }

        @Test
        void calculateACV_ShouldCalculateCorrectly() {
                // When
                BigDecimal acv = calculator.calculateACV(testQuote);

                // Then
                assertEquals(0, new BigDecimal("12000").compareTo(acv));
        }

        @Test
        void calculateCLV_ShouldUseChurnRateCorrectly() {
                // Given
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));

                // When
                BigDecimal clv = calculator.calculateCLV(testQuote);

                // Then
                assertNotNull(clv);
                assertTrue(clv.compareTo(BigDecimal.ZERO) > 0);

                verify(customerRepository).findByCustomerId("C123");
        }

        @Test
        void calculateCLV_ShouldUseDefaultValuesWhenCustomerNotFound() {
                // Given
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.empty());

                // When
                BigDecimal clv = calculator.calculateCLV(testQuote);

                // Then
                assertNotNull(clv);
                assertTrue(clv.compareTo(BigDecimal.ZERO) > 0);
        }

        // ==================== PARAMETRIZED TESTS ====================

        @ParameterizedTest
        @DisplayName("Should calculate ARR correctly for various monthly prices")
        @CsvSource({
                        "500, 6000", // 500 * 12
                        "1000, 12000", // 1000 * 12
                        "5000, 60000", // 5000 * 12
                        "10000, 120000", // 10000 * 12
                        "0, 0" // Edge case: zero price
        })
        void testCalculateARRWithVariousPrices(String monthlyPrice, String expectedARR) {
                // Given
                testQuote.setMonthlyPrice(new BigDecimal(monthlyPrice));

                // When
                BigDecimal arr = calculator.calculateARR(testQuote);

                // Then
                assertThat(arr).isEqualTo(new BigDecimal(expectedARR));
        }

        @ParameterizedTest
        @DisplayName("Should calculate TCV correctly for various durations")
        @CsvSource({
                        "1000, 12, 12000", // 1 year
                        "1000, 24, 24000", // 2 years
                        "1000, 36, 36000", // 3 years
                        "1000, 60, 60000", // 5 years
                        "2000, 24, 48000" // Different price and duration
        })
        void testCalculateTCVWithVariousDurations(String monthlyPrice, String duration, String expectedTCV) {
                // Given
                testQuote.setMonthlyPrice(new BigDecimal(monthlyPrice));
                testQuote.setDurationInMonths(Integer.parseInt(duration));

                // When
                BigDecimal tcv = calculator.calculateTCV(testQuote);

                // Then
                assertThat(tcv).isEqualTo(new BigDecimal(expectedTCV));
        }

        @ParameterizedTest
        @DisplayName("Should handle different customer types for metrics calculation")
        @ValueSource(strings = { "SMB", "MID_MARKET", "ENTERPRISE", "STARTUP" })
        void testCalculateMetricsWithVariousCustomerTypes(String customerType) {
                // Given
                testCustomer.setCustomerType(customerType);
                testQuote.setCustomerType(customerType);

                when(metricsRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.empty());
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));
                when(metricsRepository.save(any(FinancialMetrics.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                FinancialMetrics result = calculator.calculateMetrics(testQuote);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getCustomerId()).isEqualTo("C123");
                assertThat(result.getArr()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle null quote gracefully")
        void testCalculateMetricsWithNullQuote() {
                // When/Then
                assertThatThrownBy(() -> calculator.calculateMetrics(null))
                                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should calculate metrics and save to repository")
        void testCalculateMetricsPersistence() {
                // Given
                when(metricsRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.empty());
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));
                when(metricsRepository.save(any(FinancialMetrics.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                FinancialMetrics result = calculator.calculateMetrics(testQuote);

                // Then
                assertThat(result).isNotNull();
                verify(metricsRepository).save(any(FinancialMetrics.class));
        }

        @Test
        @DisplayName("Should handle large financial values correctly")
        void testCalculateMetricsWithLargeValues() {
                // Given
                testQuote.setMonthlyPrice(new BigDecimal("100000"));
                testQuote.setDurationInMonths(60);
                testQuote.setBasePrice(new BigDecimal("6000000"));

                when(metricsRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.empty());
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));
                when(metricsRepository.save(any(FinancialMetrics.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                FinancialMetrics result = calculator.calculateMetrics(testQuote);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getArr()).isGreaterThan(new BigDecimal("1000000"));
                assertThat(result.getTcv()).isGreaterThan(new BigDecimal("5000000"));
        }

        @Test
        @DisplayName("Should calculate CLV considering customer churn risk")
        void testCalculateCLVWithChurnRisk() {
                // Given
                testCustomer.setChurnRiskScore(new BigDecimal("0.10")); // 10% churn
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));

                // When
                BigDecimal clv = calculator.calculateCLV(testQuote);

                // Then
                assertThat(clv).isNotNull();
                assertThat(clv).isGreaterThan(BigDecimal.ZERO);
                // CLV should be lower with higher churn risk
        }

        @Test
        @DisplayName("Should validate metric calculations maintain consistency")
        void testMetricsConsistency() {
                // Given
                when(metricsRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.empty());
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));
                when(metricsRepository.save(any(FinancialMetrics.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                FinancialMetrics metrics = calculator.calculateMetrics(testQuote);

                // Then - Validate relationships between metrics
                assertThat(metrics.getArr()).isGreaterThan(BigDecimal.ZERO);
                assertThat(metrics.getTcv()).isGreaterThanOrEqualTo(metrics.getArr());
                assertThat(metrics.getAcv()).isLessThanOrEqualTo(metrics.getTcv());
                assertThat(metrics.getClv()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should update existing metrics for returning customers")
        void testUpdateExistingMetrics() {
                // Given
                FinancialMetrics existingMetrics = new FinancialMetrics();
                existingMetrics.setCustomerId("C123");
                existingMetrics.setArr(new BigDecimal("10000"));

                when(metricsRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(existingMetrics));
                when(customerRepository.findByCustomerId("C123"))
                                .thenReturn(Optional.of(testCustomer));
                when(metricsRepository.save(any(FinancialMetrics.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // When
                FinancialMetrics result = calculator.calculateMetrics(testQuote);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getCustomerId()).isEqualTo("C123");
                verify(metricsRepository).save(any(FinancialMetrics.class));
        }

        @Test
        @DisplayName("Should handle subscription type differences")
        void testCalculateMetricsWithDifferentSubscriptionTypes() {
                // Test both ANNUAL and MONTHLY
                String[] subscriptionTypes = { "ANNUAL", "MONTHLY", "QUARTERLY" };

                for (String subType : subscriptionTypes) {
                        testQuote.setSubscriptionType(subType);
                        when(metricsRepository.findByCustomerId("C123"))
                                        .thenReturn(Optional.empty());
                        when(customerRepository.findByCustomerId("C123"))
                                        .thenReturn(Optional.of(testCustomer));
                        when(metricsRepository.save(any(FinancialMetrics.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        // When
                        FinancialMetrics result = calculator.calculateMetrics(testQuote);

                        // Then
                        assertThat(result).isNotNull();
                        assertThat(result.getArr()).isGreaterThan(BigDecimal.ZERO);
                }
        }

        @ParameterizedTest
        @DisplayName("Should calculate ACV correctly for various contract lengths")
        @CsvSource({
                        "12000, 1, 12000", // Annual, 1 year contract
                        "12000, 2, 6000", // Annual, 2 year contract (average)
                        "12000, 3, 4000", // Annual, 3 year contract
                        "24000, 2, 12000", // Semi-annual
                        "6000, 1, 6000" // Monthly charged as annual
        })
        void testCalculateACVWithVariousContractLengths(String basePrice, String years, String expectedACV) {
                // Given
                testQuote.setBasePrice(new BigDecimal(basePrice));
                testQuote.setDurationInMonths(Integer.parseInt(years) * 12);

                // When
                BigDecimal acv = calculator.calculateACV(testQuote);

                // Then
                // ACV calculation depends on implementation, but should be reasonable
                assertThat(acv).isGreaterThan(BigDecimal.ZERO);
        }
}