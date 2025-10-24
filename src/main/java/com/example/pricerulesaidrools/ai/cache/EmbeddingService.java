package com.example.pricerulesaidrools.ai.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for generating embeddings for text queries.
 * Embeddings are cached for performance optimization.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingModel embeddingClient;

    /**
     * Generates an embedding vector for the given text.
     * Results are cached to improve performance for repeated queries.
     *
     * @param text The text to generate embeddings for
     * @return A list of floating-point values representing the embedding vector
     */
    @Cacheable(value = "embeddings", key = "#text")
    public List<Double> generateEmbedding(String text) {
        log.debug("Generating embedding for text: {}", text.substring(0, Math.min(text.length(), 100)));

        try {
            List<float[]> embeddings = embeddingClient.embed(List.of(text));

            if (embeddings != null && !embeddings.isEmpty()) {
                float[] embedding = embeddings.get(0);
                List<Double> embeddingList = convertFloatArrayToDoubleList(embedding);
                log.debug("Successfully generated embedding with {} dimensions", embeddingList.size());
                return embeddingList;
            }

            log.warn("Empty embedding response for text: {}", text);
            throw new RuntimeException("Failed to generate embedding: empty response");

        } catch (Exception e) {
            log.error("Error generating embedding for text: {}", text, e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    /**
     * Converts a float array to a List of Doubles.
     *
     * @param floatArray The float array to convert
     * @return A List of Double values
     */
    private List<Double> convertFloatArrayToDoubleList(float[] floatArray) {
        List<Double> doubleList = new java.util.ArrayList<>();
        for (float value : floatArray) {
            doubleList.add((double) value);
        }
        return doubleList;
    }

    /**
     * Calculates cosine similarity between two embedding vectors.
     *
     * @param embedding1 First embedding vector
     * @param embedding2 Second embedding vector
     * @return Cosine similarity score between 0 and 1
     */
    public double calculateCosineSimilarity(List<Double> embedding1, List<Double> embedding2) {
        if (embedding1.size() != embedding2.size()) {
            throw new IllegalArgumentException("Embeddings must have the same dimension");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.size(); i++) {
            dotProduct += embedding1.get(i) * embedding2.get(i);
            norm1 += embedding1.get(i) * embedding1.get(i);
            norm2 += embedding2.get(i) * embedding2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
