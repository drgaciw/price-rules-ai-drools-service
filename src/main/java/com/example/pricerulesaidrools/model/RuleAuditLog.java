package com.example.pricerulesaidrools.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing an audit log entry for rule operations
 */
@Entity
@Table(name = "rule_audit_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    private Rule rule;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;

    @CreationTimestamp
    @Column(name = "performed_at", nullable = false, updatable = false)
    private LocalDateTime performedAt;

    /**
     * Common audit action constants
     */
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_ACTIVATE = "ACTIVATE";
    public static final String ACTION_DEACTIVATE = "DEACTIVATE";
    public static final String ACTION_PUBLISH = "PUBLISH";
    public static final String ACTION_EXECUTE = "EXECUTE";
    public static final String ACTION_VALIDATE = "VALIDATE";
}