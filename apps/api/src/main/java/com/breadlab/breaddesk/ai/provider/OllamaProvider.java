package com.breadlab.breaddesk.ai.provider;

import com.breadlab.breaddesk.ai.LLMProvider;
import com.breadlab.breaddesk.ai.LLMResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Ollama LLM Provider 구현
 */
@Slf4j
@Component
public class OllamaProvider implements LLMProvider {

    private final WebClient webClient;
    private final String model;

    public OllamaProvider(
            @Value("${breaddesk.llm.ollama.url:http://localhost:11434}") String url,
            @Value("${breaddesk.llm.ollama.model:llama3.1:8b}") String model) {
        this.webClient = WebClient.builder()
                .baseUrl(url)
                .build();
        this.model = model;
        log.info("Initialized OllamaProvider: {} @ {}", model, url);
    }

    @Override
    public LLMResponse chat(String systemPrompt, String userMessage, List<String> contextDocuments) {
        try {
            String combinedPrompt = buildPrompt(systemPrompt, userMessage, contextDocuments);

            var request = Map.of(
                    "model", model,
                    "prompt", combinedPrompt,
                    "stream", false
            );

            var response = webClient.post()
                    .uri("/api/generate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, clientResponse ->
                            Mono.error(new RuntimeException("Ollama API error: " + clientResponse.statusCode())))
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .onErrorReturn(Map.of("response", "AI 서비스 오류가 발생했습니다. 담당자에게 문의해주세요.", "error", true))
                    .block();

            if (response != null && response.containsKey("response")) {
                String content = (String) response.get("response");
                boolean isError = response.containsKey("error");
                float confidence = isError ? 0.0f : estimateConfidence(content);

                log.debug("Ollama response: {} chars, confidence: {}, error: {}", content.length(), confidence, isError);
                return LLMResponse.of(content, confidence);
            } else {
                log.warn("Unexpected Ollama response format: {}", response);
                return LLMResponse.of("응답 생성에 실패했습니다.", 0.0f);
            }
        } catch (Exception e) {
            log.error("Failed to call Ollama API: {}", e.getMessage(), e);
            // Fallback 응답 반환
            return LLMResponse.of("AI 서비스에 연결할 수 없습니다. 담당자가 곧 연락드리겠습니다.", 0.0f);
        }
        // TODO Phase 2: 동시 요청 제한 (Resilience4j RateLimiter 또는 Semaphore)
    }

    @Override
    public float[] embed(String text) {
        try {
            var request = Map.of(
                    "model", model,
                    "prompt", text
            );

            var response = webClient.post()
                    .uri("/api/embeddings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null && response.containsKey("embedding")) {
                List<Double> embedding = (List<Double>) response.get("embedding");
                float[] result = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    result[i] = embedding.get(i).floatValue();
                }
                return result;
            } else {
                log.warn("Unexpected embedding response: {}", response);
                return new float[0];
            }
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            return new float[0];
        }
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public boolean isAvailable() {
        try {
            // 간단한 health check
            webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Ollama not available: {}", e.getMessage());
            return false;
        }
    }

    private String buildPrompt(String systemPrompt, String userMessage, List<String> contextDocuments) {
        StringBuilder prompt = new StringBuilder();

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            prompt.append("System: ").append(systemPrompt).append("\n\n");
        }

        if (contextDocuments != null && !contextDocuments.isEmpty()) {
            prompt.append("관련 문서:\n");
            for (int i = 0; i < contextDocuments.size(); i++) {
                prompt.append(i + 1).append(". ").append(contextDocuments.get(i)).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("사용자 문의: ").append(userMessage).append("\n\n");
        prompt.append("답변:");

        return prompt.toString();
    }

    /**
     * 답변의 신뢰도를 추정
     * Phase 1: 간단한 휴리스틱
     * TODO Phase 2: 실제 모델 기반 신뢰도 평가
     */
    private float estimateConfidence(String response) {
        if (response == null || response.isBlank()) {
            return 0.0f;
        }

        // 간단한 휴리스틱: 답변 길이, 특정 키워드 포함 여부 등
        float confidence = 0.5f;

        // 충분한 답변 길이
        if (response.length() > 100) {
            confidence += 0.2f;
        }

        // 확신 없는 표현이 포함되면 감점
        if (response.contains("모르겠") || response.contains("확실하지") || response.contains("잘 모릅니다")) {
            confidence -= 0.3f;
        }

        // 구체적 정보 포함 시 가점
        if (response.contains("단계") || response.contains("방법") || response.matches(".*\\d+\\..*")) {
            confidence += 0.1f;
        }

        return Math.max(0.0f, Math.min(1.0f, confidence));
    }
}
