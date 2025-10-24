package com.example.pricerulesaidrools.ai.cache;

import java.util.Optional;

/**
 * Service interface for semantic caching of AI responses.
 * Uses vector similarity search to find cached responses for similar queries.
 */
public interface SemanticCacheService {

    /**
     * Retrieves a cached response for a query if a semantically similar query exists in cache.
     *
     * @param query The user query to search for
     * @return Optional containing the cached response if found above similarity threshold, empty otherwise
     */
    Optional<String> getCachedResponse(String query);

    /**
     * Caches a query-response pair for future semantic similarity searches.
     *
     * @param query The user query
     * @param response The AI response to cache
     */
    void cacheResponse(String query, String response);

    /**
     * Clears all entries from the semantic cache.
     */
    void clearCache();

    /**
     * Retrieves current cache performance statistics.
     *
     * @return CacheStatistics containing hit/miss metrics and other cache information
     */
    CacheStatistics getStatistics();

    /**
     * Resets cache statistics counters to zero.
     */
    void resetStatistics();

    /**
     * Checks if caching is enabled.
     *
     * @return true if caching is enabled, false otherwise
     */
    boolean isCacheEnabled();

    /**
     * Retrieves the current cache size.
     *
     * @return The number of entries in the cache
     */
    long getCacheSize();
}
