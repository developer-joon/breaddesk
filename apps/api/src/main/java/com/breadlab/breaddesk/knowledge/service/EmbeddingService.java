package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.ai.LLMProvider;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 텍스트 임베딩 서비스.
 * 텍스트 → 청킹 → 임베딩 벡터 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 100;

    private final LLMProvider llmProvider;

    /**
     * 텍스트를 임베딩 벡터로 변환
     */
    public float[] embed(String text) {
        if (text == null || text.isBlank()) {
            return new float[0];
        }
        return llmProvider.embed(text);
    }

    /**
     * 임베딩 벡터를 pgvector 포맷 문자열로 변환
     * 예: [0.1,0.2,0.3]
     */
    public String toVectorString(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 텍스트를 청크로 분할 (고정 크기 + 오버랩)
     */
    public List<String> chunkText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        int length = text.length();

        if (length <= CHUNK_SIZE) {
            chunks.add(text.trim());
            return chunks;
        }

        int start = 0;
        while (start < length) {
            int end = Math.min(start + CHUNK_SIZE, length);

            // 문장 경계에서 자르기 시도
            if (end < length) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int breakPoint = Math.max(lastPeriod, lastNewline);
                if (breakPoint > start + CHUNK_SIZE / 2) {
                    end = breakPoint + 1;
                }
            }

            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start = end - CHUNK_OVERLAP;
            if (start >= length) break;
        }

        return chunks;
    }

    /**
     * 텍스트를 청킹 후 각 청크를 임베딩
     */
    public List<ChunkWithEmbedding> chunkAndEmbed(String text) {
        List<String> chunks = chunkText(text);
        List<ChunkWithEmbedding> results = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            float[] embedding = embed(chunk);
            if (embedding.length > 0) {
                results.add(new ChunkWithEmbedding(chunk, embedding, i));
            } else {
                log.warn("청크 #{} 임베딩 실패 — 스킵", i);
            }
        }

        return results;
    }

    public record ChunkWithEmbedding(String content, float[] embedding, int chunkIndex) {}
}
