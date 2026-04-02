package com.breadlab.breaddesk.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Anthropic Claude API Provider
 * 임베딩은 Voyage AI 또는 다른 provider로 위임 (Claude는 임베딩 미제공)
 */
@Slf4j
public class ClaudeLLMProvider implements LLMProvider {

    private final WebClient webClient;
    private final String model;
    private final boolean isAvailable;

    public ClaudeLLMProvider(
            @Value("${ANTHROPIC_API_KEY:}") String apiKey,
            @Value("${breaddesk.llm.claude.model:claude-sonnet-4-5-20250514}") String model) {
        this.model = model;
        this.isAvailable = apiKey != null && !apiKey.isBlank();

        if (!isAvailable) {
            log.warn("Claude API 키가 설정되지 않았습니다. AI 기능이 비활성화됩니다.");
            this.webClient = null;
        } else {
            this.webClient = WebClient.builder()
                    .baseUrl("https://api.anthropic.com/v1")
                    .defaultHeader("x-api-key", apiKey)
                    .defaultHeader("anthropic-version", "2023-06-01")
                    .build();
        }
    }

    @Override
    public LLMResponse chat(String systemPrompt, String userMessage, List<String> contextDocuments) {
        if (!isAvailable) {
            return LLMResponse.of("AI 서비스가 설정되지 않았습니다.", 0.0f);
        }

        try {
            StringBuilder userContent = new StringBuilder();
            if (contextDocuments != null && !contextDocuments.isEmpty()) {
                userContent.append("참고 문서:\n");
                for (String doc : contextDocuments) {
                    userContent.append("- ").append(doc).append("\n");
                }
                userContent.append("\n");
            }
            userContent.append("사용자 질문: ").append(userMessage);

            List<Map<String, String>> messages = List.of(
                    Map.of("role", "user", "content", userContent.toString())
            );

            Map<String, Object> request = Map.of(
                    "model", model,
                    "max_tokens", 1000,
                    "system", systemPrompt,
                    "messages", messages
            );

            ClaudeChatResponse response = webClient.post()
                    .uri("/messages")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ClaudeChatResponse.class)
                    .block();

            if (response == null || response.content() == null || response.content().isEmpty()) {
                return LLMResponse.of("죄송합니다. 답변을 생성할 수 없습니다.", 0.0f);
            }

            String content = response.content().get(0).text().trim();
            float confidence = estimateConfidence(content, response);

            return new LLMResponse(content, confidence, Map.of(
                    "model", model,
                    "usage", response.usage() != null ? response.usage() : Map.of(),
                    "stopReason", response.stopReason() != null ? response.stopReason() : "unknown"
            ));
        } catch (Exception e) {
            log.error("Claude API 호출 실패: {}", e.getMessage(), e);
            return LLMResponse.of("AI 서비스에 일시적인 문제가 있습니다.", 0.0f);
        }
    }

    @Override
    public float[] embed(String text) {
        // Claude는 임베딩을 제공하지 않으므로 fallback (OpenAI 또는 로컬 모델 사용 권장)
        log.warn("Claude는 임베딩을 지원하지 않습니다. 다른 임베딩 provider를 사용하세요.");
        return new float[0];
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public boolean isAvailable() {
        return isAvailable;
    }

    private float estimateConfidence(String content, ClaudeChatResponse response) {
        if (content == null || content.isBlank()) return 0.0f;

        String lower = content.toLowerCase();
        int uncertaintyCount = 0;

        String[] uncertaintyPhrases = {
                "잘 모르겠", "확실하지 않", "정확하지 않", "확인이 필요",
                "추가 정보", "더 알아봐야", "i'm not sure", "i don't know",
                "uncertain", "perhaps", "maybe", "might", "담당자에게"
        };

        for (String phrase : uncertaintyPhrases) {
            if (lower.contains(phrase)) uncertaintyCount++;
        }

        if (uncertaintyCount >= 3) return 0.3f;
        if (uncertaintyCount >= 2) return 0.5f;
        if (uncertaintyCount >= 1) return 0.6f;

        if (content.length() < 50) return 0.6f;

        // Claude의 stop_reason이 max_tokens면 잘렸을 수 있음
        if ("max_tokens".equals(response.stopReason())) {
            return 0.7f;
        }

        return 0.85f; // Claude는 일반적으로 높은 품질
    }

    // Claude API response DTOs
    private record ClaudeChatResponse(
            List<ContentBlock> content,
            Usage usage,
            @JsonProperty("stop_reason") String stopReason
    ) {
        record ContentBlock(String text) {}
        record Usage(
                @JsonProperty("input_tokens") int inputTokens,
                @JsonProperty("output_tokens") int outputTokens
        ) {}
    }
}
