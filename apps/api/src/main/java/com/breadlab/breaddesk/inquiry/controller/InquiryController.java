package com.breadlab.breaddesk.inquiry.controller;

import com.breadlab.breaddesk.ai.ConversationSummaryService;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.inquiry.dto.*;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import com.breadlab.breaddesk.inquiry.service.SimilarInquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inquiry", description = "고객 문의 관리 API")
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final SimilarInquiryService similarInquiryService;
    private final ConversationSummaryService conversationSummaryService;

    @Operation(summary = "문의 생성", description = "새로운 고객 문의를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(@Valid @RequestBody InquiryRequest request) {
        InquiryResponse response = inquiryService.createInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "문의 목록 조회", description = "모든 문의를 페이지네이션으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getAllInquiries(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teamId,
            Pageable pageable) {
        Page<InquiryResponse> responses = inquiryService.getAllInquiries(status, teamId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "문의 상세 조회", description = "특정 문의의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryById(@PathVariable Long id) {
        InquiryResponse response = inquiryService.getInquiryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "문의 상태 변경", description = "문의의 상태를 변경합니다.")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody InquiryStatusUpdateRequest request) {
        InquiryResponse response = inquiryService.updateInquiryStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "문의에 메시지 추가", description = "문의에 새로운 메시지를 추가합니다.")
    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<InquiryMessageResponse>> addMessage(
            @PathVariable Long id,
            @Valid @RequestBody InquiryMessageRequest request) {
        InquiryMessageResponse response = inquiryService.addMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "AI 태스크 생성 미리보기", description = "AI가 자동 생성한 태스크 내용을 미리 확인합니다.")
    @GetMapping("/{id}/generate-task-preview")
    public ResponseEntity<ApiResponse<TaskPreviewResponse>> generateTaskPreview(@PathVariable Long id) {
        TaskPreviewResponse response = inquiryService.generateTaskPreview(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "문의를 태스크로 전환", description = "문의를 태스크로 전환합니다.")
    @PostMapping("/{id}/convert-to-task")
    public ResponseEntity<ApiResponse<InquiryResponse>> convertToTask(
            @PathVariable Long id,
            @Valid @RequestBody ConvertToTaskRequest request) {
        InquiryResponse response = inquiryService.convertToTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "유사 문의 찾기", description = "벡터 검색으로 유사한 문의를 찾습니다.")
    @GetMapping("/{id}/similar")
    public ResponseEntity<ApiResponse<List<SimilarInquiryService.SimilarInquiry>>> getSimilarInquiries(
            @PathVariable Long id) {
        InquiryResponse inquiry = inquiryService.getInquiryById(id);
        var similar = similarInquiryService.findSimilar(inquiry.getMessage(), id);
        return ResponseEntity.ok(ApiResponse.success(similar));
    }

    @Operation(summary = "문의 대화 요약", description = "AI가 문의 대화를 요약합니다.")
    @GetMapping("/{id}/summary")
    public ResponseEntity<ApiResponse<String>> getSummary(@PathVariable Long id) {
        String summary = conversationSummaryService.generateSummary(id);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Operation(summary = "문의 삭제", description = "문의를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
