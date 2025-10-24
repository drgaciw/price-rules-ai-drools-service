package com.example.pricerulesaidrools.ai.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration for AI routing strategies.
 * Binds to application.yml properties under spring.ai.routing.
 */
@Configuration
@ConfigurationProperties(prefix = "spring.ai.routing")
@Getter
@Setter
@Validated
@Slf4j
public class RoutingConfiguration {

    /**
     * List of routing strategy configurations.
     */
    private List<RoutingStrategy> strategies = new ArrayList<>();

    /**
     * The default fallback route when confidence is below threshold.
     */
    @NotBlank(message = "Fallback route must be specified")
    private String fallbackRoute = "general-review";

    /**
     * Minimum confidence threshold for routing decisions (0.0 to 1.0).
     * If confidence is below this threshold, fallback route is used.
     */
    @DecimalMin(value = "0.0", message = "Confidence threshold must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Confidence threshold must be at most 1.0")
    private double confidenceThreshold = 0.7;

    /**
     * Enable detailed logging of routing decisions.
     */
    private boolean enableDetailedLogging = false;

    /**
     * Enable caching of routing decisions for similar requests.
     */
    private boolean enableCaching = true;

    /**
     * Cache TTL in seconds for routing decisions.
     */
    @Min(value = 1, message = "Cache TTL must be at least 1 second")
    @Max(value = 3600, message = "Cache TTL must be at most 3600 seconds")
    private int cacheTtlSeconds = 300;

    /**
     * Maximum number of cached routing decisions.
     */
    @Min(value = 1, message = "Max cache size must be at least 1")
    @Max(value = 10000, message = "Max cache size must be at most 10000")
    private int maxCacheSize = 1000;

    /**
     * Enable metrics collection for routing decisions.
     */
    private boolean enableMetrics = true;

    /**
     * Custom routing rules that can be evaluated dynamically.
     */
    private Map<String, String> customRules;

    /**
     * Individual routing strategy configuration.
     */
    @Getter
    @Setter
    public static class RoutingStrategy {

        /**
         * Name of the routing strategy (e.g., "billing-review").
         */
        @NotBlank(message = "Strategy name must be specified")
        private String name;

        /**
         * Condition expression for this strategy.
         */
        @NotBlank(message = "Strategy condition must be specified")
        private String condition;

        /**
         * Description of when this strategy applies.
         */
        private String description;

        /**
         * Priority of this strategy (higher values are evaluated first).
         */
        @Min(value = 0, message = "Priority must be non-negative")
        private int priority = 0;

        /**
         * Whether this strategy is enabled.
         */
        private boolean enabled = true;

        /**
         * Minimum confidence required for this strategy.
         */
        @DecimalMin(value = "0.0", message = "Min confidence must be at least 0.0")
        @DecimalMax(value = "1.0", message = "Min confidence must be at most 1.0")
        private double minConfidence = 0.5;

        /**
         * Additional metadata for this strategy.
         */
        private Map<String, String> metadata;
    }

    /**
     * Get list of available route names.
     */
    public List<String> getAvailableRoutes() {
        List<String> routes = new ArrayList<>();

        // Add configured strategies
        for (RoutingStrategy strategy : strategies) {
            if (strategy.isEnabled()) {
                routes.add(strategy.getName());
            }
        }

        // Always include fallback route
        if (!routes.contains(fallbackRoute)) {
            routes.add(fallbackRoute);
        }

        return routes;
    }

    /**
     * Get a specific routing strategy by name.
     */
    public RoutingStrategy getStrategy(String name) {
        return strategies.stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if a specific route is enabled.
     */
    public boolean isRouteEnabled(String routeName) {
        RoutingStrategy strategy = getStrategy(routeName);
        return strategy != null && strategy.isEnabled();
    }

    /**
     * Initialize and validate configuration after loading.
     */
    @PostConstruct
    public void init() {
        log.info("Initializing AI Routing Configuration");
        log.info("Configured {} routing strategies", strategies.size());
        log.info("Fallback route: {}", fallbackRoute);
        log.info("Confidence threshold: {}", confidenceThreshold);
        log.info("Caching enabled: {}", enableCaching);

        if (strategies.isEmpty()) {
            log.warn("No routing strategies configured. Using default strategies.");
            initializeDefaultStrategies();
        }

        // Sort strategies by priority
        strategies.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));

        // Log configured strategies
        for (RoutingStrategy strategy : strategies) {
            log.info("Strategy '{}': condition='{}', priority={}, enabled={}",
                    strategy.getName(), strategy.getCondition(),
                    strategy.getPriority(), strategy.isEnabled());
        }
    }

    /**
     * Initialize default routing strategies if none are configured.
     */
    private void initializeDefaultStrategies() {
        // Billing review strategy
        RoutingStrategy billingStrategy = new RoutingStrategy();
        billingStrategy.setName("billing-review");
        billingStrategy.setCondition("deal.type == 'ENTERPRISE' && deal.value > 100000");
        billingStrategy.setDescription("Route enterprise deals above $100,000 to billing review");
        billingStrategy.setPriority(10);
        billingStrategy.setEnabled(true);
        billingStrategy.setMinConfidence(0.7);
        strategies.add(billingStrategy);

        // Technical review strategy
        RoutingStrategy technicalStrategy = new RoutingStrategy();
        technicalStrategy.setName("technical-review");
        technicalStrategy.setCondition("deal.complexity == 'HIGH'");
        technicalStrategy.setDescription("Route high-complexity deals to technical review");
        technicalStrategy.setPriority(8);
        technicalStrategy.setEnabled(true);
        technicalStrategy.setMinConfidence(0.6);
        strategies.add(technicalStrategy);

        // Risk review strategy
        RoutingStrategy riskStrategy = new RoutingStrategy();
        riskStrategy.setName("risk-review");
        riskStrategy.setCondition("customer.riskScore > 70");
        riskStrategy.setDescription("Route high-risk customers to risk review");
        riskStrategy.setPriority(9);
        riskStrategy.setEnabled(true);
        riskStrategy.setMinConfidence(0.7);
        strategies.add(riskStrategy);

        log.info("Initialized {} default routing strategies", strategies.size());
    }

    /**
     * Validates that the configuration is consistent.
     */
    public void validate() {
        if (!getAvailableRoutes().contains(fallbackRoute)) {
            log.warn("Fallback route '{}' is not in available routes", fallbackRoute);
        }

        if (confidenceThreshold < 0.0 || confidenceThreshold > 1.0) {
            throw new IllegalArgumentException("Confidence threshold must be between 0.0 and 1.0");
        }

        for (RoutingStrategy strategy : strategies) {
            if (strategy.getName() == null || strategy.getName().isEmpty()) {
                throw new IllegalArgumentException("Strategy name cannot be null or empty");
            }
            if (strategy.getCondition() == null || strategy.getCondition().isEmpty()) {
                throw new IllegalArgumentException("Strategy condition cannot be null or empty");
            }
        }
    }
}