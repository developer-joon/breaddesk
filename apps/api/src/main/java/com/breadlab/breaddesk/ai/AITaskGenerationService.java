package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 기반 태스크 자동 생성 서비스
 * 문의 내용을 분석하여 태스크 제목, 설명, 체크리스트를 자동 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AITaskGenerationService {

    private final LLMProvider llmProvider;
    private final ObjectMapper objectMapper;
    private final AIClassificationService classificationService;

    private static final String TASK_GENERATION_PROMPT = """
            당신은 고객 문의를 분석하여 실행 가능한 태스크(작업)로 변환하는 AI 어시스턴트입니다.
            
            아래 문의 내용을 읽고, 서비스 데스크 담당자가 처리할 수 있도록 명확한 태스크를 생성하세요.
            
            응답은 반드시 JSON 형식으로만 출력하세요 (설명 없이):
            {
              "title": "간결한 작업 제목 (5-10단어)",
              "description": "구체적인 문제 상황 및 배경 설명 (2-3 문장)",
              "checklist": [
                "첫 번째 실행 단계",
                "두 번째 실행 단계",
                "세 번째 실행 단계"
              ]
            }
            
            작성 원칙:
            - title: 액션 중심, 명확한 목적 (예: "VPN 접근 권한 부여", "배포 실패 원인 조사")
            - description: 요청자 정보, 문제 상황, 필요한 컨텍스트 포함
            - checklist: 3-5개의 구체적인 실행 단계 (확인 → 조치 → 검증 순서)
            
            문의 내용:
            """;

    public record GeneratedTaskData(
            String title,
            String description,
            List<String> checklist,
            String category,
            String urgency
    ) {}

    /**
     * 문의 내용을 기반으로 태스크 자동 생성
     */
    public GeneratedTaskData generateTaskFromInquiry(Inquiry inquiry) {
        if (!llmProvider.isAvailable()) {
            log.warn("LLM 서비스 사용 불가 — 기본 태스크 생성");
            return fallbackGeneration(inquiry);
        }

        try {
            // AI 분류 먼저 수행
            var classification = classificationService.classifyInquiry(inquiry);

            // 문의 컨텍스트 구성
            String inquiryContext = buildInquiryContext(inquiry);

            // LLM을 통한 태스크 생성
            LLMResponse response = llmProvider.chat(
                    TASK_GENERATION_PROMPT,
                    inquiryContext,
                    List.of()
            );

            String content = response.content().trim();
            String json = extractJson(content);

            if (json == null) {
                log.warn("LLM 응답이 JSON 형식이 아님: {}", content);
                return fallbackGeneration(inquiry);
            }

            GeneratedTask parsed = objectMapper.readValue(json, GeneratedTask.class);

            return new GeneratedTaskData(
                    parsed.title(),
                    parsed.description(),
                    parsed.checklist() != null ? parsed.checklist() : List.of(),
                    classification.category(),
                    classification.urgency().name()
            );

        } catch (Exception e) {
            log.error("AI 태스크 생성 중 오류: {}", e.getMessage(), e);
            return fallbackGeneration(inquiry);
        }
    }

    /**
     * 문의 컨텍스트 구성
     */
    private String buildInquiryContext(Inquiry inquiry) {
        StringBuilder context = new StringBuilder();
        context.append("요청자: ").append(inquiry.getSenderName());
        if (inquiry.getSenderEmail() != null) {
            context.append(" <").append(inquiry.getSenderEmail()).append(">");
        }
        context.append("\n");
        context.append("채널: ").append(inquiry.getChannel()).append("\n");
        context.append("메시지:\n").append(inquiry.getMessage()).append("\n");

        if (inquiry.getAiResponse() != null) {
            context.append("\n[AI 자동 답변 시도]\n").append(inquiry.getAiResponse()).append("\n");
            context.append("(신뢰도: ").append(inquiry.getAiConfidence()).append(")\n");
        }

        return context.toString();
    }

    /**
     * JSON 추출 (마크다운 코드 블록이나 추가 텍스트 제거)
     */
    private String extractJson(String text) {
        // ```json ... ``` 패턴 찾기
        int jsonStart = text.indexOf("```json");
        if (jsonStart != -1) {
            int jsonEnd = text.indexOf("```", jsonStart + 7);
            if (jsonEnd != -1) {
                return text.substring(jsonStart + 7, jsonEnd).trim();
            }
        }

        // ``` ... ``` 패턴 찾기
        jsonStart = text.indexOf("```");
        if (jsonStart != -1) {
            int jsonEnd = text.indexOf("```", jsonStart + 3);
            if (jsonEnd != -1) {
                return text.substring(jsonStart + 3, jsonEnd).trim();
            }
        }

        // { ... } 패턴 찾기
        int braceStart = text.indexOf("{");
        int braceEnd = text.lastIndexOf("}");
        if (braceStart != -1 && braceEnd != -1 && braceStart < braceEnd) {
            return text.substring(braceStart, braceEnd + 1).trim();
        }

        return null;
    }

    /**
     * LLM 실패 시 기본 태스크 생성
     */
    private GeneratedTaskData fallbackGeneration(Inquiry inquiry) {
        var classification = classificationService.classifyInquiry(inquiry);

        String title = generateFallbackTitle(inquiry);
        String description = generateFallbackDescription(inquiry);
        List<String> checklist = generateFallbackChecklist(classification.category());

        return new GeneratedTaskData(
                title,
                description,
                checklist,
                classification.category(),
                classification.urgency().name()
        );
    }

    private String generateFallbackTitle(Inquiry inquiry) {
        String message = inquiry.getMessage();
        if (message.length() > 50) {
            message = message.substring(0, 47) + "...";
        }
        return inquiry.getSenderName() + " 님의 " + message;
    }

    private String generateFallbackDescription(Inquiry inquiry) {
        return String.format(
                "요청자: %s <%s>\n채널: %s\n\n원본 문의:\n%s",
                inquiry.getSenderName(),
                inquiry.getSenderEmail() != null ? inquiry.getSenderEmail() : "N/A",
                inquiry.getChannel(),
                inquiry.getMessage()
        );
    }

    private List<String> generateFallbackChecklist(String category) {
        return switch (category) {
            case "DEVELOPMENT" -> List.of(
                    "요청 사항 재확인 및 상세 요구사항 파악",
                    "관련 코드/이슈 확인",
                    "개발 또는 수정 작업 진행",
                    "테스트 및 검증",
                    "요청자에게 결과 회신"
            );
            case "ACCESS" -> List.of(
                    "요청자 신원 확인",
                    "권한 요청 승인 여부 확인",
                    "계정/권한 설정",
                    "설정 완료 확인",
                    "요청자에게 접속 정보 전달"
            );
            case "INFRA" -> List.of(
                    "현재 인프라 상태 확인",
                    "요청 사항의 영향 범위 분석",
                    "작업 일정 및 방법 협의",
                    "인프라 설정 변경 또는 생성",
                    "변경 사항 검증 및 문서화"
            );
            case "INCIDENT" -> List.of(
                    "장애 범위 및 영향 파악",
                    "긴급 조치 또는 임시 복구",
                    "근본 원인 분석",
                    "영구 해결 방안 적용",
                    "재발 방지 대책 수립"
            );
            default -> List.of(
                    "요청 내용 검토",
                    "필요한 정보 확인",
                    "처리 방법 결정",
                    "작업 수행",
                    "요청자에게 완료 통보"
            );
        };
    }

    private record GeneratedTask(
            String title,
            String description,
            List<String> checklist
    ) {}
}
