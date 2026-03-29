package com.breadlab.breaddesk.inquiry.service;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.knowledge.service.EmbeddingService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 유사 문의 감지 서비스.
 * 새 문의 접수 시 기존 문의와 벡터 유사도를 비교하여 유사 문의를 찾는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimilarInquiryService {

    private static final double SIMILARITY_THRESHOLD = 0.85;
    private static final int MAX_SIMILAR = 5;

    private final EmbeddingService embeddingService;
    private final InquiryRepository inquiryRepository;

    /**
     * 주어진 메시지와 유사한 기존 문의를 찾는다.
     */
    public List<SimilarInquiry> findSimilar(String message, Long excludeInquiryId) {
        float[] embedding = embeddingService.embed(message);
        if (embedding.length == 0) {
            log.warn("유사 문의 검색 실패: 임베딩 생성 불가");
            return List.of();
        }

        // 최근 문의들과 코사인 유사도 비교 (in-memory)
        List<Inquiry> recentInquiries = inquiryRepository.findAll(
                PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();

        List<SimilarInquiry> results = new ArrayList<>();

        for (Inquiry inquiry : recentInquiries) {
            if (inquiry.getId().equals(excludeInquiryId)) continue;
            if (inquiry.getMessage() == null || inquiry.getMessage().isBlank()) continue;

            float[] otherEmbedding = embeddingService.embed(inquiry.getMessage());
            if (otherEmbedding.length == 0) continue;

            double similarity = cosineSimilarity(embedding, otherEmbedding);
            if (similarity >= SIMILARITY_THRESHOLD) {
                results.add(new SimilarInquiry(
                        inquiry.getId(),
                        inquiry.getMessage(),
                        inquiry.getStatus().name(),
                        similarity
                ));
            }

            if (results.size() >= MAX_SIMILAR) break;
        }

        results.sort((a, b) -> Double.compare(b.similarity(), a.similarity()));
        log.debug("유사 문의 검색 결과: {} 건 (threshold: {})", results.size(), SIMILARITY_THRESHOLD);
        return results;
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length || a.length == 0) return 0.0;

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        double denominator = Math.sqrt(normA) * Math.sqrt(normB);
        return denominator == 0 ? 0.0 : dotProduct / denominator;
    }

    public record SimilarInquiry(
            Long inquiryId,
            String message,
            String status,
            double similarity
    ) {}
}
