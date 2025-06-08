package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialMetricsRepository extends JpaRepository<FinancialMetrics, Long> {
    
    /**
     * Find the most recent financial metrics for a customer
     * 
     * @param customerId The customer ID
     * @return The financial metrics, if found
     */
    Optional<FinancialMetrics> findByCustomerId(String customerId);
    
    /**
     * Find all financial metrics for a customer ordered by creation date
     * 
     * @param customerId The customer ID
     * @return List of financial metrics
     */
    List<FinancialMetrics> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    /**
     * Find financial metrics for a customer created after a specific date
     * 
     * @param customerId The customer ID
     * @param cutoffDate The cutoff date
     * @return List of financial metrics created after the cutoff date
     */
    List<FinancialMetrics> findByCustomerIdAndCreatedAtAfterOrderByCreatedAtAsc(
            String customerId, LocalDateTime cutoffDate);
    
    /**
     * Find financial metrics for a customer within a date range
     * 
     * @param customerId The customer ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of financial metrics within the date range
     */
    @Query("SELECT fm FROM FinancialMetrics fm WHERE fm.customerId = :customerId " +
           "AND fm.createdAt >= :startDate AND fm.createdAt <= :endDate " +
           "ORDER BY fm.createdAt ASC")
    List<FinancialMetrics> findByCustomerIdAndDateRange(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}