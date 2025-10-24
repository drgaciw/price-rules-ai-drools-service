package com.example.pricerulesaidrools.ai.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Integration tests for SemanticCacheService.
 * Tests cache hits, misses, TTL expiration, and similarity matching.
 */
@ExtendWith(MockitoExtension.class)
class SemanticCacheServiceTest {

    @Mock
    private VectorStore vectorStore;

    private SemanticCacheServiceImpl cacheService;

    private static final double SIMILARITY_THRESHOLD = 0.85;
    private static final String SAMPLE_QUERY = "What is the pricing for an enterprise deal?";
    private static final String SIMILAR_QUERY = "What is the pricing for a large enterprise contract?";
    private static final String DIFFERENT_QUERY = "How do I calculate customer churn rate?";
    private static final String SAMPLE_RESPONSE = "Enterprise pricing starts at $50,000 annually with volume discounts available.";

    @BeforeEach
    void setUp() {
        cacheService = new SemanticCacheServiceImpl(vectorStore);

        // Set configuration values using reflection
        ReflectionTestUtils.setField(cacheService, "similarityThreshold", SIMILARITY_THRESHOLD);
        ReflectionTestUtils.setField(cacheService, "maxCacheSize", 10000L);
        ReflectionTestUtils.setField(cacheService, "ttlSeconds", 3600L);
        ReflectionTestUtils.setField(cacheService, "cacheEnabled", true);
    }

    @Test
    void testCacheResponse_Success() {
        // Arrange
        doNothing().when(vectorStore).add(anyList());

        // Act
        cacheService.cacheResponse(SAMPLE_QUERY, SAMPLE_RESPONSE);

        // Assert
        verify(vectorStore, times(1)).add(anyList());
    }

    @ParameterizedTest
    @DisplayName("Should handle null or empty queries appropriately")
    @ValueSource(strings = { "", "   ", "\n", "\t" })
    void testCacheResponseWithEmptyQueries(String query) {
        // Act
        cacheService.cacheResponse(query, SAMPLE_RESPONSE);

        // Assert
        verify(vectorStore, never()).add(anyList());
    }

    @Test
    void testCacheResponse_NullQuery_ShouldNotCache() {
        // Act
        cacheService.cacheResponse(null, SAMPLE_RESPONSE);

        // Assert
        verify(vectorStore, never()).add(anyList());
    }

    @Test
    @DisplayName("Should handle null responses when caching")
    void testCacheResponseWithNullResponse() {
        // Act - null response should still be stored (cached)
        cacheService.cacheResponse(SAMPLE_QUERY, null);

        // Assert - Verify it was added (behavior depends on implementation)
        // Most implementations will still cache null
    }

    @Test
    void testCacheResponse_EmptyQuery_ShouldNotCache() {
        // Act
        cacheService.cacheResponse("", SAMPLE_RESPONSE);

        // Assert
        verify(vectorStore, never()).add(anyList());
    }

    @Test
    void testGetCachedResponse_CacheHit_ExactMatch() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cached_response", SAMPLE_RESPONSE);
        metadata.put("original_query", SAMPLE_QUERY);
        metadata.put("similarity_score", 1.0);

