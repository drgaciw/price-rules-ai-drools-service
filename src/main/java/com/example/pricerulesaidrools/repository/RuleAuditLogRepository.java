package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.RuleAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for the RuleAuditLog entity
 */
@Repository
public interface RuleAuditLogRepository extends JpaRepository<RuleAuditLog, Long> {

    /**
     * Finds audit logs for a specific rule
     *
     * @param ruleId The rule ID
     * @return List of audit logs for the rule
     */
    List<RuleAuditLog> findByRuleId(Long ruleId);

    /**
     * Finds audit logs for a specific rule, paginated
     *
     * @param ruleId   The rule ID
     * @param pageable The pagination information
     * @return Page of audit logs for the rule
     */
    Page<RuleAuditLog> findByRuleId(Long ruleId, Pageable pageable);

    /**
     * Finds audit logs for a specific action
     *
     * @param action The action to search for
     * @return List of audit logs for the action
     */
    List<RuleAuditLog> findByAction(String action);

    /**
     * Finds audit logs performed by a specific user
     *
     * @param userId The user ID
     * @return List of audit logs performed by the user
     */
    @Query("SELECT log FROM RuleAuditLog log WHERE log.performedBy.id = :userId")
    List<RuleAuditLog> findByPerformedBy(@Param("userId") Long userId);

    /**
     * Finds audit logs for a specific time period
     *
     * @param startTime The start time
     * @param endTime   The end time
     * @return List of audit logs for the time period
     */
    List<RuleAuditLog> findByPerformedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Finds audit logs for rules in a specific rule set
     *
     * @param ruleSetId The rule set ID
     * @return List of audit logs for rules in the rule set
     */
    @Query("SELECT log FROM RuleAuditLog log WHERE log.rule.ruleSet.id = :ruleSetId")
    List<RuleAuditLog> findByRuleSetId(@Param("ruleSetId") Long ruleSetId);

    /**
     * Counts the number of audit logs for a specific rule
     *
     * @param ruleId The rule ID
     * @return The count of audit logs for the rule
     */
    long countByRuleId(Long ruleId);

    /**
     * Counts the number of audit logs for a specific action
     *
     * @param action The action
     * @return The count of audit logs for the action
     */
    long countByAction(String action);
}