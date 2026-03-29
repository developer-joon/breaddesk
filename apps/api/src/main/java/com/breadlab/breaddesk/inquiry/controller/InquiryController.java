package com.breadlab.breaddesk.inquiry.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.common.dto.PageResponse;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryResponse;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InquiryResponse> createInquiry(@Valid @RequestBody InquiryRequest request) {
        return ApiResponse.success("Inquiry received successfully", inquiryService.createInquiry(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<InquiryResponse> getInquiry(@PathVariable Long id) {
        return ApiResponse.success(inquiryService.getInquiry(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<InquiryResponse>> getInquiries(
            @RequestParam(required = false) Inquiry.InquiryStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InquiryResponse> inquiries = inquiryService.getInquiries(status, pageable);
        return ApiResponse.success(PageResponse.of(inquiries));
    }

    @PostMapping("/{id}/reply")
    public ApiResponse<InquiryResponse> replyToInquiry(
            @PathVariable Long id,
            @Valid @RequestBody InquiryRequest.Reply request) {
        return ApiResponse.success("Reply sent successfully", inquiryService.replyToInquiry(id, request));
    }

    @PostMapping("/{id}/feedback")
    public ApiResponse<InquiryResponse> submitFeedback(
            @PathVariable Long id,
            @RequestBody InquiryRequest.Feedback request) {
        return ApiResponse.success("Feedback received", inquiryService.submitFeedback(id, request));
    }
}
