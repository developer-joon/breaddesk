package com.breadlab.breaddesk.inquiry.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.inquiry.dto.*;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(@Valid @RequestBody InquiryRequest request) {
        InquiryResponse response = inquiryService.createInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getAllInquiries(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teamId,
            Pageable pageable) {
        Page<InquiryResponse> responses = inquiryService.getAllInquiries(status, teamId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryById(@PathVariable Long id) {
        InquiryResponse response = inquiryService.getInquiryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody InquiryStatusUpdateRequest request) {
        InquiryResponse response = inquiryService.updateInquiryStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<InquiryMessageResponse>> addMessage(
            @PathVariable Long id,
            @Valid @RequestBody InquiryMessageRequest request) {
        InquiryMessageResponse response = inquiryService.addMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}/generate-task-preview")
    public ResponseEntity<ApiResponse<TaskPreviewResponse>> generateTaskPreview(@PathVariable Long id) {
        TaskPreviewResponse response = inquiryService.generateTaskPreview(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/convert-to-task")
    public ResponseEntity<ApiResponse<InquiryResponse>> convertToTask(
            @PathVariable Long id,
            @Valid @RequestBody ConvertToTaskRequest request) {
        InquiryResponse response = inquiryService.convertToTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
