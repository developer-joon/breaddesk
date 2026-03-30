package com.breadlab.breaddesk.knowledge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingService {

    private final WebClient webClient;
    private final String model;

    public EmbeddingService(
            @Value("${breaddesk.llm.ollama.url:http://localhost:11434}") String ollamaUrl,
            @Value("${breaddesk.embedding.local.model:all-minilm:l6-v2}") String model) {
        this.webClient = WebClient.builder().baseUrl(ollamaUrl).build();
        this.model = model;
    }

    @SuppressWarnings("unchecked")
    public float[] embed(String text) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri("/api/embeddings")
                    .bodyValue(Map.of("model", model, "prompt", text))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("embedding")) {
                List<Double> embedding = (List<Double>) response.get("embedding");
                float[] result = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    result[i] = embedding.get(i).floatValue();
                }
                return result;
            }
        } catch (Exception e) {
            log.warn("임베딩 생성 실패: {}", e.getMessage());
        }
        return new float[0];
    }

    public List<String> chunk(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += chunkSize - overlap;
        }
        return chunks;
    }

    public List<String> chunk(String text) {
        return chunk(text, 500, 100);
    }
}
