package com.example.pricerulesaidrools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialMetrics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
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
    
    @Column(name = "contract_months")
    private Integer contractMonths;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}