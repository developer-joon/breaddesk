package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 기능 API 컨트롤러
 * - AI 응답 추천 (Copilot)
 * - 답변 리라이트
 * - 자동 분류
 * - AI 상태 확인
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI", description = "AI 기능 API")
public class AIController {

    private final LLMProvider llmProvider;
    private final AIAnswerService aiAnswerService;
    private final AIClassificationService aiClassificationService;
    private final AIAssignmentService aiAssignmentService;
    private final InquiryRepository inquiryRepository;
    private final TaskRepository taskRepository;

    @GetMapping("/status")
    @Operation(summary = "AI 서비스 상태 확인")
    public ResponseEntity<Map<String, Object>> getStatus() {
        boolean available = llmProvider.isAvailable();
        String modelName = available ? llmProvider.getModelName() : "N/A";

        return ResponseEntity.ok(Map.of(
                "available", available,
                "model", modelName,
                "message", available
                        ? "AI 서비스가 정상 동작 중입니다."
                        : "AI 서비스를 사용할 수 없습니다. LLM 설정을 확인하세요."
        ));
    }

    @PostMapping("/inquiries/{inquiryId}/suggest-reply")
    @Operation(summary = "AI 응답 추천 (Copilot)", description = "상담원을 위한 답변 초안 제시")
    public ResponseEntity<Map<String, Object>> suggestReply(@PathVariable Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));

        LLMResponse suggestion = aiAnswerService.suggestReply(inquiry);

        return ResponseEntity.ok(Map.of(
                "suggestion", suggestion.content(),
                "confidence", suggestion.confidence(),
                "metadata", suggestion.metadata()
        ));
    }

    @PostMapping("/rewrite")
    @Operation(summary = "답변 리라이트", description = "톤 조절: friendly, formal, concise")
    public ResponseEntity<Map<String, String>> rewriteReply(
            @RequestBody RewriteRequest request) {
        String rewritten = aiAnswerService.rewriteReply(request.originalReply(), request.tone());
        return ResponseEntity.ok(Map.of(
                "original", request.originalReply(),
                "rewritten", rewritten,
                "tone", request.tone()
        ));
    }

    @PostMapping("/classify/inquiry/{inquiryId}")
    @Operation(summary = "문의 자동 분류", description = "카테고리 + 긴급도 자동 판단")
    public ResponseEntity<AIClassificationService.ClassificationResult> classifyInquiry(
            @PathVariable Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + inquiryId));

        AIClassificationService.ClassificationResult result = aiClassificationService.classifyInquiry(inquiry);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/classify/task/{taskId}")
    @Operation(summary = "업무 자동 분류", description = "카테고리 + 긴급도 자동 판단")
    public ResponseEntity<AIClassificationService.ClassificationResult> classifyTask(
            @PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        AIClassificationService.ClassificationResult result = aiClassificationService.classifyTask(task);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/classify/text")
    @Operation(summary = "텍스트 분류", description = "임의 텍스트의 카테고리 + 긴급도 판단")
    public ResponseEntity<AIClassificationService.ClassificationResult> classifyText(
            @RequestBody ClassifyTextRequest request) {
        AIClassificationService.ClassificationResult result = aiClassificationService.classify(request.text());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tasks/{taskId}/recommend-assignees")
    @Operation(summary = "AI 담당자 추천", description = "스킬 매칭 + 업무량 기반 추천")
    public ResponseEntity<List<AIAssignmentService.AssigneeRecommendation>> recommendAssignees(
            @PathVariable Long taskId) {
        List<AIAssignmentService.AssigneeRecommendation> recommendations =
                aiAssignmentService.recommendAssignees(taskId);
        return ResponseEntity.ok(recommendations);
    }

    // DTOs
    private record RewriteRequest(String originalReply, String tone) {}
    private record ClassifyTextRequest(String text) {}
}
