package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.FinancialMetricsSnapshot;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.pricing.PricingStrategyFactory;
import com.example.pricerulesaidrools.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialMetricsServiceImplTest {

    @Mock
    private FinancialMetricsCalculator calculator;

    @Mock
    private PricingStrategyFactory pricingStrategyFactory;

    @Mock
    private QuoteRepository quoteRepository;

    @InjectMocks
    private FinancialMetricsServiceImpl service;

    private Quote testQuote;
    private FinancialMetrics testMetrics;

    @BeforeEach
    void setUp() {
        testQuote = Quote.builder()
                .quoteId("Q123")
                .customerId("C123")
                .monthlyPrice(new BigDecimal("1000"))
                .durationInMonths(12)
                .basePrice(new BigDecimal("12000"))
                .build();

        testMetrics = FinancialMetrics.builder()
                .customerId("C123")
                .arr(new BigDecimal("12000"))
                .tcv(new BigDecimal("12000"))
                .acv(new BigDecimal("12000"))
                .clv(new BigDecimal("240000"))
                .churnRiskScore(new BigDecimal("0.02"))
                .build();
    }

    @Test
    void calculateMetrics_ShouldReturnCalculatedMetrics() {
        // Given
        when(calculator.calculateMetrics(testQuote)).thenReturn(testMetrics);

        // When
        FinancialMetrics result = service.calculateMetrics(testQuote);

        // Then
        assertNotNull(result);
        assertEquals("C123", result.getCustomerId());
        assertEquals(new BigDecimal("12000"), result.getArr());
        assertEquals(new BigDecimal("12000"), result.getTcv());
        assertEquals(new BigDecimal("12000"), result.getAcv());
        assertEquals(new BigDecimal("240000"), result.getClv());
        assertEquals(new BigDecimal("0.02"), result.getChurnRiskScore());

        verify(calculator).calculateMetrics(testQuote);
    }

    @Test
    void getARR_ShouldReturnARRFromQuote() {
        // Given
        when(quoteRepository.findByQuoteId("Q123")).thenReturn(Optional.of(testQuote));
        when(calculator.calculateARR(testQuote)).thenReturn(new BigDecimal("12000"));

        // When
        BigDecimal arr = service.getARR("Q123");

        // Then
        assertEquals(new BigDecimal("12000"), arr);
        verify(quoteRepository).findByQuoteId("Q123");
        verify(calculator).calculateARR(testQuote);
    }

    @Test
    void getARR_ShouldThrowExceptionWhenQuoteNotFound() {
        // Given
        when(quoteRepository.findByQuoteId("Q123")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> service.getARR("Q123"));
        verify(quoteRepository).findByQuoteId("Q123");
        verifyNoInteractions(calculator);
    }

    @Test
    void getTCV_ShouldReturnTCVFromQuote() {
        // Given
        when(quoteRepository.findByQuoteId("Q123")).thenReturn(Optional.of(testQuote));
        when(calculator.calculateTCV(testQuote)).thenReturn(new BigDecimal("12000"));

        // When
        BigDecimal tcv = service.getTCV("Q123");

        // Then
        assertEquals(new BigDecimal("12000"), tcv);
        verify(calculator).calculateTCV(testQuote);
    }

    @Test
    void getACV_ShouldReturnACVFromQuote() {
        // Given
        when(quoteRepository.findByQuoteId("Q123")).thenReturn(Optional.of(testQuote));
        when(calculator.calculateACV(testQuote)).thenReturn(new BigDecimal("12000"));

        // When
        BigDecimal acv = service.getACV("Q123");

        // Then
        assertEquals(new BigDecimal("12000"), acv);
        verify(calculator).calculateACV(testQuote);
    }

    @Test
    void getCLV_ShouldReturnCLVFromQuote() {
        // Given
        when(quoteRepository.findByQuoteId("Q123")).thenReturn(Optional.of(testQuote));
        when(calculator.calculateCLV(testQuote)).thenReturn(new BigDecimal("240000"));

        // When
        BigDecimal clv = service.getCLV("Q123");

        // Then
        assertEquals(new BigDecimal("240000"), clv);
        verify(calculator).calculateCLV(testQuote);
    }

    @Test
    void getHistoricalMetrics_ShouldConvertSnapshotsToMetrics() {
        // Given
        FinancialMetricsSnapshot snapshot1 = FinancialMetricsSnapshot.builder()
                .customerId("C123")
                .arr(new BigDecimal("10000"))
                .tcv(new BigDecimal("10000"))
                .acv(new BigDecimal("10000"))
                .clv(new BigDecimal("200000"))
                .churnRiskScore(new BigDecimal("0.03"))
                .growthRate(new BigDecimal("0.1"))
                .createdAt(LocalDateTime.now().minusDays(30))
                .build();

        FinancialMetricsSnapshot snapshot2 = FinancialMetricsSnapshot.builder()
                .customerId("C123")
                .arr(new BigDecimal("12000"))
                .tcv(new BigDecimal("12000"))
                .acv(new BigDecimal("12000"))
                .clv(new BigDecimal("240000"))
                .churnRiskScore(new BigDecimal("0.02"))
                .growthRate(new BigDecimal("0.2"))
                .createdAt(LocalDateTime.now())
                .build();

        List<FinancialMetricsSnapshot> snapshots = Arrays.asList(snapshot1, snapshot2);
        when(calculator.getHistoricalMetrics("C123", Duration.ofDays(365))).thenReturn(snapshots);

        // When
        List<FinancialMetrics> result = service.getHistoricalMetrics("C123", Duration.ofDays(365));

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());

        FinancialMetrics metrics1 = result.get(0);
        assertEquals("C123", metrics1.getCustomerId());
        assertEquals(new BigDecimal("10000"), metrics1.getArr());
        assertEquals("STABLE", metrics1.getChurnTrend()); // Default value

        FinancialMetrics metrics2 = result.get(1);
        assertEquals("C123", metrics2.getCustomerId());
        assertEquals(new BigDecimal("12000"), metrics2.getArr());

        verify(calculator).getHistoricalMetrics("C123", Duration.ofDays(365));
    }

    @Test
    void calculateChurnRiskScore_ShouldReturnRiskScore() {
        // Given
        when(calculator.calculateChurnRiskScore("C123")).thenReturn(new BigDecimal("25"));

        // When
        BigDecimal churnRisk = calculator.calculateChurnRiskScore("C123");

        // Then
        assertEquals(new BigDecimal("25"), churnRisk);
        verify(calculator).calculateChurnRiskScore("C123");
    }
}