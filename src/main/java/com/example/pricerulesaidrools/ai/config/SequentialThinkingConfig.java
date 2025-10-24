package com.example.pricerulesaidrools.ai.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Sequential Thinking API integration.
 * This class manages the connection settings and client configuration
 * for interacting with the Sequential Thinking service.
 */
@Configuration
@ConfigurationProperties(prefix = "ai.sequential-thinking")
@Data
@Getter
@Setter
public class SequentialThinkingConfig {

    /**
     * Base URL for the Sequential Thinking API.
     */
    private String apiUrl;
    
    /**
     * API key for authentication with the Sequential Thinking service.
     */
    private String apiKey;
    
    /**
     * Connection timeout in milliseconds.
     */
    private int connectionTimeout = 10000;
    
    /**
     * Read timeout in milliseconds.
     */
    private int readTimeout = 30000;
    
    /**
     * Maximum number of thoughts in a sequence.
     */
    private int maxThoughts = 8;
    
    /**
     * Default model to use for Sequential Thinking.
     */
    private String model = "claude-3-opus-20240229";
    
    /**
     * Create a RestTemplate bean configured for Sequential Thinking API.
     *
     * @return RestTemplate configured with appropriate timeouts
     */
    @Bean(name = "sequentialThinkingRestTemplate")
    public RestTemplate sequentialThinkingRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}