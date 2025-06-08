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
@Table(name = "quotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "quote_id", nullable = false, unique = true)
    private String quoteId;
    
    @Column(name = "customer_id", nullable = false)
    private String customerId;
    
    @Column(name = "product_id")
    private String productId;
    
    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;
    
    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;
    
    @Column(name = "final_price")
    private BigDecimal finalPrice;
    
    @Column(name = "duration_in_months", nullable = false)
    private Integer durationInMonths;
    
    @Column(name = "expected_duration")
    private Integer expectedDuration;
    
    @Column(name = "customer_tenure_months")
    private Integer customerTenureMonths;
    
    @Column(name = "customer_type")
    private String customerType;
    
    @Column(name = "subscription_type")
    private String subscriptionType;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private QuoteStatus status;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    public enum QuoteStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED, EXPIRED
    }
}