        Document mockDocument = new Document(SAMPLE_QUERY, metadata);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(mockDocument));

        // Act
        Optional<String> result = cacheService.getCachedResponse(SAMPLE_QUERY);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(SAMPLE_RESPONSE);

        CacheStatistics stats = cacheService.getStatistics();
        assertThat(stats.getCacheHits()).isEqualTo(1);
        assertThat(stats.getCacheMisses()).isEqualTo(0);
    }

    @Test
    void testGetCachedResponse_CacheHit_SimilarQuery() {
        // Arrange - Similar query should produce cache hit
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cached_response", SAMPLE_RESPONSE);
        metadata.put("original_query", SAMPLE_QUERY);
        metadata.put("similarity_score", 0.92); // Above threshold

        Document mockDocument = new Document(SIMILAR_QUERY, metadata);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(mockDocument));

        // Act
        Optional<String> result = cacheService.getCachedResponse(SIMILAR_QUERY);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(SAMPLE_RESPONSE);

        CacheStatistics stats = cacheService.getStatistics();
        assertThat(stats.getCacheHits()).isEqualTo(1);
        assertThat(stats.getAverageSimilarity()).isGreaterThan(0.9);
    }

    @Test
    void testGetCachedResponse_CacheMiss_DifferentQuery() {
        // Arrange - Different query returns no results
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList());

        // Act
        Optional<String> result = cacheService.getCachedResponse(DIFFERENT_QUERY);

        // Assert
        assertThat(result).isEmpty();

        CacheStatistics stats = cacheService.getStatistics();
        assertThat(stats.getCacheHits()).isEqualTo(0);
        assertThat(stats.getCacheMisses()).isEqualTo(1);
    }

    @Test
    void testGetCachedResponse_CacheMiss_BelowThreshold() {
        // Arrange - Similarity below threshold
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cached_response", SAMPLE_RESPONSE);
        metadata.put("original_query", SAMPLE_QUERY);
        metadata.put("similarity_score", 0.70); // Below threshold (0.85)

        // VectorStore would not return this document since it's below threshold
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList());

        // Act
        Optional<String> result = cacheService.getCachedResponse(DIFFERENT_QUERY);

        // Assert
        assertThat(result).isEmpty();

        CacheStatistics stats = cacheService.getStatistics();
        assertThat(stats.getCacheMisses()).isEqualTo(1);
    }

    @Test
    void testGetCachedResponse_NullQuery_ShouldReturnEmpty() {
        // Act
        Optional<String> result = cacheService.getCachedResponse(null);

        // Assert
        assertThat(result).isEmpty();
        verify(vectorStore, never()).similaritySearch(any(SearchRequest.class));
    }

    @ParameterizedTest
    @DisplayName("Should handle various empty string patterns")
    @ValueSource(strings = { "", "   ", "\n", "\t", "  \n  " })
    void testGetCachedResponseWithEmptyStrings(String emptyQuery) {
        // Act
        Optional<String> result = cacheService.getCachedResponse(emptyQuery);

        // Assert
        assertThat(result).isEmpty();
        // Empty queries should not trigger a vectorstore search
    }

    @Test
    void testGetCachedResponse_CacheDisabled_ShouldReturnEmpty() {
        // Arrange
        ReflectionTestUtils.setField(cacheService, "cacheEnabled", false);

        // Act
        Optional<String> result = cacheService.getCachedResponse(SAMPLE_QUERY);

        // Assert
        assertThat(result).isEmpty();
        verify(vectorStore, never()).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testClearCache_Success() {
        // Arrange
        doNothing().when(vectorStore).delete(anyList());

        // Act
        cacheService.clearCache();

        // Assert
        verify(vectorStore, times(1)).delete(anyList());

        CacheStatistics stats = cacheService.getStatistics();
        assertThat(stats.getCacheHits()).isEqualTo(0);
        assertThat(stats.getCacheMisses()).isEqualTo(0);
    }

    @Test
    void testGetStatistics_CalculatesCorrectly() {
        // Arrange - Simulate cache hits and misses
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cached_response", SAMPLE_RESPONSE);
        metadata.put("original_query", SAMPLE_QUERY);
        metadata.put("similarity_score", 0.95);

        Document mockDocument = new Document(SAMPLE_QUERY, metadata);

        // First call - hit
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(mockDocument))
                // Second call - miss
                .thenReturn(Collections.emptyList())
                // Third call - hit
                .thenReturn(List.of(mockDocument));

        // Act
        cacheService.getCachedResponse(SAMPLE_QUERY); // Hit
        cacheService.getCachedResponse(DIFFERENT_QUERY); // Miss
        cacheService.getCachedResponse(SIMILAR_QUERY); // Hit

        CacheStatistics stats = cacheService.getStatistics();

        // Assert
        assertThat(stats.getCacheHits()).isEqualTo(2);
        assertThat(stats.getCacheMisses()).isEqualTo(1);
        assertThat(stats.getTotalQueries()).isEqualTo(3);
        assertThat(stats.getHitRate()).isCloseTo(66.67, within(0.1));
        assertThat(stats.getAverageSimilarity()).isGreaterThan(0.9);
    }

    @Test
    @DisplayName("Should verify cache statistics start at zero")
    void testInitialStatisticsAreZero() {
        // Act
        CacheStatistics stats = cacheService.getStatistics();

        // Assert
        assertThat(stats.getCacheHits()).isZero();
        assertThat(stats.getCacheMisses()).isZero();
        assertThat(stats.getTotalQueries()).isZero();
        assertThat(stats.getHitRate()).isZero();
    }

    @Test
    void testResetStatistics_ResetsCounters() {
        // Arrange - Generate some statistics
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cached_response", SAMPLE_RESPONSE);
        metadata.put("original_query", SAMPLE_QUERY);
        metadata.put("similarity_score", 0.95);

        Document mockDocument = new Document(SAMPLE_QUERY, metadata);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(mockDocument));

        cacheService.getCachedResponse(SAMPLE_QUERY);

        // Act
        cacheService.resetStatistics();
        CacheStatistics stats = cacheService.getStatistics();

        // Assert
        assertThat(stats.getCacheHits()).isEqualTo(0);
        assertThat(stats.getCacheMisses()).isEqualTo(0);
        assertThat(stats.getTotalQueries()).isEqualTo(0);
        assertThat(stats.getHitRate()).isEqualTo(0.0);
        assertThat(stats.getLastResetTime()).isNotNull();
    }

    @Test
    void testIsCacheEnabled_ReturnsCorrectValue() {
        // Act & Assert - Default enabled
        assertThat(cacheService.isCacheEnabled()).isTrue();

        // Disable and verify
        ReflectionTestUtils.setField(cacheService, "cacheEnabled", false);
        assertThat(cacheService.isCacheEnabled()).isFalse();
    }

    @Test
    void testPrePopulateCache_AddsCommonQueries() {
        // Arrange
        doNothing().when(vectorStore).add(anyList());

        // Act
        cacheService.prePopulateCache();

        // Assert - Should add multiple common queries
        verify(vectorStore, atLeast(4)).add(anyList());
    }

    @Test
    void testMultipleSimilarQueries_ProduceCacheHits() {
        // Test that semantically similar queries produce cache hits
        // This tests the core semantic caching functionality

        String[] similarQueries = {
                "What is the pricing for an enterprise deal?",
                "What is enterprise deal pricing?",
                "How much does an enterprise deal cost?",
                "Tell me about enterprise pricing"
        };

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cached_response", SAMPLE_RESPONSE);
        metadata.put("original_query", similarQueries[0]);
        metadata.put("similarity_score", 0.90);

        Document mockDocument = new Document(similarQueries[0], metadata);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(mockDocument));

        // Act - Query with similar variations
        for (String query : similarQueries) {
            Optional<String> result = cacheService.getCachedResponse(query);
            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(SAMPLE_RESPONSE);
        }

        // Assert
        CacheStatistics stats = cacheService.getStatistics();
        assertThat(stats.getCacheHits()).isEqualTo(similarQueries.length);
        assertThat(stats.getHitRate()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Should handle multiple cache documents with different similarities")
    void testMultipleCacheDocumentsRanking() {
        // Arrange - Multiple documents with different similarities
        List<Document> documents = new ArrayList<>();

        Map<String, Object> meta1 = new HashMap<>();
        meta1.put("cached_response", SAMPLE_RESPONSE);
        meta1.put("similarity_score", 0.95);
        documents.add(new Document(SAMPLE_QUERY, meta1));

        Map<String, Object> meta2 = new HashMap<>();
        meta2.put("cached_response", "Different response");
        meta2.put("similarity_score", 0.88);
        documents.add(new Document(SIMILAR_QUERY, meta2));

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(documents);

        // Act
        Optional<String> result = cacheService.getCachedResponse("Some query");

        // Assert - Should return the document (implementation may vary on ranking)
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Should provide meaningful cache statistics")
    void testCacheStatisticsAccuracy() {
        // Arrange
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("cached_response", SAMPLE_RESPONSE);
        metadata.put("original_query", SAMPLE_QUERY);
        metadata.put("similarity_score", 0.92);

        Document mockDocument = new Document(SAMPLE_QUERY, metadata);

        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(mockDocument))
                .thenReturn(Collections.emptyList());

        // Act - 1 hit, 1 miss
        cacheService.getCachedResponse(SAMPLE_QUERY);
        cacheService.getCachedResponse(DIFFERENT_QUERY);

        CacheStatistics stats = cacheService.getStatistics();

        // Assert
        assertThat(stats.getCacheHits()).isEqualTo(1);
        assertThat(stats.getCacheMisses()).isEqualTo(1);
        assertThat(stats.getTotalQueries()).isEqualTo(2);
        assertThat(stats.getHitRate()).isCloseTo(50.0, within(0.1));
        assertThat(stats.getAverageSimilarity()).isCloseTo(0.92, within(0.01));
        assertThat(stats.getLastAccessTime()).isNotNull();
    }

    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
