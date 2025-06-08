package com.example.pricerulesaidrools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a history of rule executions
 */
@Entity
@Table(name = "rule_execution_history", indexes = {
    @Index(name = "idx_rule_execution_history_rule_id", columnList = "rule_id"),
    @Index(name = "idx_rule_execution_history_rule_set_id", columnList = "rule_set_id"),
    @Index(name = "idx_rule_execution_history_executed_at", columnList = "executed_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleExecutionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private Rule rule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_set_id")
    private RuleSet ruleSet;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "input_data", columnDefinition = "TEXT")
    private String inputData;

    @Column(name = "result", columnDefinition = "TEXT")
    private String result;

    @Column(name = "successful")
    private Boolean successful;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executed_by")
    private User executedBy;

    @CreationTimestamp
    @Column(name = "executed_at", nullable = false, updatable = false)
    private LocalDateTime executedAt;
}