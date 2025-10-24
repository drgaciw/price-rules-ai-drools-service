package com.example.pricerulesaidrools.ai.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EmbeddingService.
 * Tests embedding generation and cosine similarity calculations.
 */
@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingClient embeddingClient;

    private EmbeddingService embeddingService;

    private static final String SAMPLE_TEXT = "What is the pricing for enterprise deals?";
    private static final float[] SAMPLE_EMBEDDING = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(embeddingClient);
    }

    @Test
    void testGenerateEmbedding_Success() {
        // Arrange
        Embedding mockEmbedding = new Embedding(SAMPLE_EMBEDDING, 0);
        EmbeddingResponse mockResponse = new EmbeddingResponse(List.of(mockEmbedding));

        when(embeddingClient.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

        // Act
        List<Double> result = embeddingService.generateEmbedding(SAMPLE_TEXT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(SAMPLE_EMBEDDING.length);
        assertThat(result.get(0)).isEqualTo(0.1, within(0.001));
        assertThat(result.get(4)).isEqualTo(0.5, within(0.001));
    }

    @Test
    void testGenerateEmbedding_EmptyResponse_ThrowsException() {
        // Arrange
        EmbeddingResponse mockResponse = new EmbeddingResponse(List.of());
        when(embeddingClient.call(any(EmbeddingRequest.class))).thenReturn(mockResponse);

        // Act & Assert
        assertThatThrownBy(() -> embeddingService.generateEmbedding(SAMPLE_TEXT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate embedding");
    }

    @Test
    void testGenerateEmbedding_NullResponse_ThrowsException() {
        // Arrange
        when(embeddingClient.call(any(EmbeddingRequest.class))).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> embeddingService.generateEmbedding(SAMPLE_TEXT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate embedding");
    }

    @Test
    void testCalculateCosineSimilarity_IdenticalVectors() {
        // Arrange
        List<Double> vector1 = List.of(1.0, 2.0, 3.0, 4.0);
        List<Double> vector2 = List.of(1.0, 2.0, 3.0, 4.0);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Assert - Identical vectors should have similarity of 1.0
        assertThat(similarity).isCloseTo(1.0, within(0.0001));
    }

    @Test
    void testCalculateCosineSimilarity_OrthogonalVectors() {
        // Arrange - Orthogonal vectors (perpendicular)
        List<Double> vector1 = List.of(1.0, 0.0, 0.0);
        List<Double> vector2 = List.of(0.0, 1.0, 0.0);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Assert - Orthogonal vectors should have similarity of 0.0
        assertThat(similarity).isCloseTo(0.0, within(0.0001));
    }

    @Test
    void testCalculateCosineSimilarity_SimilarVectors() {
        // Arrange - Similar but not identical vectors
        List<Double> vector1 = List.of(1.0, 2.0, 3.0, 4.0);
        List<Double> vector2 = List.of(1.1, 2.1, 2.9, 4.0);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Assert - Similar vectors should have high similarity
        assertThat(similarity).isGreaterThan(0.95);
        assertThat(similarity).isLessThan(1.0);
    }

    @Test
    void testCalculateCosineSimilarity_OppositeVectors() {
        // Arrange - Opposite vectors
        List<Double> vector1 = List.of(1.0, 2.0, 3.0);
        List<Double> vector2 = List.of(-1.0, -2.0, -3.0);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Assert - Opposite vectors should have similarity of -1.0
        assertThat(similarity).isCloseTo(-1.0, within(0.0001));
    }

    @Test
    void testCalculateCosineSimilarity_DifferentDimensions_ThrowsException() {
        // Arrange
        List<Double> vector1 = List.of(1.0, 2.0, 3.0);
        List<Double> vector2 = List.of(1.0, 2.0);

        // Act & Assert
        assertThatThrownBy(() -> embeddingService.calculateCosineSimilarity(vector1, vector2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("same dimension");
    }

    @Test
    void testCalculateCosineSimilarity_ZeroVectors() {
        // Arrange - Zero vectors
        List<Double> vector1 = List.of(0.0, 0.0, 0.0);
        List<Double> vector2 = List.of(1.0, 2.0, 3.0);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Assert - Zero vector should result in 0.0 similarity
        assertThat(similarity).isEqualTo(0.0);
    }

    @Test
    void testCalculateCosineSimilarity_RealWorldScenario() {
        // Test with realistic embedding-like vectors
        // Simulating similar semantic queries

        List<Double> embedding1 = List.of(
            0.234, 0.567, -0.123, 0.789, -0.345,
            0.901, -0.456, 0.234, 0.678, -0.890
        );

        List<Double> embedding2 = List.of(
            0.240, 0.570, -0.120, 0.790, -0.340,
            0.900, -0.450, 0.230, 0.680, -0.885
        );

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2);

        // Assert - Very similar embeddings should have high similarity
        assertThat(similarity).isGreaterThan(0.99);
    }

    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
