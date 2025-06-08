package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.RuleExecutionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for the RuleExecutionHistory entity
 */
@Repository
public interface RuleExecutionHistoryRepository extends JpaRepository<RuleExecutionHistory, Long> {

    /**
     * Finds execution history entries for a specific rule
     *
     * @param ruleId The rule ID
     * @return List of execution history entries for the rule
     */
    List<RuleExecutionHistory> findByRuleId(Long ruleId);

    /**
     * Finds execution history entries for a specific rule set
     *
     * @param ruleSetId The rule set ID
     * @return List of execution history entries for the rule set
     */
    List<RuleExecutionHistory> findByRuleSetId(Long ruleSetId);

    /**
     * Finds execution history entries for a specific rule set, paginated
     *
     * @param ruleSetId The rule set ID
     * @param pageable  The pagination information
     * @return Page of execution history entries for the rule set
     */
    Page<RuleExecutionHistory> findByRuleSetId(Long ruleSetId, Pageable pageable);

    /**
     * Finds successful execution history entries
     *
     * @return List of successful execution history entries
     */
    List<RuleExecutionHistory> findBySuccessfulTrue();

    /**
     * Finds failed execution history entries
     *
     * @return List of failed execution history entries
     */
    List<RuleExecutionHistory> findBySuccessfulFalse();

    /**
     * Finds execution history entries executed by a specific user
     *
     * @param userId The user ID
     * @return List of execution history entries executed by the user
     */
    @Query("SELECT h FROM RuleExecutionHistory h WHERE h.executedBy.id = :userId")
    List<RuleExecutionHistory> findByExecutedBy(@Param("userId") Long userId);

    /**
     * Finds execution history entries for a specific time period
     *
     * @param startTime The start time
     * @param endTime   The end time
     * @return List of execution history entries for the time period
     */
    List<RuleExecutionHistory> findByExecutedAtBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Calculates average execution time for a rule
     *
     * @param ruleId The rule ID
     * @return The average execution time
     */
    @Query("SELECT AVG(h.executionTimeMs) FROM RuleExecutionHistory h WHERE h.rule.id = :ruleId AND h.successful = true")
    Double calculateAverageExecutionTimeForRule(@Param("ruleId") Long ruleId);

    /**
     * Calculates average execution time for a rule set
     *
     * @param ruleSetId The rule set ID
     * @return The average execution time
     */
    @Query("SELECT AVG(h.executionTimeMs) FROM RuleExecutionHistory h WHERE h.ruleSet.id = :ruleSetId AND h.successful = true")
    Double calculateAverageExecutionTimeForRuleSet(@Param("ruleSetId") Long ruleSetId);

    /**
     * Counts the number of executions for a rule
     *
     * @param ruleId The rule ID
     * @return The count of executions for the rule
     */
    long countByRuleId(Long ruleId);

    /**
     * Counts the number of executions for a rule set
     *
     * @param ruleSetId The rule set ID
     * @return The count of executions for the rule set
     */
    long countByRuleSetId(Long ruleSetId);

    /**
     * Counts the number of successful executions for a rule set
     *
     * @param ruleSetId The rule set ID
     * @return The count of successful executions for the rule set
     */
    long countByRuleSetIdAndSuccessfulTrue(Long ruleSetId);

    /**
     * Counts the number of failed executions for a rule set
     *
     * @param ruleSetId The rule set ID
     * @return The count of failed executions for the rule set
     */
    long countByRuleSetIdAndSuccessfulFalse(Long ruleSetId);
}