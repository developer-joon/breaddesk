package com.breadlab.breaddesk.template.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.auth.AuthUtils;
import com.breadlab.breaddesk.template.dto.ReplyTemplateRequest;
import com.breadlab.breaddesk.template.dto.ReplyTemplateResponse;
import com.breadlab.breaddesk.template.service.ReplyTemplateService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Tag(name = "Reply Template", description = "답변 템플릿 관리 API")
@RestController
@RequestMapping("/api/v1/reply-templates")
@RequiredArgsConstructor
public class ReplyTemplateController {

    private final ReplyTemplateService replyTemplateService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<ReplyTemplateResponse>> createTemplate(
            @Valid @RequestBody ReplyTemplateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long createdBy = authUtils.getMemberId(userDetails);
        ReplyTemplateResponse response = replyTemplateService.createTemplate(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReplyTemplateResponse>>> getAllTemplates(
            @RequestParam(required = false) String category,
            Pageable pageable) {
        Page<ReplyTemplateResponse> responses = category != null
                ? replyTemplateService.getTemplatesByCategory(category, pageable)
                : replyTemplateService.getAllTemplates(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReplyTemplateResponse>> getTemplateById(@PathVariable Long id) {
        ReplyTemplateResponse response = replyTemplateService.getTemplateById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReplyTemplateResponse>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody ReplyTemplateRequest request) {
        ReplyTemplateResponse response = replyTemplateService.updateTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse<String>> applyTemplate(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> variables) {
        String content = replyTemplateService.applyTemplate(id, variables);
        return ResponseEntity.ok(ApiResponse.success(content));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTemplate(@PathVariable Long id) {
        replyTemplateService.deleteTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
