package com.breadlab.breaddesk.ai;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class OllamaLLMProvider implements LLMProvider {

    private final WebClient webClient;
    private final String model;

    public OllamaLLMProvider(
            @Value("${breaddesk.llm.ollama.url}") String ollamaUrl,
            @Value("${breaddesk.llm.ollama.model}") String model) {
        this.webClient = WebClient.builder()
                .baseUrl(ollamaUrl)
                .build();
        this.model = model;
    }

    @Override
    public LLMResponse chat(String systemPrompt, String userMessage, List<String> contextDocuments) {
        StringBuilder prompt = new StringBuilder();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            prompt.append(systemPrompt).append("\n\n");
        }
        if (contextDocuments != null && !contextDocuments.isEmpty()) {
            prompt.append("참고 문서:\n");
            for (String doc : contextDocuments) {
                prompt.append("- ").append(doc).append("\n");
            }
            prompt.append("\n");
        }
        prompt.append("사용자 질문: ").append(userMessage);

        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "prompt", prompt.toString(),
                    "stream", false
            );

            OllamaGenerateResponse response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaGenerateResponse.class)
                    .block();

            if (response == null || response.response() == null) {
                return LLMResponse.of("죄송합니다. 답변을 생성할 수 없습니다.", 0.0f);
            }

            String content = response.response().trim();
            float confidence = estimateConfidence(content);

            return new LLMResponse(content, confidence, Map.of(
                    "model", model,
                    "totalDuration", response.totalDuration() != null ? response.totalDuration() : 0
            ));
        } catch (Exception e) {
            log.error("Ollama LLM 호출 실패: {}", e.getMessage(), e);
            return LLMResponse.of("AI 서비스에 일시적인 문제가 있습니다.", 0.0f);
        }
    }

    @Override
    public float[] embed(String text) {
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "input", text
            );

            OllamaEmbeddingResponse response = webClient.post()
                    .uri("/api/embed")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaEmbeddingResponse.class)
                    .block();

            if (response != null && response.embeddings() != null && !response.embeddings().isEmpty()) {
                List<Float> embedding = response.embeddings().get(0);
                float[] result = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    result[i] = embedding.get(i);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("Ollama 임베딩 생성 실패: {}", e.getMessage(), e);
        }
        return new float[0];
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public boolean isAvailable() {
        try {
            webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 응답 내용 기반 confidence 추정.
     * 불확실성 표현이 많으면 낮게, 구체적이면 높게.
     */
    private float estimateConfidence(String content) {
        if (content == null || content.isBlank()) return 0.0f;

        String lower = content.toLowerCase();
        int uncertaintyCount = 0;

        String[] uncertaintyPhrases = {
                "잘 모르겠", "확실하지 않", "정확하지 않", "확인이 필요",
                "추가 정보", "더 알아봐야", "i'm not sure", "i don't know",
                "uncertain", "perhaps", "maybe", "might"
        };

        for (String phrase : uncertaintyPhrases) {
            if (lower.contains(phrase)) uncertaintyCount++;
        }

        if (uncertaintyCount >= 3) return 0.3f;
        if (uncertaintyCount >= 2) return 0.5f;
        if (uncertaintyCount >= 1) return 0.6f;

        // 길이가 너무 짧으면 낮은 confidence
        if (content.length() < 50) return 0.6f;

        return 0.8f;
    }

    private record OllamaGenerateResponse(
            String response,
            Long totalDuration,
            Boolean done
    ) {}

    private record OllamaEmbeddingResponse(
            List<List<Float>> embeddings
    ) {}
}
