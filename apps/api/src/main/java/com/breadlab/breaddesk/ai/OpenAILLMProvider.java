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
 * OpenAI API Provider (GPT-4, GPT-3.5, text-embedding-3-small 등)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "breaddesk.llm.provider", havingValue = "openai")
public class OpenAILLMProvider implements LLMProvider {

    private final WebClient webClient;
    private final String model;
    private final String embeddingModel;
    private final boolean isAvailable;

    public OpenAILLMProvider(
            @Value("${OPENAI_API_KEY:}") String apiKey,
            @Value("${breaddesk.llm.openai.model:gpt-4o}") String model,
            @Value("${breaddesk.llm.openai.embedding-model:text-embedding-3-small}") String embeddingModel) {
        this.model = model;
        this.embeddingModel = embeddingModel;
        this.isAvailable = apiKey != null && !apiKey.isBlank();

        if (!isAvailable) {
            log.warn("OpenAI API 키가 설정되지 않았습니다. AI 기능이 비활성화됩니다.");
            this.webClient = null;
        } else {
            this.webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
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
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userContent.toString())
            );

            Map<String, Object> request = Map.of(
                    "model", model,
                    "messages", messages,
                    "temperature", 0.7,
                    "max_tokens", 1000
            );

            OpenAIChatResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAIChatResponse.class)
                    .block();

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return LLMResponse.of("죄송합니다. 답변을 생성할 수 없습니다.", 0.0f);
            }

            String content = response.choices().get(0).message().content().trim();
            float confidence = estimateConfidence(content, response);

            return new LLMResponse(content, confidence, Map.of(
                    "model", model,
                    "usage", response.usage() != null ? response.usage() : Map.of()
            ));
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage(), e);
            return LLMResponse.of("AI 서비스에 일시적인 문제가 있습니다.", 0.0f);
        }
    }

    @Override
    public float[] embed(String text) {
        if (!isAvailable) {
            return new float[0];
        }

        try {
            Map<String, Object> request = Map.of(
                    "model", embeddingModel,
                    "input", text
            );

            OpenAIEmbeddingResponse response = webClient.post()
                    .uri("/embeddings")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OpenAIEmbeddingResponse.class)
                    .block();

            if (response != null && response.data() != null && !response.data().isEmpty()) {
                List<Float> embedding = response.data().get(0).embedding();
                float[] result = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    result[i] = embedding.get(i);
                }
                return result;
            }
        } catch (Exception e) {
            log.error("OpenAI 임베딩 생성 실패: {}", e.getMessage(), e);
        }
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

    /**
     * 응답 내용 및 OpenAI 메타데이터 기반 confidence 추정
     */
    private float estimateConfidence(String content, OpenAIChatResponse response) {
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

        // 길이가 너무 짧으면 낮은 confidence
        if (content.length() < 50) return 0.6f;

        // finish_reason이 length면 중간 정도로 (잘렸을 수 있음)
        if (response.choices() != null && !response.choices().isEmpty()) {
            String finishReason = response.choices().get(0).finishReason();
            if ("length".equals(finishReason)) {
                return 0.7f;
            }
        }

        return 0.85f; // OpenAI는 일반적으로 높은 품질
    }

    // OpenAI API response DTOs
    private record OpenAIChatResponse(
            List<Choice> choices,
            Usage usage
    ) {
        record Choice(Message message, @JsonProperty("finish_reason") String finishReason) {}
        record Message(String content) {}
        record Usage(
                @JsonProperty("prompt_tokens") int promptTokens,
                @JsonProperty("completion_tokens") int completionTokens,
                @JsonProperty("total_tokens") int totalTokens
        ) {}
    }

    private record OpenAIEmbeddingResponse(
            List<EmbeddingData> data
    ) {
        record EmbeddingData(List<Float> embedding) {}
    }
}
