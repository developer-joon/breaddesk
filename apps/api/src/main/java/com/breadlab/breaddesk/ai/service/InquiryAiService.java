package com.breadlab.breaddesk.ai.service;

import com.breadlab.breaddesk.ai.LLMProvider;
import com.breadlab.breaddesk.ai.LLMResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 문의 AI 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryAiService {

    private final LLMProvider llmProvider;

    /**
     * 문의에 대한 AI 답변 생성
     */
    public AiAnswerResult generateAnswer(String message) {
        log.info("Generating AI answer for inquiry");

        if (!llmProvider.isAvailable()) {
            log.warn("LLM not available, returning fallback response");
            return AiAnswerResult.builder()
                    .answer("AI 서비스가 일시적으로 사용할 수 없습니다. 담당자가 곧 연락드리겠습니다.")
                    .confidence(0.0)
                    .shouldEscalate(true)
                    .build();
        }

        String systemPrompt = """
                당신은 IT 서비스 데스크 AI 어시스턴트입니다.
                사용자의 문의에 친절하고 정확하게 답변하세요.
                확실하지 않은 경우 솔직하게 "담당자에게 문의해주세요"라고 답변하세요.
                """;

        // TODO Phase 2: 벡터 검색으로 관련 문서 조회
        List<String> contextDocs = List.of();

        LLMResponse response = llmProvider.chat(systemPrompt, message, contextDocs);

        boolean shouldEscalate = response.confidence() < 0.5;

        return AiAnswerResult.builder()
                .answer(response.content())
                .confidence(response.confidence())
                .shouldEscalate(shouldEscalate)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AiAnswerResult {
        private String answer;
        private Double confidence;
        private Boolean shouldEscalate;
    }
}
