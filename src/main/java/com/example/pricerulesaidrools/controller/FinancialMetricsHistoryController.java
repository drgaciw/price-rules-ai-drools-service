package com.example.pricerulesaidrools.controller;

import com.example.pricerulesaidrools.model.FinancialMetricsSnapshot;
import com.example.pricerulesaidrools.repository.FinancialMetricsSnapshotRepository;
import com.example.pricerulesaidrools.service.FinancialMetricsCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for accessing historical financial metrics
 */
@RestController
@RequestMapping("/api/financial-metrics/history")
@RequiredArgsConstructor
@Slf4j
public class FinancialMetricsHistoryController {
    
    private final FinancialMetricsCalculator metricsCalculator;
    private final FinancialMetricsSnapshotRepository snapshotRepository;
    
    /**
     * Get historical metrics for a customer for the specified period
     * 
     * @param customerId Customer ID
     * @param days Number of days to look back
     * @return List of metrics snapshots
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<List<FinancialMetricsSnapshot>> getHistoricalMetrics(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Getting historical metrics for customer {} over {} days", customerId, days);
        List<FinancialMetricsSnapshot> metrics = metricsCalculator.getHistoricalMetrics(
                customerId, Duration.ofDays(days));
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get historical metrics for a customer for a specific date range
     * 
     * @param customerId Customer ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of metrics snapshots
     */
    @GetMapping("/{customerId}/range")
    public ResponseEntity<List<FinancialMetricsSnapshot>> getHistoricalMetricsByDateRange(
            @PathVariable String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        log.info("Getting historical metrics for customer {} from {} to {}", 
                customerId, startDate, endDate);
        
        List<FinancialMetricsSnapshot> metrics = snapshotRepository.findByCustomerIdAndDateRange(
                customerId, startDate, endDate);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get metrics for a specific quarter
     * 
     * @param customerId Customer ID
     * @param quarter Quarter in format YYYY-QN (e.g., 2025-Q1)
     * @return List of metrics snapshots
     */
    @GetMapping("/{customerId}/quarter/{quarter}")
    public ResponseEntity<List<FinancialMetricsSnapshot>> getMetricsByQuarter(
            @PathVariable String customerId,
            @PathVariable String quarter) {
        
        log.info("Getting metrics for customer {} for quarter {}", customerId, quarter);
        List<FinancialMetricsSnapshot> metrics = snapshotRepository.findByCustomerIdAndQuarterOrderBySnapshotDateAsc(
                customerId, quarter);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get metrics for a specific month
     * 
     * @param customerId Customer ID
     * @param month Month in format YYYY-MM (e.g., 2025-01)
     * @return List of metrics snapshots
     */
    @GetMapping("/{customerId}/month/{month}")
    public ResponseEntity<List<FinancialMetricsSnapshot>> getMetricsByMonth(
            @PathVariable String customerId,
            @PathVariable String month) {
        
        log.info("Getting metrics for customer {} for month {}", customerId, month);
        List<FinancialMetricsSnapshot> metrics = snapshotRepository.findByCustomerIdAndMonthOrderBySnapshotDateAsc(
                customerId, month);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Create an on-demand snapshot of the current metrics
     * 
     * @param customerId Customer ID
     * @return The created snapshot
     */
    @PostMapping("/{customerId}/snapshot")
    public ResponseEntity<FinancialMetricsSnapshot> createSnapshot(
            @PathVariable String customerId) {
        
        log.info("Creating on-demand snapshot for customer {}", customerId);
        FinancialMetricsSnapshot snapshot = metricsCalculator.createMetricsSnapshot(
                customerId, FinancialMetricsSnapshot.SnapshotType.ON_DEMAND);
        
        return ResponseEntity.ok(snapshot);
    }
    
    /**
     * Get metrics by snapshot type
     * 
     * @param customerId Customer ID
     * @param type Snapshot type (DAILY, WEEKLY, MONTHLY, QUARTERLY, ANNUAL, ON_DEMAND)
     * @return List of metrics snapshots
     */
    @GetMapping("/{customerId}/type/{type}")
    public ResponseEntity<List<FinancialMetricsSnapshot>> getMetricsByType(
            @PathVariable String customerId,
            @PathVariable String type) {
        
        FinancialMetricsSnapshot.SnapshotType snapshotType = 
                FinancialMetricsSnapshot.SnapshotType.valueOf(type.toUpperCase());
        
        log.info("Getting metrics for customer {} of type {}", customerId, snapshotType);
        List<FinancialMetricsSnapshot> metrics = 
                snapshotRepository.findByCustomerIdAndSnapshotTypeOrderBySnapshotDateDesc(
                        customerId, snapshotType);
        
        return ResponseEntity.ok(metrics);
    }
    
    /**
     * Get metrics statistics and summary
     * 
     * @param customerId Customer ID
     * @return Statistics and summary
     */
    @GetMapping("/{customerId}/stats")
    public ResponseEntity<Map<String, Object>> getMetricsStats(
            @PathVariable String customerId) {
        
        // Get all snapshots for the customer
        List<FinancialMetricsSnapshot> snapshots = 
                snapshotRepository.findByCustomerIdOrderBySnapshotDateDesc(customerId);
        
        if (snapshots.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Get most recent snapshot
        FinancialMetricsSnapshot latest = snapshots.get(0);
        
        // Calculate growth rates if multiple snapshots exist
        double arrGrowthRate = 0.0;
        double tcvGrowthRate = 0.0;
        double acvGrowthRate = 0.0;
        double clvGrowthRate = 0.0;
        
        if (snapshots.size() > 1) {
            FinancialMetricsSnapshot oldest = snapshots.get(snapshots.size() - 1);
            
            arrGrowthRate = calculateGrowthRate(
                    oldest.getArr().doubleValue(), latest.getArr().doubleValue());
            tcvGrowthRate = calculateGrowthRate(
                    oldest.getTcv().doubleValue(), latest.getTcv().doubleValue());
            acvGrowthRate = calculateGrowthRate(
                    oldest.getAcv().doubleValue(), latest.getAcv().doubleValue());
            clvGrowthRate = calculateGrowthRate(
                    oldest.getClv().doubleValue(), latest.getClv().doubleValue());
        }
        
        // Build response
        Map<String, Object> stats = Map.of(
                "customerId", customerId,
                "snapshotCount", snapshots.size(),
                "latestSnapshot", latest,
                "arrGrowthRate", arrGrowthRate,
                "tcvGrowthRate", tcvGrowthRate,
                "acvGrowthRate", acvGrowthRate,
                "clvGrowthRate", clvGrowthRate,
                "churnRiskTrend", getChurnRiskTrend(snapshots)
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Calculate growth rate between two values
     * 
     * @param initialValue Initial value
     * @param finalValue Final value
     * @return Growth rate as a percentage
     */
    private double calculateGrowthRate(double initialValue, double finalValue) {
        if (initialValue == 0) {
            return 0.0;
        }
        return ((finalValue - initialValue) / initialValue) * 100;
    }
    
    /**
     * Determine churn risk trend from snapshots
     * 
     * @param snapshots List of snapshots ordered by date (newest first)
     * @return Trend description
     */
    private String getChurnRiskTrend(List<FinancialMetricsSnapshot> snapshots) {
        if (snapshots.size() < 2) {
            return "STABLE";
        }
        
        // Get last 3 snapshots or all if less than 3
        int count = Math.min(snapshots.size(), 3);
        List<FinancialMetricsSnapshot> recent = snapshots.subList(0, count);
        
        // Compare first (newest) and last (oldest) in the recent list
        double newest = recent.get(0).getChurnRiskScore().doubleValue();
        double oldest = recent.get(count - 1).getChurnRiskScore().doubleValue();
        
        double change = newest - oldest;
        
        if (Math.abs(change) < 0.05) {
            return "STABLE";
        } else if (change > 0) {
            return "INCREASING";
        } else {
            return "DECREASING";
        }
    }
}