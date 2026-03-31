package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * AI 자동 분류/라우팅 서비스
 * - 문의/업무 카테고리 자동 분류
 * - 긴급도 자동 판단
 * - 담당팀 자동 배정 추천
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIClassificationService {

    private final LLMProvider llmProvider;
    private final ObjectMapper objectMapper;

    private static final String CLASSIFICATION_PROMPT = """
            당신은 IT 서비스데스크 문의/업무를 분류하는 AI입니다.
            
            아래 카테고리 중 가장 적합한 것을 선택하세요:
            - DEVELOPMENT: 개발, 버그, 기능 개선, 코드 관련
            - ACCESS: 권한 요청, 계정, VPN, 접근 권한
            - INFRA: 서버, VM, 네트워크, 인프라 구성
            - FIREWALL: 방화벽, 포트 오픈, 통신 요청
            - DEPLOY: 배포, 릴리스, CI/CD
            - INCIDENT: 장애, 긴급 이슈, 시스템 다운
            - GENERAL: 기타 일반 문의
            
            긴급도 (URGENCY)를 판단하세요:
            - CRITICAL: 시스템 다운, 전사 영향 장애, 긴급 보안 이슈
            - HIGH: 주요 기능 불가, 다수 사용자 영향
            - NORMAL: 일반 요청, 작은 버그, 개선 요청
            - LOW: 단순 문의, 장기 개선 사항
            
            응답은 반드시 JSON 형식으로만 출력하세요 (설명 없이):
            {
              "category": "DEVELOPMENT",
              "urgency": "NORMAL",
              "reason": "개발 관련 버그 수정 요청이므로 DEVELOPMENT/NORMAL"
            }
            """;

    public record ClassificationResult(
            String category,
            TaskUrgency urgency,
            String reason
    ) {}

    /**
     * 문의 내용을 기반으로 카테고리/긴급도 자동 분류
     */
    public ClassificationResult classify(String message) {
        if (!llmProvider.isAvailable()) {
            log.warn("LLM 서비스 사용 불가 — 기본 분류 반환");
            return new ClassificationResult("GENERAL", TaskUrgency.NORMAL, "AI 서비스 미사용");
        }

        try {
            LLMResponse response = llmProvider.chat(CLASSIFICATION_PROMPT, message, List.of());
            String content = response.content().trim();

            // JSON 추출 (LLM이 추가 텍스트를 포함할 수 있으므로)
            String json = extractJson(content);
            if (json == null) {
                log.warn("LLM 응답이 JSON 형식이 아님: {}", content);
                return fallbackClassification(message);
            }

            ClassificationResponse parsed = objectMapper.readValue(json, ClassificationResponse.class);
            TaskUrgency urgency = parseUrgency(parsed.urgency());

            return new ClassificationResult(
                    parsed.category().toUpperCase(),
                    urgency,
                    parsed.reason()
            );
        } catch (Exception e) {
            log.error("AI 분류 중 오류: {}", e.getMessage(), e);
            return fallbackClassification(message);
        }
    }

    /**
     * 문의 자동 분류
     */
    public ClassificationResult classifyInquiry(Inquiry inquiry) {
        return classify(inquiry.getMessage());
    }

    /**
     * 업무 자동 분류
     */
    public ClassificationResult classifyTask(Task task) {
        String message = task.getTitle() + "\n" + (task.getDescription() != null ? task.getDescription() : "");
        return classify(message);
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
     * LLM 실패 시 키워드 기반 fallback 분류
     */
    private ClassificationResult fallbackClassification(String message) {
        String lower = message.toLowerCase();

        if (lower.contains("장애") || lower.contains("다운") || lower.contains("먹통") ||
                lower.contains("incident") || lower.contains("critical")) {
            return new ClassificationResult("INCIDENT", TaskUrgency.CRITICAL, "키워드 기반 장애 감지");
        }

        if (lower.contains("권한") || lower.contains("계정") || lower.contains("vpn") ||
                lower.contains("access") || lower.contains("로그인")) {
            return new ClassificationResult("ACCESS", TaskUrgency.NORMAL, "키워드 기반 권한 요청");
        }

        if (lower.contains("서버") || lower.contains("vm") || lower.contains("네트워크") ||
                lower.contains("infra") || lower.contains("인프라")) {
            return new ClassificationResult("INFRA", TaskUrgency.NORMAL, "키워드 기반 인프라");
        }

        if (lower.contains("방화벽") || lower.contains("포트") || lower.contains("firewall") ||
                lower.contains("통신")) {
            return new ClassificationResult("FIREWALL", TaskUrgency.NORMAL, "키워드 기반 방화벽");
        }

        if (lower.contains("배포") || lower.contains("릴리스") || lower.contains("deploy") ||
                lower.contains("cicd")) {
            return new ClassificationResult("DEPLOY", TaskUrgency.NORMAL, "키워드 기반 배포");
        }

        if (lower.contains("버그") || lower.contains("개발") || lower.contains("기능") ||
                lower.contains("bug") || lower.contains("feature")) {
            return new ClassificationResult("DEVELOPMENT", TaskUrgency.NORMAL, "키워드 기반 개발");
        }

        return new ClassificationResult("GENERAL", TaskUrgency.NORMAL, "기본 분류");
    }

    private TaskUrgency parseUrgency(String urgency) {
        if (urgency == null) return TaskUrgency.NORMAL;
        return switch (urgency.toUpperCase()) {
            case "CRITICAL" -> TaskUrgency.CRITICAL;
            case "HIGH" -> TaskUrgency.HIGH;
            case "LOW" -> TaskUrgency.LOW;
            default -> TaskUrgency.NORMAL;
        };
    }

    private record ClassificationResponse(
            String category,
            String urgency,
            String reason
    ) {}
}
