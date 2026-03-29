package com.breadlab.breaddesk.ai.service;

import com.breadlab.breaddesk.ai.LLMProvider;
import com.breadlab.breaddesk.ai.LLMResponse;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 문의 AI 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryAiService {

    private final LLMProvider llmProvider;
    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository messageRepository;

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

    /**
     * 비동기로 AI 답변 생성 후 문의 상태 업데이트
     * @param inquiryId 문의 ID
     * @param message 문의 내용
     */
    @Async
    @Transactional
    public void generateAnswerAsync(Long inquiryId, String message) {
        log.info("Async AI answer generation started for inquiry {}", inquiryId);

        try {
            Inquiry inquiry = inquiryRepository.findById(inquiryId).orElseThrow();

            // AI 답변 생성
            AiAnswerResult aiResult = generateAnswer(message);
            inquiry.setAiResponse(aiResult.getAnswer());
            inquiry.setAiConfidence(aiResult.getConfidence());

            // 신뢰도에 따른 상태 변경
            if (aiResult.getConfidence() >= 0.8) {
                // 높은 신뢰도: 자동 답변
                inquiry.setStatus(Inquiry.InquiryStatus.AI_ANSWERED);
                saveMessage(inquiryId, InquiryMessage.MessageRole.AI, aiResult.getAnswer());
                log.info("High confidence AI answer for inquiry {}", inquiryId);
            } else if (aiResult.getConfidence() >= 0.5) {
                // 중간 신뢰도: 답변 + 담당자 알림
                inquiry.setStatus(Inquiry.InquiryStatus.AI_ANSWERED);
                saveMessage(inquiryId, InquiryMessage.MessageRole.AI, aiResult.getAnswer());
                log.warn("Medium confidence AI answer for inquiry {} - human review needed", inquiryId);
            } else {
                // 낮은 신뢰도: 에스컬레이션 필요 (InquiryService에서 처리)
                inquiry.setStatus(Inquiry.InquiryStatus.OPEN);
                log.info("Low confidence for inquiry {} - manual escalation needed", inquiryId);
            }

            inquiryRepository.save(inquiry);
            log.info("Async AI answer generation completed for inquiry {}", inquiryId);

        } catch (Exception e) {
            log.error("Failed to generate AI answer for inquiry {}: {}", inquiryId, e.getMessage(), e);
            // AI 실패 시 문의 상태를 OPEN으로 유지
            try {
                Inquiry inquiry = inquiryRepository.findById(inquiryId).orElse(null);
                if (inquiry != null && inquiry.getStatus() != Inquiry.InquiryStatus.AI_ANSWERED) {
                    inquiry.setStatus(Inquiry.InquiryStatus.OPEN);
                    inquiry.setAiResponse("AI 답변 생성 실패: 담당자가 곧 연락드리겠습니다.");
                    inquiryRepository.save(inquiry);
                }
            } catch (Exception ex) {
                log.error("Failed to update inquiry status after AI error: {}", ex.getMessage());
            }
            // TODO Phase 2: 관리자 알림 (이메일, Slack 등)
        }
    }

    private void saveMessage(Long inquiryId, InquiryMessage.MessageRole role, String message) {
        InquiryMessage msg = InquiryMessage.builder()
                .inquiryId(inquiryId)
                .role(role)
                .message(message)
                .build();
        messageRepository.save(msg);
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
