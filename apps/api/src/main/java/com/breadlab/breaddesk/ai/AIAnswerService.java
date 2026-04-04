package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.entity.InquiryResolvedBy;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.service.VectorSearchService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 자동 답변 서비스 (RAG 기반).
 * 문의 접수 시:
 * 1. 벡터 검색으로 관련 문서 3~5개 조회
 * 2. LLM에 질문 + 관련 문서 전달
 * 3. 응답 + confidence 저장
 * 4. confidence >= 0.7 이면 AI_ANSWERED, 아니면 에스컬레이션 대상
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnswerService {

    private static final float CONFIDENCE_THRESHOLD = 0.7f;
    private static final int CONTEXT_DOCS_LIMIT = 5;

    private static final String SYSTEM_PROMPT = """
            당신은 사내 IT 서비스데스크의 AI 어시스턴트입니다.
            임직원의 문의에 정중하고 전문적인 비즈니스 톤으로 한국어로 답변해주세요.
            
            **답변 규칙:**
            1. 참고 문서를 활용하여 정확한 답변을 제공하되, 문서에 없는 내용은 추측하지 마세요.
            2. 확실하지 않거나 민감한 사항(개인정보, 보안, 금융정보 등)은 담당자에게 전달하겠다고 안내하세요.
            3. 개인정보, 비밀번호, 금융정보, 민감한 사내 정보를 요청하지 마세요.
            4. 답변은 간결하되 필요한 정보는 충분히 제공하세요.
            5. 참고 문서 출처(URL)가 있다면 답변 끝에 "📎 관련 문서: [URL]" 형태로 안내하세요.
            6. 정중한 존댓말을 사용하세요 (예: ~입니다, ~해주세요).
            
            **예시 답변 톤:**
            "안녕하세요. 문의 주신 내용 확인했습니다. [답변 내용] 추가 문의사항이 있으시면 언제든 말씀해주세요."
            """;

    private final LLMProvider llmProvider;
    private final VectorSearchService vectorSearchService;
    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;

    /**
     * 문의에 대해 AI 답변을 시도합니다 (RAG 기반).
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
            // 1. 벡터 검색으로 관련 문서 조회
            List<Object[]> similarDocs = vectorSearchService.search(inquiry.getMessage(), CONTEXT_DOCS_LIMIT);
            List<String> contextDocuments = similarDocs.stream()
                    .map(row -> {
                        KnowledgeDocumentEntity doc = (KnowledgeDocumentEntity) row[0];
                        Double similarity = (Double) row[1];
                        String url = doc.getUrl() != null ? doc.getUrl() : "";
                        return String.format("%s\n%s\n(유사도: %.2f, 출처: %s)",
                                doc.getTitle(), doc.getContent(), similarity, url);
                    })
                    .collect(Collectors.toList());

            log.info("문의 #{}: 관련 문서 {}개 검색 완료", inquiry.getId(), contextDocuments.size());

            // 2. LLM에 질문 + 관련 문서 전달
            LLMResponse response = llmProvider.chat(
                    SYSTEM_PROMPT,
                    inquiry.getMessage(),
                    contextDocuments
            );

            // 3. AI 응답 저장
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

            // 4. confidence 기반 자동 답변 여부 결정
            if (response.confidence() >= CONFIDENCE_THRESHOLD) {
                inquiry.setStatus(InquiryStatus.AI_ANSWERED);
                inquiry.setResolvedBy(InquiryResolvedBy.AI);
                inquiryRepository.save(inquiry);
                log.info("문의 #{} AI 자동 답변 성공 (confidence: {}, 참고 문서: {}개)",
                        inquiry.getId(), response.confidence(), contextDocuments.size());
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

    /**
     * 상담원을 위한 AI 응답 추천 (Copilot)
     * 에스컬레이션된 문의에 대해 초안 제시
     */
    public LLMResponse suggestReply(Inquiry inquiry) {
        if (!llmProvider.isAvailable()) {
            return LLMResponse.of("AI 서비스를 사용할 수 없습니다.", 0.0f);
        }

        try {
            // 벡터 검색으로 관련 문서 + 과거 유사 문의의 답변 조회
            List<Object[]> similarDocs = vectorSearchService.search(inquiry.getMessage(), CONTEXT_DOCS_LIMIT);
            List<String> contextDocuments = similarDocs.stream()
                    .map(row -> {
                        KnowledgeDocumentEntity doc = (KnowledgeDocumentEntity) row[0];
                        return String.format("%s\n%s", doc.getTitle(), doc.getContent());
                    })
                    .collect(Collectors.toList());

            String copilotPrompt = """
                    당신은 상담원을 돕는 AI 어시스턴트입니다.
                    아래 문의에 대한 답변 초안을 작성해주세요.
                    
                    상담원이 검토하고 수정할 수 있도록, 정중하고 상세하게 작성해주세요.
                    참고 문서를 최대한 활용하되, 불확실한 부분은 [확인 필요] 표시를 해주세요.
                    """;

            return llmProvider.chat(copilotPrompt, inquiry.getMessage(), contextDocuments);
        } catch (Exception e) {
            log.error("AI 응답 추천 중 오류 (문의 #{}): {}", inquiry.getId(), e.getMessage(), e);
            return LLMResponse.of("AI 응답 추천에 실패했습니다.", 0.0f);
        }
    }

    /**
     * 답변 리라이트 (톤 조절)
     */
    public String rewriteReply(String originalReply, String tone) {
        if (!llmProvider.isAvailable()) {
            return originalReply;
        }

        try {
            String tonePrompt = switch (tone.toLowerCase()) {
                case "friendly" -> "아래 답변을 더 친근하고 따뜻한 톤으로 리라이트해주세요. 이모티콘을 적절히 활용하세요.";
                case "formal" -> "아래 답변을 공식적이고 격식있는 톤으로 리라이트해주세요.";
                case "concise" -> "아래 답변을 핵심만 간결하게 리라이트해주세요. 불필요한 설명은 제거하세요.";
                default -> "아래 답변을 더 나은 표현으로 리라이트해주세요.";
            };

            LLMResponse response = llmProvider.chat(tonePrompt, originalReply, List.of());
            return response.content();
        } catch (Exception e) {
            log.error("답변 리라이트 중 오류: {}", e.getMessage(), e);
            return originalReply;
        }
    }
}
