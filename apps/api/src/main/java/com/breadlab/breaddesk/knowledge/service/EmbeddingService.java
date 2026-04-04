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

    private final WebClient ollamaClient;
    private final WebClient openaiClient;
    private final String provider;
    private final String localModel;
    private final String openaiModel;
    private final String openaiApiKey;

    public EmbeddingService(
            @Value("${breaddesk.embedding.provider:local}") String provider,
            @Value("${breaddesk.llm.ollama.url:http://localhost:11434}") String ollamaUrl,
            @Value("${breaddesk.embedding.local.model:all-minilm:l6-v2}") String localModel,
            @Value("${breaddesk.embedding.openai.model:text-embedding-3-small}") String openaiModel,
            @Value("${OPENAI_API_KEY:}") String openaiApiKey) {
        this.provider = provider;
        this.localModel = localModel;
        this.openaiModel = openaiModel;
        this.openaiApiKey = openaiApiKey;
        
        this.ollamaClient = WebClient.builder().baseUrl(ollamaUrl).build();
        this.openaiClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + openaiApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        
        log.info("EmbeddingService initialized with provider: {}", provider);
    }

    @SuppressWarnings("unchecked")
    public float[] embed(String text) {
        if ("openai".equalsIgnoreCase(provider)) {
            return embedWithOpenAI(text);
        }
        return embedWithOllama(text);
    }

    @SuppressWarnings("unchecked")
    private float[] embedWithOllama(String text) {
        try {
            Map<String, Object> response = ollamaClient.post()
                    .uri("/api/embeddings")
                    .bodyValue(Map.of("model", localModel, "prompt", text))
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
            log.warn("Ollama 임베딩 생성 실패: {}", e.getMessage());
        }
        return new float[0];
    }

    @SuppressWarnings("unchecked")
    private float[] embedWithOpenAI(String text) {
        try {
            Map<String, Object> requestBody = Map.of(
                "input", text,
                "model", openaiModel
            );
            
            Map<String, Object> response = openaiClient.post()
                    .uri("/v1/embeddings")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                if (!data.isEmpty()) {
                    List<Double> embedding = (List<Double>) data.get(0).get("embedding");
                    float[] result = new float[embedding.size()];
                    for (int i = 0; i < embedding.size(); i++) {
                        result[i] = embedding.get(i).floatValue();
                    }
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("OpenAI 임베딩 생성 실패: {}", e.getMessage(), e);
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

    /**
     * Convert float array to PostgreSQL vector string format: "[0.1, 0.2, 0.3]"
     */
    public String floatArrayToString(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
