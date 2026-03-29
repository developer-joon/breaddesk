package com.breadlab.breaddesk.sla.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.sla.dto.SlaRuleResponse;
import com.breadlab.breaddesk.sla.dto.SlaRuleUpdateRequest;
import com.breadlab.breaddesk.sla.dto.SlaStatsResponse;
import com.breadlab.breaddesk.sla.service.SlaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sla")
@RequiredArgsConstructor
public class SlaController {

    private final SlaService slaService;

    @GetMapping("/rules")
    public ResponseEntity<ApiResponse<List<SlaRuleResponse>>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.success(slaService.getAllRules()));
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<ApiResponse<SlaRuleResponse>> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody SlaRuleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(slaService.updateRule(id, request)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<SlaStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.success(slaService.getStats()));
    }
}
