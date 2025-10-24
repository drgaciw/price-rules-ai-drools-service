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
 * Configuration for Context7 API integration.
 * This class manages the connection settings and client configuration
 * for interacting with the Context7 documentation service.
 */
@Configuration
@ConfigurationProperties(prefix = "ai.context7")
@Data
@Getter
@Setter
public class Context7Config {

    /**
     * Base URL for the Context7 API.
     */
    private String apiUrl;
    
    /**
     * API key for authentication with the Context7 service.
     */
    private String apiKey;
    
    /**
     * Connection timeout in milliseconds.
     */
    private int connectionTimeout = 5000;
    
    /**
     * Read timeout in milliseconds.
     */
    private int readTimeout = 15000;
    
    /**
     * Default maximum number of tokens to retrieve.
     */
    private int defaultTokenLimit = 5000;
    
    /**
     * Default library name for Drools documentation.
     */
    private String defaultLibraryName = "drools";
    
    /**
     * Create a RestTemplate bean configured for Context7 API.
     *
     * @return RestTemplate configured with appropriate timeouts
     */
    @Bean(name = "context7RestTemplate")
    public RestTemplate context7RestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        
        return new RestTemplate(factory);
    }
}