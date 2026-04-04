package com.breadlab.breaddesk.automation.controller;

import com.breadlab.breaddesk.automation.dto.AutomationRuleRequest;
import com.breadlab.breaddesk.automation.dto.AutomationRuleResponse;
import com.breadlab.breaddesk.automation.service.AutomationRuleService;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/automation-rules")
@RequiredArgsConstructor
public class AutomationRuleController {

    private final AutomationRuleService automationRuleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AutomationRuleResponse>>> getAllRules() {
        List<AutomationRuleResponse> rules = automationRuleService.getAllRules();
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> getRuleById(@PathVariable Long id) {
        AutomationRuleResponse rule = automationRuleService.getRuleById(id);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> createRule(
            @RequestBody AutomationRuleRequest request) {
        AutomationRuleResponse response = automationRuleService.createRule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AutomationRuleResponse>> updateRule(
            @PathVariable Long id,
            @RequestBody AutomationRuleRequest request) {
        AutomationRuleResponse response = automationRuleService.updateRule(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleRule(
            @PathVariable Long id,
            @RequestParam Boolean active) {
        automationRuleService.toggleRule(id, active);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(@PathVariable Long id) {
        automationRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
