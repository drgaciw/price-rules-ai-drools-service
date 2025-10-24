package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.Customer;
import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.repository.CustomerRepository;
import com.example.pricerulesaidrools.repository.FinancialMetricsRepository;
import com.example.pricerulesaidrools.repository.FinancialMetricsSnapshotRepository;
import com.example.pricerulesaidrools.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

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
                .customerName("Test Customer")
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
        assertEquals(new BigDecimal("12000"), result.getArr());
        assertEquals(new BigDecimal("12000"), result.getTcv());
        assertEquals(new BigDecimal("12000"), result.getAcv());
        assertNotNull(result.getClv());
        assertEquals(new BigDecimal("0.02"), result.getChurnRiskScore());
        
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
        assertEquals(new BigDecimal("12000"), acv);
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
    
    @Test
    void getChurnRate_ShouldReturnCustomerChurnRate() {
        // Given
        when(customerRepository.findByCustomerId("C123"))
                .thenReturn(Optional.of(testCustomer));
        
        // When
        BigDecimal churnRate = calculator.getChurnRate("C123");
        
        // Then
        assertEquals(new BigDecimal("0.02"), churnRate);
    }
    
    @Test
    void getChurnRate_ShouldReturnDefaultWhenCustomerNotFound() {
        // Given
        when(customerRepository.findByCustomerId("C123"))
                .thenReturn(Optional.empty());
        
        // When
        BigDecimal churnRate = calculator.getChurnRate("C123");
        
        // Then
        assertEquals(new BigDecimal("0.03"), churnRate);
    }
}