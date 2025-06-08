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
@Table(name = "customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "customer_id", nullable = false, unique = true)
    private String customerId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "customer_type")
    private String customerType;
    
    @Column(name = "segment")
    private String segment;
    
    @Column(name = "contract_start_date")
    private LocalDateTime contractStartDate;
    
    @Column(name = "tenure_months")
    private Integer tenureMonths;
    
    @Column(name = "total_lifetime_value")
    private BigDecimal totalLifetimeValue;
    
    @Column(name = "churn_risk_score")
    private BigDecimal churnRiskScore;
    
    @Column(name = "churn_risk_factors")
    private String churnRiskFactors;
    
    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate;
    
    @Column(name = "purchase_frequency")
    private Integer purchaseFrequency;
    
    @Column(name = "support_tickets_count")
    private Integer supportTicketsCount;
    
    @Column(name = "payment_issues_count")
    private Integer paymentIssuesCount;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}