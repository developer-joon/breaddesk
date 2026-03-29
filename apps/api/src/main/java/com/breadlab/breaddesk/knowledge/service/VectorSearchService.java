package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.knowledge.repository.KnowledgeDocumentRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * pgvector 기반 벡터 유사도 검색 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private static final double DEFAULT_THRESHOLD = 0.7;
    private static final int DEFAULT_LIMIT = 5;

    private final KnowledgeDocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    public List<SearchResult> search(String query) {
        return search(query, DEFAULT_LIMIT, DEFAULT_THRESHOLD);
    }

    public List<SearchResult> search(String query, int limit, double threshold) {
        float[] queryEmbedding = embeddingService.embed(query);
        if (queryEmbedding.length == 0) {
            log.warn("쿼리 임베딩 생성 실패: {}", query);
            return List.of();
        }

        String vectorString = embeddingService.toVectorString(queryEmbedding);
        List<Object[]> rows = documentRepository.findSimilarDocuments(vectorString, threshold, limit);

        List<SearchResult> results = new ArrayList<>();
        for (Object[] row : rows) {
            try {
                Long id = ((Number) row[0]).longValue();
                String source = (String) row[1];
                String sourceId = (String) row[2];
                String title = (String) row[3];
                String content = (String) row[4];
                String url = (String) row[5];
                double similarity = ((Number) row[row.length - 1]).doubleValue();
                results.add(new SearchResult(id, source, sourceId, title, content, url, similarity));
            } catch (Exception e) {
                log.warn("검색 결과 파싱 오류: {}", e.getMessage());
            }
        }

        log.debug("벡터 검색 '{}' → {} 결과 (threshold: {})", query, results.size(), threshold);
        return results;
    }

    public List<String> searchAsContext(String query, int limit) {
        return search(query, limit, DEFAULT_THRESHOLD).stream()
                .map(r -> "[%s] %s\n%s".formatted(r.source(), r.title(), r.content()))
                .toList();
    }

    public record SearchResult(
            Long id, String source, String sourceId, String title,
            String content, String url, double similarity
    ) {}
}
