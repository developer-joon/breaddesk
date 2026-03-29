package com.breadlab.breaddesk.template.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.common.dto.PageResponse;
import com.breadlab.breaddesk.template.dto.TemplateRequest;
import com.breadlab.breaddesk.template.dto.TemplateResponse;
import com.breadlab.breaddesk.template.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TemplateResponse> createTemplate(@Valid @RequestBody TemplateRequest request) {
        return ApiResponse.success("Template created successfully", templateService.createTemplate(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TemplateResponse> getTemplate(@PathVariable Long id) {
        return ApiResponse.success(templateService.getTemplate(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<TemplateResponse>> getTemplates(
            @RequestParam(required = false) String category,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TemplateResponse> templates = templateService.getTemplates(category, pageable);
        return ApiResponse.success(PageResponse.of(templates));
    }

    @PutMapping("/{id}")
    public ApiResponse<TemplateResponse> updateTemplate(
            @PathVariable Long id,
            @RequestBody TemplateRequest.Update request) {
        return ApiResponse.success("Template updated successfully", templateService.updateTemplate(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
    }

    @PostMapping("/{id}/render")
    public ApiResponse<String> renderTemplate(
            @PathVariable Long id,
            @RequestBody Map<String, String> variables) {
        String rendered = templateService.renderTemplate(id, variables);
        return ApiResponse.success(rendered);
    }
}
