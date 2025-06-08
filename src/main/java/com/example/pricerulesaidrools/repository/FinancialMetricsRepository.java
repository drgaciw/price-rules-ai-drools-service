package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.FinancialMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinancialMetricsRepository extends JpaRepository<FinancialMetrics, Long> {
    
    Optional<FinancialMetrics> findByCustomerId(String customerId);
}