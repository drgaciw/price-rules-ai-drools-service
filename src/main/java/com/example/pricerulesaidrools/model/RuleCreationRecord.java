package com.example.pricerulesaidrools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity to store rule creation records in database instead of memory
 */
@Entity
@Table(name = "rule_creation_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleCreationRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_id", nullable = false, unique = true)
    private String ruleId;
    
    @Column(name = "business_requirement", columnDefinition = "TEXT")
    private String businessRequirement;
    
    @Column(name = "generated_rule", columnDefinition = "TEXT")
    private String generatedRule;
    
    @Column(name = "rule_name")
    private String ruleName;
    
    @Column(name = "rule_type")
    private String ruleType;
    
    @Column(name = "confidence_score")
    private Double confidenceScore;
    
    @Column(name = "validation_status")
    private String validationStatus;
    
    @Column(name = "deployment_status")
    private String deploymentStatus;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}