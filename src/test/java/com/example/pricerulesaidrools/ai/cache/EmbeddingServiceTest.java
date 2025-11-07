package com.example.pricerulesaidrools.ai.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * Unit tests for EmbeddingService.
 * Tests embedding generation and cosine similarity calculations.
 */
@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    private EmbeddingService embeddingService;

    private static final String SAMPLE_TEXT = "What is the pricing for enterprise deals?";
    private static final float[] SAMPLE_EMBEDDING = { 0.1f, 0.2f, 0.3f, 0.4f, 0.5f };

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(embeddingModel);
    }

    @Test
    void testGenerateEmbedding_Success() {
        // Arrange
        when(embeddingModel.embed(anyList())).thenReturn(List.of(SAMPLE_EMBEDDING));

        // Act
        List<Double> result = embeddingService.generateEmbedding(SAMPLE_TEXT);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(SAMPLE_EMBEDDING.length);
        assertThat(result.get(0)).isEqualTo(0.1, within(0.001));
        assertThat(result.get(4)).isEqualTo(0.5, within(0.001));
    }

    @ParameterizedTest
    @DisplayName("Should generate embeddings for various input texts")
    @ValueSource(strings = {
            "Simple query",
            "What is the pricing for enterprise deals?",
            "A very long query with many words to test how embeddings handle longer text inputs that might come from complex user interactions and detailed business requirements",
            "Special chars !@#$%^&*()",
            "Numbers 12345 6789"
    })
    void testGenerateEmbeddingWithVariousTexts(String inputText) {
        // Arrange
        when(embeddingModel.embed(anyList())).thenReturn(List.of(SAMPLE_EMBEDDING));

        // Act
        List<Double> result = embeddingService.generateEmbedding(inputText);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(v -> v >= -1.0 && v <= 1.0, "Embeddings should be in normalized range");
    }

    @Test
    @DisplayName("Should generate embeddings with proper dimensions")
    void testGenerateEmbeddingDimensions() {
        // Arrange
        float[] largeDimensionEmbedding = new float[768]; // Common embedding dimension
        for (int i = 0; i < largeDimensionEmbedding.length; i++) {
            largeDimensionEmbedding[i] = (float) Math.random();
        }
        when(embeddingModel.embed(anyList())).thenReturn(List.of(largeDimensionEmbedding));

        // Act
        List<Double> result = embeddingService.generateEmbedding(SAMPLE_TEXT);

        // Assert
        assertThat(result).hasSize(768);
    }

    @Test
    void testGenerateEmbedding_EmptyResponse_ThrowsException() {
        // Arrange
        when(embeddingModel.embed(anyList())).thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> embeddingService.generateEmbedding(SAMPLE_TEXT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate embedding");
    }

    @Test
    void testGenerateEmbedding_NullResponse_ThrowsException() {
        // Arrange
        when(embeddingModel.embed(anyList())).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> embeddingService.generateEmbedding(SAMPLE_TEXT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to generate embedding");
    }

    @Test
    @DisplayName("Should handle null or empty input text")
    void testGenerateEmbeddingWithNullOrEmptyText() {
        // Test null
        assertThatThrownBy(() -> embeddingService.generateEmbedding(null))
                .isInstanceOf(IllegalArgumentException.class);

        // Test empty
        assertThatThrownBy(() -> embeddingService.generateEmbedding(""))
                .isInstanceOf(IllegalArgumentException.class);
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

    @ParameterizedTest
    @DisplayName("Should correctly calculate similarity with various vector pairs")
    @CsvSource({
            "1.0|1.0|1.0", // Identical unit vectors
            "0.5|0.5|1.0", // Scaled identical vectors
            "-1.0|-1.0|-1.0", // Negative identical vectors
            "0.707|0.707|0.707" // Normalized vectors (√2/2)
    })
    void testCalculateCosineSimilarityParametrized(double val1, double val2, double val3) {
        // Arrange
        List<Double> vector1 = List.of(val1, val2, val3);
        List<Double> vector2 = List.of(val1, val2, val3);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Assert - Identical vectors should yield 1.0
        assertThat(similarity).isCloseTo(1.0, within(0.001));
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
    @DisplayName("Should handle null vectors appropriately")
    void testCalculateCosineSimilarityNullVectors() {
        // Arrange
        List<Double> vector1 = List.of(1.0, 2.0);

        // Act & Assert
        assertThatThrownBy(() -> embeddingService.calculateCosineSimilarity(null, vector1))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> embeddingService.calculateCosineSimilarity(vector1, null))
                .isInstanceOf(IllegalArgumentException.class);
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
    @DisplayName("Should handle very small floating point values")
    void testCalculateCosineSimilaritySmallValues() {
        // Arrange
        List<Double> vector1 = List.of(1e-10, 2e-10, 3e-10);
        List<Double> vector2 = List.of(1e-10, 2e-10, 3e-10);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(vector1, vector2);

        // Assert - Should still be 1.0 regardless of scale
        assertThat(similarity).isCloseTo(1.0, within(0.0001));
    }

    @Test
    void testCalculateCosineSimilarity_RealWorldScenario() {
        // Test with realistic embedding-like vectors
        // Simulating similar semantic queries

        List<Double> embedding1 = List.of(
                0.234, 0.567, -0.123, 0.789, -0.345,
                0.901, -0.456, 0.234, 0.678, -0.890);

        List<Double> embedding2 = List.of(
                0.240, 0.570, -0.120, 0.790, -0.340,
                0.900, -0.450, 0.230, 0.680, -0.885);

        // Act
        double similarity = embeddingService.calculateCosineSimilarity(embedding1, embedding2);

        // Assert - Very similar embeddings should have high similarity
        assertThat(similarity).isGreaterThan(0.99);
    }

    @Test
    @DisplayName("Should maintain range of [-1, 1] for cosine similarity")
    void testCosineSimilarityRange() {
        // Test multiple vector combinations
        List<Double> v1 = List.of(0.234, 0.567, -0.123);
        List<Double> v2 = List.of(0.789, -0.345, 0.901);
        List<Double> v3 = List.of(-0.456, 0.234, 0.678);

        double sim1 = embeddingService.calculateCosineSimilarity(v1, v2);
        double sim2 = embeddingService.calculateCosineSimilarity(v2, v3);
        double sim3 = embeddingService.calculateCosineSimilarity(v1, v3);

        assertThat(sim1).isBetween(-1.0, 1.0);
        assertThat(sim2).isBetween(-1.0, 1.0);
        assertThat(sim3).isBetween(-1.0, 1.0);
    }

    private static org.assertj.core.data.Offset<Double> within(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
