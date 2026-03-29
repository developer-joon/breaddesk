package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.entity.InquiryResolvedBy;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 자동 답변 서비스.
 * 문의 접수 시 LLM에 질문 → 응답 + confidence 저장.
 * confidence >= 0.7 이면 AI_ANSWERED, 아니면 에스컬레이션 대상.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnswerService {

    private static final float CONFIDENCE_THRESHOLD = 0.7f;

    private static final String SYSTEM_PROMPT = """
            당신은 BreadDesk 고객 지원 AI 어시스턴트입니다.
            고객의 문의에 친절하고 정확하게 답변해주세요.
            확실하지 않은 내용은 추측하지 말고, 담당자 확인이 필요하다고 안내해주세요.
            답변은 간결하되 도움이 되도록 작성해주세요.
            """;

    private final LLMProvider llmProvider;
    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;

    /**
     * 문의에 대해 AI 답변을 시도합니다.
     *
     * @return true if AI confidence >= threshold (auto-resolved), false otherwise (escalation needed)
     */
    @Transactional
    public boolean tryAutoAnswer(Inquiry inquiry) {
        if (!llmProvider.isAvailable()) {
            log.warn("LLM 서비스 사용 불가 — 에스컬레이션으로 전환");
            return false;
        }

        try {
            LLMResponse response = llmProvider.chat(
                    SYSTEM_PROMPT,
                    inquiry.getMessage(),
                    List.of()  // Phase 2에서 knowledge docs 연동
            );

            // AI 응답 저장
            inquiry.setAiResponse(response.content());
            inquiry.setAiConfidence(response.confidence());

            // AI 메시지 이력 추가
            InquiryMessage aiMessage = InquiryMessage.builder()
                    .inquiry(inquiry)
                    .role(InquiryMessageRole.AI)
                    .message(response.content())
                    .createdAt(LocalDateTime.now())
                    .build();
            inquiryMessageRepository.save(aiMessage);

            if (response.confidence() >= CONFIDENCE_THRESHOLD) {
                inquiry.setStatus(InquiryStatus.AI_ANSWERED);
                inquiry.setResolvedBy(InquiryResolvedBy.AI);
                inquiryRepository.save(inquiry);
                log.info("문의 #{} AI 자동 답변 성공 (confidence: {})", inquiry.getId(), response.confidence());
                return true;
            } else {
                inquiryRepository.save(inquiry);
                log.info("문의 #{} AI confidence 부족 ({} < {}) — 에스컬레이션 필요",
                        inquiry.getId(), response.confidence(), CONFIDENCE_THRESHOLD);
                return false;
            }
        } catch (Exception e) {
            log.error("AI 답변 시도 중 오류 (문의 #{}): {}", inquiry.getId(), e.getMessage(), e);
            return false;
        }
    }
}
