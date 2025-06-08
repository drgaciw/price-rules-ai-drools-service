package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.repository.FinancialMetricsRepository;
import com.example.pricerulesaidrools.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for tracking and retrieving historical financial metrics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsHistoryService {
    
    private final FinancialMetricsRepository metricsRepository;
    private final QuoteRepository quoteRepository;
    
    /**
     * Record metrics history for a customer
     * 
     * @param customerId The customer ID
     * @param metrics The metrics to record
     */
    @Transactional
    public void recordMetricsHistory(String customerId, FinancialMetrics metrics) {
        log.info("Recording metrics history for customer ID: {}", customerId);
        
        // In a production system, we would store historical records in a separate table
        // For this implementation, we're using the main metrics table
        
        // Create a copy of the metrics with a new ID to keep history
        FinancialMetrics historicalMetrics = FinancialMetrics.builder()
                .customerId(customerId)
                .arr(metrics.getArr())
                .tcv(metrics.getTcv())
                .acv(metrics.getAcv())
                .clv(metrics.getClv())
                .churnRiskScore(metrics.getChurnRiskScore())
                .contractMonths(metrics.getContractMonths())
                .build();
        
        metricsRepository.save(historicalMetrics);
    }
    
    /**
     * Get historical metrics for a customer within a time period
     * 
     * @param customerId The customer ID
     * @param period The time period to look back
     * @return List of historical metrics
     */
    @Transactional(readOnly = true)
    public List<FinancialMetrics> getHistoricalMetrics(String customerId, Duration period) {
        log.info("Getting historical metrics for customer ID: {} over period: {}", customerId, period);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minus(period);
        
        // Get all quotes for the customer within the time period
        List<Quote> quotes = quoteRepository.findByCustomerId(customerId).stream()
                .filter(quote -> quote.getCreatedAt().isAfter(cutoffDate))
                .collect(Collectors.toList());
        
        // Get metrics for each quote
        // In a real implementation, we would have a dedicated history table and query
        return quotes.stream()
                .map(Quote::getQuoteId)
                .map(quoteId -> metricsRepository.findByCustomerId(customerId))
                .filter(optional -> optional.isPresent())
                .map(optional -> optional.get())
                .collect(Collectors.toList());
    }
    
    /**
     * Track metrics changes for audit and historical analysis
     * 
     * @param customerId The customer ID
     * @param previousMetrics The previous metrics
     * @param newMetrics The new metrics
     */
    @Transactional
    public void trackMetricsChanges(String customerId, FinancialMetrics previousMetrics, FinancialMetrics newMetrics) {
        log.info("Tracking metrics changes for customer ID: {}", customerId);
        
        // In a production system, we would store detailed change history
        // For this implementation, we'll just log the changes
        
        if (previousMetrics != null && newMetrics != null) {
            log.info("Metrics changes for customer {}: ARR: {} -> {}, CLV: {} -> {}",
                    customerId,
                    previousMetrics.getArr(), newMetrics.getArr(),
                    previousMetrics.getClv(), newMetrics.getClv());
        }
    }
}