package com.example.pricerulesaidrools.service;

import com.example.pricerulesaidrools.model.Customer;
import com.example.pricerulesaidrools.model.FinancialMetrics;
import com.example.pricerulesaidrools.model.FinancialMetricsSnapshot;
import com.example.pricerulesaidrools.model.Quote;
import com.example.pricerulesaidrools.repository.CustomerRepository;
import com.example.pricerulesaidrools.repository.FinancialMetricsRepository;
import com.example.pricerulesaidrools.repository.FinancialMetricsSnapshotRepository;
import com.example.pricerulesaidrools.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialMetricsCalculator {
    
    private final FinancialMetricsRepository metricsRepository;
    private final CustomerRepository customerRepository;
    private final QuoteRepository quoteRepository;
    private final FinancialMetricsSnapshotRepository snapshotRepository;
    
    @Value("${financial-metrics.default-churn-rate:0.03}")
    private BigDecimal defaultChurnRate;
    
    @Value("${financial-metrics.default-customer-lifespan:36}")
    private int defaultCustomerLifespan;
    
    /**
     * Calculate financial metrics for a quote
     * 
     * @param quote The quote to calculate metrics for
     * @return The calculated financial metrics
     */
    @Transactional
    public FinancialMetrics calculateMetrics(Quote quote) {
        log.info("Calculating financial metrics for quote: {}", quote.getQuoteId());
        
        // Find existing metrics or create new
        FinancialMetrics metrics = metricsRepository.findByCustomerId(quote.getCustomerId())
                .orElse(FinancialMetrics.builder()
                        .customerId(quote.getCustomerId())
                        .build());
        
        // Calculate ARR (Annual Recurring Revenue)
        BigDecimal arr = calculateARR(quote);
        metrics.setArr(arr);
        
        // Calculate TCV (Total Contract Value)
        BigDecimal tcv = calculateTCV(quote);
        metrics.setTcv(tcv);
        
        // Calculate ACV (Annual Contract Value)
        BigDecimal acv = calculateACV(quote);
        metrics.setAcv(acv);
        
        // Calculate CLV (Customer Lifetime Value)
        BigDecimal clv = calculateCLV(quote);
        metrics.setClv(clv);
        
        // Calculate churn risk score
        BigDecimal churnRiskScore = calculateChurnRiskScore(quote.getCustomerId());
        metrics.setChurnRiskScore(churnRiskScore);
        
        // Set contract months
        metrics.setContractMonths(quote.getDurationInMonths());
        
        // Save and return
        return metricsRepository.save(metrics);
    }
    
    /**
     * Calculate Annual Recurring Revenue (ARR)
     * ARR = Monthly Price * 12
     * 
     * @param quote The quote to calculate ARR for
     * @return The calculated ARR
     */
    public BigDecimal calculateARR(Quote quote) {
        return quote.getMonthlyPrice().multiply(BigDecimal.valueOf(12));
    }
    
    /**
     * Calculate Total Contract Value (TCV)
     * TCV = Monthly Price * Duration (in months)
     * 
     * @param quote The quote to calculate TCV for
     * @return The calculated TCV
     */
    public BigDecimal calculateTCV(Quote quote) {
        return quote.getMonthlyPrice().multiply(BigDecimal.valueOf(quote.getDurationInMonths()));
    }
    
    /**
     * Calculate Annual Contract Value (ACV)
     * ACV = TCV / Years
     * 
     * @param quote The quote to calculate ACV for
     * @return The calculated ACV
     */
    public BigDecimal calculateACV(Quote quote) {
        BigDecimal tcv = calculateTCV(quote);
        BigDecimal years = BigDecimal.valueOf(quote.getDurationInMonths()).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        return tcv.divide(years, 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate Customer Lifetime Value (CLV)
     * CLV = ARR * Expected Duration (in years) * (1 - Churn Rate)
     * 
     * @param quote The quote to calculate CLV for
     * @return The calculated CLV
     */
    public BigDecimal calculateCLV(Quote quote) {
        BigDecimal arr = calculateARR(quote);
        int expectedDuration = quote.getExpectedDuration() != null ? 
                quote.getExpectedDuration() : defaultCustomerLifespan;
        
        BigDecimal churnRate = getChurnRate(quote.getCustomerId());
        BigDecimal retentionRate = BigDecimal.ONE.subtract(churnRate);
        
        BigDecimal years = BigDecimal.valueOf(expectedDuration).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        
        return arr.multiply(years).multiply(retentionRate).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate churn risk score for a customer
     * Score is between 0.0 and 1.0, where higher means higher risk of churning
     * 
     * @param customerId The customer ID to calculate risk for
     * @return The calculated churn risk score
     */
    public BigDecimal calculateChurnRiskScore(String customerId) {
        Customer customer = customerRepository.findByCustomerId(customerId).orElse(null);
        
        if (customer == null) {
            return BigDecimal.valueOf(0.5); // Default medium risk for unknown customers
        }
        
        // Calculate risk based on various factors
        BigDecimal score = BigDecimal.ZERO;
        int factors = 0;
        
        // Factor 1: Tenure - longer tenure means lower churn risk
        if (customer.getTenureMonths() != null) {
            int tenureMonths = customer.getTenureMonths();
            if (tenureMonths < 3) {
                score = score.add(BigDecimal.valueOf(0.8)); // Very high risk for new customers
            } else if (tenureMonths < 12) {
                score = score.add(BigDecimal.valueOf(0.6)); // High risk
            } else if (tenureMonths < 24) {
                score = score.add(BigDecimal.valueOf(0.4)); // Medium risk
            } else if (tenureMonths < 36) {
                score = score.add(BigDecimal.valueOf(0.2)); // Low risk
            } else {
                score = score.add(BigDecimal.valueOf(0.1)); // Very low risk
            }
            factors++;
        }
        
        // Factor 2: Support tickets - more tickets means higher churn risk
        if (customer.getSupportTicketsCount() != null) {
            int tickets = customer.getSupportTicketsCount();
            if (tickets > 10) {
                score = score.add(BigDecimal.valueOf(0.8));
            } else if (tickets > 5) {
                score = score.add(BigDecimal.valueOf(0.6));
            } else if (tickets > 3) {
                score = score.add(BigDecimal.valueOf(0.4));
            } else if (tickets > 1) {
                score = score.add(BigDecimal.valueOf(0.2));
            } else {
                score = score.add(BigDecimal.valueOf(0.1));
            }
            factors++;
        }
        
        // Factor 3: Payment issues - more issues means higher churn risk
        if (customer.getPaymentIssuesCount() != null) {
            int issues = customer.getPaymentIssuesCount();
            if (issues > 3) {
                score = score.add(BigDecimal.valueOf(0.9));
            } else if (issues > 1) {
                score = score.add(BigDecimal.valueOf(0.7));
            } else if (issues == 1) {
                score = score.add(BigDecimal.valueOf(0.4));
            } else {
                score = score.add(BigDecimal.valueOf(0.1));
            }
            factors++;
        }
        
        // Factor 4: Purchase frequency - more frequent purchases mean lower churn risk
        if (customer.getPurchaseFrequency() != null) {
            int frequency = customer.getPurchaseFrequency();
            if (frequency > 10) {
                score = score.add(BigDecimal.valueOf(0.1));
            } else if (frequency > 5) {
                score = score.add(BigDecimal.valueOf(0.2));
            } else if (frequency > 3) {
                score = score.add(BigDecimal.valueOf(0.3));
            } else if (frequency > 1) {
                score = score.add(BigDecimal.valueOf(0.5));
            } else {
                score = score.add(BigDecimal.valueOf(0.7));
            }
            factors++;
        }
        
        // Calculate average score
        if (factors > 0) {
            score = score.divide(BigDecimal.valueOf(factors), 2, RoundingMode.HALF_UP);
        } else {
            score = BigDecimal.valueOf(0.5); // Default medium risk
        }
        
        return score;
    }
    
    /**
     * Get churn rate for a customer
     * 
     * @param customerId The customer ID
     * @return The churn rate as a decimal (0.0 - 1.0)
     */
    private BigDecimal getChurnRate(String customerId) {
        Optional<Customer> customerOpt = customerRepository.findByCustomerId(customerId);
        
        if (customerOpt.isPresent() && customerOpt.get().getChurnRiskScore() != null) {
            return customerOpt.get().getChurnRiskScore();
        }
        
        return defaultChurnRate;
    }
    
    /**
     * Get historical financial metrics for a customer over a period
     * 
     * @param customerId The customer ID
     * @param period The time period to look back
     * @return List of historical financial metrics snapshots
     */
    @Transactional(readOnly = true)
    public List<FinancialMetricsSnapshot> getHistoricalMetrics(String customerId, Duration period) {
        LocalDateTime cutoffDate = LocalDateTime.now().minus(period);
        LocalDateTime endDate = LocalDateTime.now();
        
        log.info("Retrieving historical metrics for customer {} from {} to {}", 
                customerId, cutoffDate, endDate);
        
        // Get snapshots within date range
        List<FinancialMetricsSnapshot> snapshots = snapshotRepository.findByCustomerIdAndDateRange(
                customerId, cutoffDate, endDate);
        
        // If snapshots are available, return them
        if (!snapshots.isEmpty()) {
            log.info("Found {} historical metrics snapshots for customer {}", snapshots.size(), customerId);
            return snapshots;
        }
        
        // If no snapshots, generate them from historical quotes
        List<Quote> quotes = quoteRepository.findByCustomerId(customerId);
        log.info("No snapshots found, generating from {} historical quotes", quotes.size());
        
        // Filter quotes by date
        List<Quote> filteredQuotes = quotes.stream()
                .filter(q -> q.getCreatedAt().isAfter(cutoffDate) && q.getCreatedAt().isBefore(endDate))
                .toList();
        
        // Generate metrics from quotes
        List<FinancialMetricsSnapshot> generatedSnapshots = new ArrayList<>();
        for (Quote quote : filteredQuotes) {
            // Calculate metrics for the quote
            FinancialMetrics metrics = calculateMetricsForHistory(quote);
            
            // Create snapshot from metrics
            FinancialMetricsSnapshot snapshot = FinancialMetricsSnapshot.fromMetrics(
                    metrics, FinancialMetricsSnapshot.SnapshotType.ON_DEMAND);
            
            // Set snapshot date to quote creation date for historical accuracy
            snapshot.setSnapshotDate(quote.getCreatedAt());
            
            // Add notes to identify source
            snapshot.setNotes("Generated from historical quote " + quote.getQuoteId());
            
            // Add to list and save
            generatedSnapshots.add(snapshot);
            snapshotRepository.save(snapshot);
        }
        
        log.info("Generated {} historical metrics snapshots for customer {}", generatedSnapshots.size(), customerId);
        return generatedSnapshots;
    }
    
    /**
     * Calculate metrics for historical analysis (without saving to main metrics table)
     */
    private FinancialMetrics calculateMetricsForHistory(Quote quote) {
        // Create metrics entity without saving
        FinancialMetrics metrics = FinancialMetrics.builder()
                .customerId(quote.getCustomerId())
                .arr(calculateARR(quote))
                .tcv(calculateTCV(quote))
                .acv(calculateACV(quote))
                .clv(calculateCLV(quote))
                .churnRiskScore(calculateChurnRiskScore(quote.getCustomerId()))
                .contractMonths(quote.getDurationInMonths())
                .build();
        
        return metrics;
    }
    
    /**
     * Create a snapshot of the current metrics for a customer
     * 
     * @param customerId The customer ID
     * @param snapshotType The type of snapshot
     * @return The created snapshot
     */
    @Transactional
    public FinancialMetricsSnapshot createMetricsSnapshot(String customerId, 
                                                          FinancialMetricsSnapshot.SnapshotType snapshotType) {
        // Get current metrics
        FinancialMetrics metrics = metricsRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("No metrics found for customer: " + customerId));
        
        // Create snapshot
        FinancialMetricsSnapshot snapshot = FinancialMetricsSnapshot.fromMetrics(metrics, snapshotType);
        
        // Save and return
        return snapshotRepository.save(snapshot);
    }
}