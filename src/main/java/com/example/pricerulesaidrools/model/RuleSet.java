package com.example.pricerulesaidrools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a set of related pricing rules
 */
@Entity
@Table(name = "rule_sets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

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
    private boolean active = true;

    @OneToMany(mappedBy = "ruleSet", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Rule> rules = new ArrayList<>();

    /**
     * Adds a rule to this rule set
     *
     * @param rule The rule to add
     * @return This rule set for method chaining
     */
    public RuleSet addRule(Rule rule) {
        rules.add(rule);
        rule.setRuleSet(this);
        return this;
    }

    /**
     * Removes a rule from this rule set
     *
     * @param rule The rule to remove
     * @return This rule set for method chaining
     */
    public RuleSet removeRule(Rule rule) {
        rules.remove(rule);
        rule.setRuleSet(null);
        return this;
    }
}