package com.breadlab.breaddesk.inquiry.service;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.knowledge.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Detect repeated inquiry patterns using vector clustering
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryPatternService {

    private final InquiryRepository inquiryRepository;
    private final EmbeddingService embeddingService;

    /**
     * Pattern cluster result
     */
    public record InquiryPattern(
            String representativeText,
            int count,
            List<Long> inquiryIds,
            float avgConfidence
    ) {}

    /**
     * Cluster similar inquiries and return repeated patterns
     * 
     * @param minClusterSize Minimum inquiries to form a pattern (default 3)
     * @param similarityThreshold Similarity threshold 0-1 (default 0.85)
     * @param limit Max number of patterns to return
     */
    public List<InquiryPattern> findRepeatedPatterns(int minClusterSize, float similarityThreshold, int limit) {
        // Get recent resolved inquiries
        var inquiries = inquiryRepository.findAll().stream()
                .filter(i -> i.getStatus() == com.breadlab.breaddesk.inquiry.entity.InquiryStatus.RESOLVED
                          || i.getStatus() == com.breadlab.breaddesk.inquiry.entity.InquiryStatus.CLOSED)
                .sorted(Comparator.comparing(Inquiry::getCreatedAt).reversed())
                .limit(500) // Process last 500 inquiries
                .toList();

        if (inquiries.isEmpty()) {
            return Collections.emptyList();
        }

        // Generate embeddings for all inquiries
        Map<Long, float[]> embeddings = new HashMap<>();
        for (Inquiry inquiry : inquiries) {
            try {
                float[] embedding = embeddingService.embed(inquiry.getMessage());
                embeddings.put(inquiry.getId(), embedding);
            } catch (Exception e) {
                log.error("Failed to embed inquiry #{}: {}", inquiry.getId(), e.getMessage());
            }
        }

        // Cluster using simple greedy approach
        Set<Long> processed = new HashSet<>();
        List<InquiryPattern> patterns = new ArrayList<>();

        for (Inquiry inquiry : inquiries) {
            if (processed.contains(inquiry.getId())) {
                continue;
            }

            float[] embedding = embeddings.get(inquiry.getId());
            if (embedding == null || embedding.length == 0) {
                continue;
            }

            // Find all similar inquiries
            List<Long> cluster = new ArrayList<>();
            cluster.add(inquiry.getId());
            processed.add(inquiry.getId());

            float totalConfidence = inquiry.getAiConfidence() != null ? inquiry.getAiConfidence() : 0f;

            for (Inquiry other : inquiries) {
                if (processed.contains(other.getId())) {
                    continue;
                }

                float[] otherEmbedding = embeddings.get(other.getId());
                if (otherEmbedding == null || otherEmbedding.length == 0) {
                    continue;
                }

                float similarity = cosineSimilarity(embedding, otherEmbedding);
                if (similarity >= similarityThreshold) {
                    cluster.add(other.getId());
                    processed.add(other.getId());
                    totalConfidence += other.getAiConfidence() != null ? other.getAiConfidence() : 0f;
                }
            }

            // If cluster is large enough, it's a pattern
            if (cluster.size() >= minClusterSize) {
                patterns.add(new InquiryPattern(
                        inquiry.getMessage(),
                        cluster.size(),
                        cluster,
                        totalConfidence / cluster.size()
                ));
            }
        }

        // Sort by count (most frequent first)
        patterns.sort(Comparator.comparingInt(InquiryPattern::count).reversed());

        // Return top N patterns
        return patterns.stream().limit(limit).toList();
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) {
            throw new IllegalArgumentException("Vectors must have same length");
        }

        float dotProduct = 0f;
        float normA = 0f;
        float normB = 0f;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0f || normB == 0f) {
            return 0f;
        }

        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
