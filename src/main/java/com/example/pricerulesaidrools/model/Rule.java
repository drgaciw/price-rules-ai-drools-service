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
 * Entity representing a pricing rule in the Drools rule engine
 */
@Entity
@Table(name = "rules", uniqueConstraints = {
    @UniqueConstraint(name = "uk_rule_set_name_version", columnNames = {"rule_set_id", "name", "version"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_set_id", nullable = false)
    private RuleSet ruleSet;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RuleStatus status = RuleStatus.DRAFT;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "active")
    @Builder.Default
    private boolean active = true;

    @Column(name = "ai_generated")
    @Builder.Default
    private boolean aiGenerated = false;

    /**
     * Rule status enumeration
     */
    public enum RuleStatus {
        DRAFT, PENDING_REVIEW, APPROVED, PUBLISHED, DEPRECATED, ARCHIVED
    }

    /**
     * Increments the version number of this rule
     *
     * @return The new version number
     */
    public int incrementVersion() {
        this.version++;
        return this.version;
    }

    /**
     * Creates a new version of this rule
     *
     * @param newContent The content for the new version
     * @return A new rule object with incremented version
     */
    public Rule createNewVersion(String newContent) {
        return Rule.builder()
                .ruleSet(this.ruleSet)
                .name(this.name)
                .description(this.description)
                .content(newContent)
                .version(this.version + 1)
                .status(RuleStatus.DRAFT)
                .priority(this.priority)
                .createdBy(this.createdBy)
                .active(true)
                .aiGenerated(this.aiGenerated)
                .build();
    }
}