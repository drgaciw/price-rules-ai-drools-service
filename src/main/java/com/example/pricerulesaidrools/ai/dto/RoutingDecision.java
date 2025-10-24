package com.example.pricerulesaidrools.ai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO representing an AI routing decision for deal evaluation.
 * Contains the selected route, confidence score, reasoning, and metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingDecision {

    /**
     * The selected route for deal evaluation.
     * Possible values: "billing-review", "technical-review", "risk-review", "general-review"
     */
    private String route;

    /**
     * Confidence score for the routing decision (0.0 to 1.0).
     * If confidence < threshold, fallback route will be used.
     */
    private Double confidence;

    /**
     * Human-readable explanation of why this route was selected.
     */
    private String reason;

    /**
     * Timestamp when the routing decision was made.
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * The original request ID that triggered this routing decision.
     */
    private String requestId;

    /**
     * Customer ID associated with the deal.
     */
    private String customerId;

    /**
     * Deal ID associated with this routing decision.
     */
    private String dealId;

    /**
     * Deal value that influenced the routing decision.
     */
    private Double dealValue;

    /**
     * Deal type that influenced the routing decision.
     */
    private String dealType;

    /**
     * Deal complexity level.
     */
    private String complexity;

    /**
     * Customer risk score if applicable.
     */
    private Double customerRiskScore;

    /**
     * Indicates if this is a fallback decision due to low confidence.
     */
    private boolean isFallback;

    /**
     * Processing time in milliseconds.
     */
    private Long processingTimeMs;

    /**
     * Additional metadata that influenced the routing decision.
     */
    private Map<String, Object> metadata;

    /**
     * List of evaluated routing rules with their individual scores.
     */
    private Map<String, Double> ruleScores;

    /**
     * Creates a fallback routing decision when confidence is below threshold.
     */
    public static RoutingDecision createFallback(String requestId, String reason) {
        return RoutingDecision.builder()
                .route("general-review")
                .confidence(1.0)
                .reason("Fallback route selected: " + reason)
                .timestamp(LocalDateTime.now())
                .requestId(requestId)
                .isFallback(true)
                .build();
    }

    /**
     * Checks if the confidence meets the threshold.
     */
    public boolean isConfident(double threshold) {
        return confidence != null && confidence >= threshold;
    }
}