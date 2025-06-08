package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.FinancialMetricsSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for accessing financial metrics snapshots
 */
@Repository
public interface FinancialMetricsSnapshotRepository extends JpaRepository<FinancialMetricsSnapshot, Long> {
    
    /**
     * Find snapshots for a customer
     * 
     * @param customerId The customer ID
     * @return List of snapshots
     */
    List<FinancialMetricsSnapshot> findByCustomerIdOrderBySnapshotDateDesc(String customerId);
    
    /**
     * Find snapshots for a customer by snapshot type
     * 
     * @param customerId The customer ID
     * @param snapshotType The snapshot type
     * @return List of snapshots
     */
    List<FinancialMetricsSnapshot> findByCustomerIdAndSnapshotTypeOrderBySnapshotDateDesc(
            String customerId, FinancialMetricsSnapshot.SnapshotType snapshotType);
    
    /**
     * Find snapshots for a customer by quarter
     * 
     * @param customerId The customer ID
     * @param quarter The quarter (e.g., "2025-Q1")
     * @return List of snapshots
     */
    List<FinancialMetricsSnapshot> findByCustomerIdAndQuarterOrderBySnapshotDateAsc(
            String customerId, String quarter);
    
    /**
     * Find snapshots for a customer by month
     * 
     * @param customerId The customer ID
     * @param month The month (e.g., "2025-01")
     * @return List of snapshots
     */
    List<FinancialMetricsSnapshot> findByCustomerIdAndMonthOrderBySnapshotDateAsc(
            String customerId, String month);
    
    /**
     * Find snapshots for a customer within a date range
     * 
     * @param customerId The customer ID
     * @param startDate The start date
     * @param endDate The end date
     * @return List of snapshots within the date range
     */
    @Query("SELECT s FROM FinancialMetricsSnapshot s WHERE s.customerId = :customerId " +
           "AND s.snapshotDate >= :startDate AND s.snapshotDate <= :endDate " +
           "ORDER BY s.snapshotDate ASC")
    List<FinancialMetricsSnapshot> findByCustomerIdAndDateRange(
            @Param("customerId") String customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find most recent snapshot for a customer
     * 
     * @param customerId The customer ID
     * @return List containing the most recent snapshot
     */
    @Query("SELECT s FROM FinancialMetricsSnapshot s WHERE s.customerId = :customerId " +
           "ORDER BY s.snapshotDate DESC LIMIT 1")
    List<FinancialMetricsSnapshot> findMostRecentByCustomerId(@Param("customerId") String customerId);
    
    /**
     * Find snapshots with increasing churn risk
     * 
     * @return List of snapshots where churn risk is increasing
     */
    @Query(value = "WITH RankedSnapshots AS ( " +
                  "SELECT s.*, " +
                  "LAG(s.churn_risk_score) OVER (PARTITION BY s.customer_id ORDER BY s.snapshot_date) AS prev_risk " +
                  "FROM financial_metrics_snapshots s " +
                  ") " +
                  "SELECT * FROM RankedSnapshots " +
                  "WHERE churn_risk_score > prev_risk AND prev_risk IS NOT NULL " +
                  "ORDER BY snapshot_date DESC", 
           nativeQuery = true)
    List<FinancialMetricsSnapshot> findWithIncreasingChurnRisk();
}