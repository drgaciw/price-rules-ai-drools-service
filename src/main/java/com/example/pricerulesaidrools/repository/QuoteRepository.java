package com.example.pricerulesaidrools.repository;

import com.example.pricerulesaidrools.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    
    Optional<Quote> findByQuoteId(String quoteId);
    
    List<Quote> findByCustomerId(String customerId);
    
    List<Quote> findByCustomerIdAndStatus(String customerId, Quote.QuoteStatus status);
}