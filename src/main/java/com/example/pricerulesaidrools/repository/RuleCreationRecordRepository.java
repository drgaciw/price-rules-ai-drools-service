package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.RuleCreationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RuleCreationRecord entities
 */
@Repository
public interface RuleCreationRecordRepository extends JpaRepository<RuleCreationRecord, Long> {
    
    /**
     * Find rule creation record by rule ID
     * 
     * @param ruleId The rule ID to search for
     * @return Optional containing the record if found
     */
    Optional<RuleCreationRecord> findByRuleId(String ruleId);
    
    /**
     * Find records created by a specific user
     * 
     * @param createdBy The username of the creator
     * @return List of records created by the user
     */
    List<RuleCreationRecord> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    
    /**
     * Find records with specific deployment status
     * 
     * @param deploymentStatus The deployment status to filter by
     * @return List of records with matching deployment status
     */
    List<RuleCreationRecord> findByDeploymentStatusOrderByCreatedAtDesc(String deploymentStatus);
    
    /**
     * Find records created within a specific time period
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of records created within the period
     */
    @Query("SELECT r FROM RuleCreationRecord r WHERE r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<RuleCreationRecord> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                                     @Param("endDate") LocalDateTime endDate);
    
    /**
     * Count records by deployment status
     * 
     * @param deploymentStatus The deployment status
     * @return Count of records with the status
     */
    long countByDeploymentStatus(String deploymentStatus);
    
    /**
     * Find rules that failed validation
     * 
     * @return List of rules with failed validation
     */
    @Query("SELECT r FROM RuleCreationRecord r WHERE r.validationStatus = 'FAILED' ORDER BY r.createdAt DESC")
    List<RuleCreationRecord> findFailedValidations();
}