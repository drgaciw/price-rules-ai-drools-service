package com.example.pricerulesaidrools.ai.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

/**
 * Configuration class for Redis VectorStore used in semantic caching.
 * Provides beans for vector storage, embedding generation, and Redis
 * connectivity.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "spring.redis.vector-store", name = "enabled", havingValue = "true", matchIfMissing = false)
public class VectorStoreConfiguration {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.vector-store.index-name:semantic-cache-index}")
    private String indexName;

    @Value("${spring.redis.vector-store.prefix:semantic:cache:}")
    private String prefix;

    @Value("${ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${ai.openai.embedding-model:text-embedding-ada-002}")
    private String embeddingModel;

    /**
     * Creates a Redis connection factory with connection pooling.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        log.info("Configuring Redis connection factory for host: {}, port: {}", redisHost, redisPort);

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        LettucePoolingClientConfiguration poolConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(60))
                .poolConfig(new org.apache.commons.pool2.impl.GenericObjectPoolConfig<>())
                .build();

        return new LettuceConnectionFactory(redisConfig, poolConfig);
    }

    /**
     * Creates a RedisTemplate for general Redis operations.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Creates OpenAI API client for embedding generation.
     */
    @Bean
    public OpenAiApi openAiApi() {
        log.info("Configuring OpenAI API with embedding model: {}", embeddingModel);
        return OpenAiApi.builder()
                .apiKey(openAiApiKey)
                .build();
    }

    /**
     * Creates OpenAI Embedding Model for generating text embeddings.
     */
    @Bean
    public EmbeddingModel embeddingClient(OpenAiApi openAiApi) {
        log.info("Creating OpenAI Embedding Model");
        return new OpenAiEmbeddingModel(openAiApi);
    }

    /**
     * Creates JedisPooled client for Redis operations.
     */
    @Bean
    public JedisPooled jedisPooled() {
        log.info("Configuring JedisPooled for host: {}, port: {}", redisHost, redisPort);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            return new JedisPooled(redisHost, redisPort, null, redisPassword);
        } else {
            return new JedisPooled(redisHost, redisPort);
        }
    }

    /**
     * Creates Redis VectorStore for semantic similarity search.
     */
    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingClient) {
        log.info("Creating Redis VectorStore with index: {}, prefix: {}", indexName, prefix);

        // In Spring AI M6, RedisVectorStore uses JedisPooled
        return RedisVectorStore.builder(jedisPooled, embeddingClient)
                .indexName(indexName)
                .prefix(prefix)
                .initializeSchema(true)
                .build();
    }
}
