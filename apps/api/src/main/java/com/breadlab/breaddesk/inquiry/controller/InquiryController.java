package com.breadlab.breaddesk.inquiry.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.inquiry.dto.*;
import com.breadlab.breaddesk.inquiry.service.*;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;
    private final SimilarInquiryService similarInquiryService;

    @PostMapping
    public ResponseEntity<ApiResponse<InquiryResponse>> createInquiry(@Valid @RequestBody InquiryRequest request) {
        InquiryResponse response = inquiryService.createInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InquiryResponse>>> getAllInquiries(Pageable pageable) {
        Page<InquiryResponse> responses = inquiryService.getAllInquiries(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InquiryResponse>> getInquiryById(@PathVariable Long id) {
        InquiryResponse response = inquiryService.getInquiryById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InquiryResponse>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody InquiryStatusUpdateRequest request) {
        InquiryResponse response = inquiryService.updateInquiryStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<InquiryMessageResponse>> addMessage(
            @PathVariable Long id, @Valid @RequestBody InquiryMessageRequest request) {
        InquiryMessageResponse response = inquiryService.addMessage(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/{id}/convert-to-task")
    public ResponseEntity<ApiResponse<InquiryResponse>> convertToTask(
            @PathVariable Long id, @Valid @RequestBody ConvertToTaskRequest request) {
        InquiryResponse response = inquiryService.convertToTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/similar")
    public ResponseEntity<ApiResponse<List<SimilarInquiryService.SimilarInquiry>>> getSimilarInquiries(
            @PathVariable Long id) {
        InquiryResponse inquiry = inquiryService.getInquiryById(id);
        var similar = similarInquiryService.findSimilar(inquiry.getMessage(), id);
        return ResponseEntity.ok(ApiResponse.success(similar));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInquiry(@PathVariable Long id) {
        inquiryService.deleteInquiry(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
