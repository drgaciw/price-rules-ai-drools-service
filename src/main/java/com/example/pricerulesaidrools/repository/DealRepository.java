package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.Deal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Deal entities.
 */
@Repository
public interface DealRepository extends JpaRepository<Deal, Long> {

    /**
     * Find a deal by its unique deal ID.
     */
    Optional<Deal> findByDealId(String dealId);

    /**
     * Find all deals for a specific customer.
     */
    List<Deal> findByCustomerId(String customerId);

    /**
     * Find deals by status.
     */
    List<Deal> findByStatus(Deal.DealStatus status);

    /**
     * Find deals by type.
     */
    List<Deal> findByType(Deal.DealType type);

    /**
     * Find deals above a certain value.
     */
    @Query("SELECT d FROM Deal d WHERE d.value > :minValue")
    List<Deal> findDealsAboveValue(@Param("minValue") BigDecimal minValue);

    /**
     * Find high-complexity deals.
     */
    @Query("SELECT d FROM Deal d WHERE d.complexity IN ('HIGH', 'VERY_HIGH')")
    List<Deal> findHighComplexityDeals();

    /**
     * Find deals with high risk scores.
     */
    @Query("SELECT d FROM Deal d WHERE d.riskScore > :threshold")
    List<Deal> findHighRiskDeals(@Param("threshold") Double threshold);

    /**
     * Find enterprise deals above a certain value threshold.
     */
    @Query("SELECT d FROM Deal d WHERE d.type = 'ENTERPRISE' AND d.value > :valueThreshold")
    List<Deal> findEnterpriseDealsAboveThreshold(@Param("valueThreshold") BigDecimal valueThreshold);
}