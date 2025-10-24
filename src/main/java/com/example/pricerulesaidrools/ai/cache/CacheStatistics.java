package com.example.pricerulesaidrools.ai.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for semantic cache statistics.
 * Tracks cache performance metrics including hits, misses, and hit rate.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatistics {

    /**
     * Total number of cache hits (queries found in cache above similarity
     * threshold)
     */
    private long cacheHits;

    /**
     * Total number of cache misses (queries not found in cache)
     */
    private long cacheMisses;

    /**
     * Total number of queries processed
     */
    private long totalQueries;

    /**
     * Cache hit rate percentage (hits / total queries * 100)
     */
    private double hitRate;

    /**
     * Current number of entries in the cache
     */
    private long cacheSize;

    /**
     * Maximum allowed cache size
     */
    private long maxCacheSize;

    /**
     * Average similarity score for cache hits
     */
    private double averageSimilarity;

    /**
     * Time when statistics were last reset
     */
    private LocalDateTime lastResetTime;

    /**
     * Time when cache was last accessed
     */
    private LocalDateTime lastAccessTime;

    /**
     * Time when statistics were generated
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Calculate and update the hit rate based on current hits and total queries.
     */
    public void calculateHitRate() {
        this.totalQueries = this.cacheHits + this.cacheMisses;
        this.hitRate = this.totalQueries > 0
                ? (double) this.cacheHits / this.totalQueries * 100.0
                : 0.0;
    }

    /**
     * Increment cache hits and recalculate hit rate.
     */
    public void incrementHits() {
        this.cacheHits++;
        calculateHitRate();
    }

    /**
     * Increment cache misses and recalculate hit rate.
     */
    public void incrementMisses() {
        this.cacheMisses++;
        calculateHitRate();
    }

    /**
     * Reset all statistics to zero.
     */
    public void reset() {
        this.cacheHits = 0;
        this.cacheMisses = 0;
        this.totalQueries = 0;
        this.hitRate = 0.0;
        this.averageSimilarity = 0.0;
        this.lastResetTime = LocalDateTime.now();
    }

    /**
     * Update last access time to current time.
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }

    /**
     * Get the last access time.
     * 
     * @return LocalDateTime of last access
     */
    public LocalDateTime getLastAccessTime() {
        return this.lastAccessTime;
    }
}
