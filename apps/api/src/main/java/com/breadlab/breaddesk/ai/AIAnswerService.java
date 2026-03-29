package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.*;
import com.breadlab.breaddesk.inquiry.repository.*;
import com.breadlab.breaddesk.knowledge.service.VectorSearchService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnswerService {

    private static final float CONFIDENCE_THRESHOLD = 0.7f;
    private static final int RAG_CONTEXT_LIMIT = 5;

    private static final String SYSTEM_PROMPT = """
            당신은 BreadDesk 고객 지원 AI 어시스턴트입니다.
            고객의 문의에 친절하고 정확하게 답변해주세요.
            확실하지 않은 내용은 추측하지 말고, 담당자 확인이 필요하다고 안내해주세요.
            답변은 간결하되 도움이 되도록 작성해주세요.
            """;

    private static final String RAG_SYSTEM_PROMPT = """
            당신은 BreadDesk 고객 지원 AI 어시스턴트입니다.
            다음 관련 문서를 참고하여 고객의 질문에 정확하게 답변해주세요.
            문서에 없는 내용은 추측하지 말고, 담당자 확인이 필요하다고 안내해주세요.
            답변은 간결하되 도움이 되도록 작성해주세요.
            
            관련 문서:
            %s
            """;

    private final LLMProvider llmProvider;
    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final VectorSearchService vectorSearchService;

    @Transactional
    public boolean tryAutoAnswer(Inquiry inquiry) {
        if (!llmProvider.isAvailable()) {
            log.warn("LLM 서비스 사용 불가 — 에스컬레이션으로 전환");
            return false;
        }

        try {
            List<String> contextDocs = vectorSearchService.searchAsContext(inquiry.getMessage(), RAG_CONTEXT_LIMIT);

            List<VectorSearchService.SearchResult> searchResults =
                    vectorSearchService.search(inquiry.getMessage(), RAG_CONTEXT_LIMIT, 0.7);
            double avgSimilarity = searchResults.stream()
                    .mapToDouble(VectorSearchService.SearchResult::similarity).average().orElse(0.0);

            String systemPrompt;
            if (!contextDocs.isEmpty()) {
                String docsContext = String.join("\n---\n", contextDocs);
                systemPrompt = RAG_SYSTEM_PROMPT.formatted(docsContext);
                log.info("문의 #{}: RAG 컨텍스트 {} 문서, 평균 유사도 {}",
                        inquiry.getId(), contextDocs.size(), String.format("%.2f", avgSimilarity));
            } else {
                systemPrompt = SYSTEM_PROMPT;
                log.info("문의 #{}: 관련 문서 없음 — 기본 프롬프트 사용", inquiry.getId());
            }

            LLMResponse response = llmProvider.chat(systemPrompt, inquiry.getMessage(), contextDocs);

            float finalConfidence = calculateConfidence(response.confidence(), avgSimilarity, !contextDocs.isEmpty());

            inquiry.setAiResponse(response.content());
            inquiry.setAiConfidence(finalConfidence);

            InquiryMessage aiMessage = InquiryMessage.builder()
                    .inquiry(inquiry).role(InquiryMessageRole.AI)
                    .message(response.content()).createdAt(LocalDateTime.now()).build();
            inquiryMessageRepository.save(aiMessage);

            if (finalConfidence >= CONFIDENCE_THRESHOLD) {
                inquiry.setStatus(InquiryStatus.AI_ANSWERED);
                inquiry.setResolvedBy(InquiryResolvedBy.AI);
                inquiryRepository.save(inquiry);
                log.info("문의 #{} AI 자동 답변 성공 (confidence: {}, docs: {})",
                        inquiry.getId(), finalConfidence, contextDocs.size());
                return true;
            } else {
                inquiryRepository.save(inquiry);
                log.info("문의 #{} AI confidence 부족 ({} < {}) — 에스컬레이션 필요",
                        inquiry.getId(), finalConfidence, CONFIDENCE_THRESHOLD);
                return false;
            }
        } catch (Exception e) {
            log.error("AI 답변 시도 중 오류 (문의 #{}): {}", inquiry.getId(), e.getMessage(), e);
            return false;
        }
    }

    private float calculateConfidence(float llmConfidence, double avgSimilarity, boolean hasContext) {
        if (!hasContext) return llmConfidence * 0.8f;
        return (float) (llmConfidence * 0.6 + avgSimilarity * 0.4);
    }
}
