package com.example.pricerulesaidrools.ai.dto;

import com.example.pricerulesaidrools.model.Customer;
import com.example.pricerulesaidrools.model.Deal;
import com.example.pricerulesaidrools.model.PricingRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enhanced pricing request that includes Deal and Customer information
 * for AI routing decisions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnhancedPricingRequest {

    /**
     * Original pricing request.
     */
    private PricingRequest pricingRequest;

    /**
     * Deal information for routing evaluation.
     */
    private Deal deal;

    /**
     * Customer information for risk assessment.
     */
    private Customer customer;

    /**
     * Request ID for tracking.
     */
    private String requestId;

    /**
     * Creates an enhanced request from basic pricing request.
     */
    public static EnhancedPricingRequest from(PricingRequest request, Deal deal, Customer customer) {
        return EnhancedPricingRequest.builder()
                .pricingRequest(request)
                .deal(deal)
                .customer(customer)
                .requestId(java.util.UUID.randomUUID().toString())
                .build();
    }
}