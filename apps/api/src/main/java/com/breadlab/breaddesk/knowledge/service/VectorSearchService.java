package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.knowledge.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VectorSearchService {

    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentRepository documentRepository;

    public List<Object[]> search(String query, int limit) {
        float[] embedding = embeddingService.embed(query);
        if (embedding.length == 0) {
            log.warn("임베딩 생성 실패, 빈 결과 반환");
            return List.of();
        }
        String vectorString = "[" + Arrays.stream(toDoubleArray(embedding))
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(",")) + "]";
        return documentRepository.findSimilarDocuments(vectorString, limit);
    }

    private double[] toDoubleArray(float[] floats) {
        double[] doubles = new double[floats.length];
        for (int i = 0; i < floats.length; i++) doubles[i] = floats[i];
        return doubles;
    }
}
