package com.example.pricerulesaidrools.ai.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of semantic caching using Redis VectorStore.
 * Uses embedding-based similarity search to find cached responses for similar
 * queries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.redis.vector-store", name = "enabled", havingValue = "true", matchIfMissing = false)
public class SemanticCacheServiceImpl implements SemanticCacheService {

    private final VectorStore vectorStore;

    @Value("${spring.redis.vector-store.similarity-threshold:0.85}")
    private double similarityThreshold;

    @Value("${spring.redis.vector-store.max-cache-size:10000}")
    private long maxCacheSize;

    @Value("${spring.redis.vector-store.ttl:3600}")
    private long ttlSeconds;

    @Value("${spring.redis.vector-store.enabled:false}")
    private boolean cacheEnabled;

    // Cache statistics
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);
    private final AtomicLong totalSimilarityScore = new AtomicLong(0);
    private LocalDateTime lastResetTime = LocalDateTime.now();
    private LocalDateTime lastAccessTime = null; // Initialize to null, will be set when cache is accessed

    private static final String METADATA_RESPONSE_KEY = "cached_response";
    private static final String METADATA_QUERY_KEY = "original_query";
    private static final String METADATA_TIMESTAMP_KEY = "timestamp";
    private static final String METADATA_SIMILARITY_KEY = "similarity_score";

    @Override
    public Optional<String> getCachedResponse(String query) {
        // Update last access time
        lastAccessTime = LocalDateTime.now();

        if (!cacheEnabled) {
            log.debug("Cache is disabled, skipping cache lookup");
            cacheMisses.incrementAndGet();
            return Optional.empty();
        }

        if (query == null || query.trim().isEmpty()) {
            log.warn("Empty or null query provided for cache lookup");
            cacheMisses.incrementAndGet();
            return Optional.empty();
        }

        try {
            log.debug("Searching cache for query: {}", query);

            // Search for similar documents in vector store
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(query)
                    .topK(1)
                    .similarityThreshold(similarityThreshold)
                    .build();

            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);

            if (similarDocuments == null || similarDocuments.isEmpty()) {
                log.debug("No similar documents found in cache for query: {}", query);
                cacheMisses.incrementAndGet();
                return Optional.empty();
            }

            Document mostSimilar = similarDocuments.get(0);
            Map<String, Object> metadata = mostSimilar.getMetadata();

            // Extract cached response
            String cachedResponse = (String) metadata.get(METADATA_RESPONSE_KEY);
            Double similarityScore = (Double) metadata.get(METADATA_SIMILARITY_KEY);

            if (cachedResponse != null) {
                log.info("Cache HIT - Found similar query with similarity: {}. Original query: {}",
                        similarityScore, metadata.get(METADATA_QUERY_KEY));
                cacheHits.incrementAndGet();
                if (similarityScore != null) {
                    totalSimilarityScore.addAndGet(Math.round(similarityScore * 1000));
                }
                return Optional.of(cachedResponse);
            }

            log.debug("Document found but no cached response in metadata");
            cacheMisses.incrementAndGet();
            return Optional.empty();

        } catch (Exception e) {
            log.error("Error searching cache for query: {}", query, e);
            cacheMisses.incrementAndGet();
            return Optional.empty();
        }
    }

    @Override
    public void cacheResponse(String query, String response) {
        // Update last access time
        lastAccessTime = LocalDateTime.now();

        if (!cacheEnabled) {
            log.debug("Cache is disabled, skipping cache storage");
            return;
        }

        if (query == null || query.trim().isEmpty() || response == null || response.trim().isEmpty()) {
            log.warn("Empty query or response provided, skipping cache storage");
            return;
        }

        try {
            // Check cache size limit
            long currentSize = getCacheSize();
            if (currentSize >= maxCacheSize) {
                log.warn("Cache size limit reached ({}/{}), not caching new entry", currentSize, maxCacheSize);
                return;
            }

            log.debug("Caching response for query: {}", query);

            // Create metadata for the cached entry
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(METADATA_RESPONSE_KEY, response);
            metadata.put(METADATA_QUERY_KEY, query);
            metadata.put(METADATA_TIMESTAMP_KEY, LocalDateTime.now().toString());
            metadata.put("ttl", ttlSeconds);

            // Create document and store in vector store
            Document document = new Document(query, metadata);
            vectorStore.add(List.of(document));

            log.info("Successfully cached response for query: {}", query.substring(0, Math.min(query.length(), 100)));

        } catch (Exception e) {
            log.error("Error caching response for query: {}", query, e);
        }
    }

    @Override
    public void clearCache() {
        try {
            log.info("Clearing semantic cache");
            vectorStore.delete(List.of()); // Clear all documents
            resetStatistics();
            lastAccessTime = LocalDateTime.now(); // Update last access time
            log.info("Semantic cache cleared successfully");
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }

    @Override
    public CacheStatistics getStatistics() {
        long hits = cacheHits.get();
        long misses = cacheMisses.get();
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100.0 : 0.0;
        double avgSimilarity = hits > 0 ? (double) totalSimilarityScore.get() / (hits * 1000.0) : 0.0;

        return CacheStatistics.builder()
                .cacheHits(hits)
                .cacheMisses(misses)
                .totalQueries(total)
                .hitRate(hitRate)
                .cacheSize(getCacheSize())
                .maxCacheSize(maxCacheSize)
                .averageSimilarity(avgSimilarity)
                .lastResetTime(lastResetTime)
                .lastAccessTime(lastAccessTime)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public void resetStatistics() {
        cacheHits.set(0);
        cacheMisses.set(0);
        totalSimilarityScore.set(0);
        lastResetTime = LocalDateTime.now();
        lastAccessTime = null; // Reset last access time as well
        log.info("Cache statistics reset");
    }

    @Override
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    @Override
    public long getCacheSize() {
        try {
            // Since VectorStore doesn't provide a direct size method,
            // we perform a broad search to estimate size
            SearchRequest request = SearchRequest.builder()
                    .query("")
                    .topK(Integer.MAX_VALUE)
                    .similarityThreshold(0.0)
                    .build();
            List<Document> allDocs = vectorStore.similaritySearch(request);
            return allDocs != null ? allDocs.size() : 0;
        } catch (Exception e) {
            log.error("Error getting cache size", e);
            return 0;
        }
    }

    /**
     * Pre-populate cache with common deal archetypes and pricing scenarios.
     */
    public void prePopulateCache() {
        if (!cacheEnabled) {
            log.info("Cache is disabled, skipping pre-population");
            return;
        }

        log.info("Pre-populating semantic cache with common queries");

        // Common enterprise deal queries
        Map<String, String> commonQueries = Map.of(
                "What is the pricing for an enterprise deal worth $500,000 with 3-year contract?",
                "For an enterprise deal of $500,000 over 3 years, typical pricing includes volume discounts (15-20% for contracts above $100K), "
                        +
                        "multi-year commitment discounts (5-10% for 3-year terms), and enterprise support premium. Final pricing would be approximately "
                        +
                        "$425,000-$450,000 annually depending on specific terms and risk factors.",

                "Calculate ARR for a customer with monthly subscription of $10,000",
                "ARR (Annual Recurring Revenue) for a customer with $10,000 monthly subscription is $120,000 " +
                        "(calculated as $10,000 × 12 months). This represents the annualized value of recurring revenue.",

                "What discount should we offer for a 5-year commitment?",
                "For a 5-year commitment, standard industry practice suggests 12-15% discount. This longer commitment "
                        +
                        "reduces customer acquisition costs and provides revenue stability. Actual discount may vary based on "
                        +
                        "deal size, customer risk profile, and competitive factors.",

                "How do we calculate Customer Lifetime Value?",
                "Customer Lifetime Value (CLV) is calculated as: (Average Revenue Per Customer × Customer Lifespan) - "
                        +
                        "Customer Acquisition Cost. For SaaS businesses, this typically uses the formula: CLV = (Monthly Recurring Revenue × "
                        +
                        "Gross Margin %) / Monthly Churn Rate. This helps determine the long-term value of acquiring a customer.");

        commonQueries.forEach(this::cacheResponse);

        log.info("Pre-populated cache with {} common queries", commonQueries.size());
    }
}
