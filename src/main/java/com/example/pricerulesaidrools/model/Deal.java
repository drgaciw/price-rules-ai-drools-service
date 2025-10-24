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

/**
 * Deal entity representing a business deal/opportunity for pricing evaluation.
 * Used by the AI routing service to determine specialized review paths.
 */
@Entity
@Table(name = "deals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "deal_id", nullable = false, unique = true)
    private String dealId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    private Customer customer;

    @Column(name = "deal_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DealType type;

    @Column(name = "deal_value", nullable = false)
    private BigDecimal value;

    @Column(name = "complexity", nullable = false)
    @Enumerated(EnumType.STRING)
    private DealComplexity complexity;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "contract_length_months")
    private Integer contractLengthMonths;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private DealStatus status;

    @Column(name = "risk_score")
    private Double riskScore;

    @Column(name = "technical_requirements", columnDefinition = "TEXT")
    private String technicalRequirements;

    @Column(name = "billing_terms")
    private String billingTerms;

    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum DealType {
        STANDARD,
        ENTERPRISE,
        STRATEGIC,
        GOVERNMENT,
        NON_PROFIT,
        PARTNER
    }

    public enum DealComplexity {
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }

    public enum DealStatus {
        DRAFT,
        UNDER_REVIEW,
        APPROVED,
        REJECTED,
        NEGOTIATION,
        CLOSED_WON,
        CLOSED_LOST
    }
}