package com.example.pricerulesaidrools.ai.service;

import com.example.pricerulesaidrools.ai.dto.EnhancedPricingRequest;
import com.example.pricerulesaidrools.ai.dto.RoutingDecision;
import com.example.pricerulesaidrools.model.PricingRequest;

import java.util.List;
import java.util.Map;

/**
 * Service interface for AI-orchestrated routing of deal evaluations.
 * Directs pricing requests to specialized review paths based on deal characteristics.
 */
public interface AIRoutingService {

    /**
     * Routes a pricing request to the appropriate specialized review path.
     *
     * @param request The pricing request to route
     * @return RoutingDecision containing the selected route and confidence
     */
    RoutingDecision route(PricingRequest request);

    /**
     * Routes an enhanced pricing request with full deal and customer context.
     *
     * @param request The enhanced pricing request with deal and customer information
     * @return RoutingDecision containing the selected route and confidence
     */
    RoutingDecision routeEnhanced(EnhancedPricingRequest request);

    /**
     * Evaluates all routing strategies and returns scores for each.
     * Useful for debugging and understanding routing decisions.
     *
     * @param request The enhanced pricing request to evaluate
     * @return Map of route names to confidence scores
     */
    Map<String, Double> evaluateAllStrategies(EnhancedPricingRequest request);

    /**
     * Gets the configured routing strategies.
     *
     * @return List of configured routing strategy names
     */
    List<String> getAvailableRoutes();

    /**
     * Gets the current confidence threshold for routing decisions.
     *
     * @return The confidence threshold (0.0 to 1.0)
     */
    double getConfidenceThreshold();

    /**
     * Sets the confidence threshold for routing decisions.
     *
     * @param threshold The new confidence threshold (0.0 to 1.0)
     */
    void setConfidenceThreshold(double threshold);

    /**
     * Gets the fallback route used when confidence is below threshold.
     *
     * @return The name of the fallback route
     */
    String getFallbackRoute();

    /**
     * Validates if a route name is valid and available.
     *
     * @param routeName The route name to validate
     * @return true if the route is valid, false otherwise
     */
    boolean isValidRoute(String routeName);

    /**
     * Gets routing statistics for monitoring and analysis.
     *
     * @return Map containing routing statistics
     */
    Map<String, Object> getRoutingStatistics();

    /**
     * Resets routing statistics.
     */
    void resetStatistics();
}