package com.example.pricerulesaidrools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for storing historical snapshots of financial metrics for trend analysis
 */
@Entity
@Table(name = "financial_metrics_snapshots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMetricsSnapshot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Column(name = "snapshot_date", nullable = false)
    private LocalDateTime snapshotDate;
    
    @Column(name = "arr", nullable = false)
    private BigDecimal arr; // Annual Recurring Revenue
    
    @Column(name = "tcv", nullable = false)
    private BigDecimal tcv; // Total Contract Value
    
    @Column(name = "acv", nullable = false)
    private BigDecimal acv; // Annual Contract Value
    
    @Column(name = "clv", nullable = false)
    private BigDecimal clv; // Customer Lifetime Value
    
    @Column(name = "churn_risk_score")
    private BigDecimal churnRiskScore;
    
    @Column(name = "growth_rate")
    private BigDecimal growthRate;
    
    @Column(name = "quarter")
    private String quarter; // e.g., "2025-Q1"
    
    @Column(name = "month")
    private String month; // e.g., "2025-01"
    
    @Column(name = "snapshot_type")
    @Enumerated(EnumType.STRING)
    private SnapshotType snapshotType;
    
    @Column(name = "notes")
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Creates a snapshot from a FinancialMetrics entity
     * 
     * @param metrics The metrics to create a snapshot from
     * @param snapshotType The type of snapshot
     * @return A new snapshot entity
     */
    public static FinancialMetricsSnapshot fromMetrics(FinancialMetrics metrics, SnapshotType snapshotType) {
        LocalDateTime now = LocalDateTime.now();
        String quarter = String.format("%d-Q%d", now.getYear(), (now.getMonthValue() - 1) / 3 + 1);
        String month = String.format("%d-%02d", now.getYear(), now.getMonthValue());
        
        return FinancialMetricsSnapshot.builder()
                .customerId(metrics.getCustomerId())
                .snapshotDate(now)
                .arr(metrics.getArr())
                .tcv(metrics.getTcv())
                .acv(metrics.getAcv())
                .clv(metrics.getClv())
                .churnRiskScore(metrics.getChurnRiskScore())
                .growthRate(metrics.getGrowthRate())
                .quarter(quarter)
                .month(month)
                .snapshotType(snapshotType)
                .build();
    }
    
    /**
     * The type of snapshot
     */
    public enum SnapshotType {
        DAILY,      // Daily snapshot for detailed analysis
        WEEKLY,     // Weekly snapshot for trend analysis
        MONTHLY,    // Monthly snapshot for reporting
        QUARTERLY,  // Quarterly snapshot for business reviews
        ANNUAL,     // Annual snapshot for year-over-year comparisons
        ON_DEMAND   // On-demand snapshot triggered by user
    }
}