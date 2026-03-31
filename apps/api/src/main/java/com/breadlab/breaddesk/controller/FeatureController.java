package com.breadlab.breaddesk.controller;

import com.breadlab.breaddesk.config.FeatureProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Features", description = "기능 플래그 관리 API")
@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureProperties featureProperties;

    @Operation(summary = "현재 활성화된 기능 목록 조회", description = "시스템에서 현재 활성화된 기능 플래그 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Boolean>> getFeatures() {
        Map<String, Boolean> features = new HashMap<>();
        features.put("kanbanTasks", featureProperties.isKanbanTasks());
        features.put("internalNotes", featureProperties.isInternalNotes());
        features.put("aiAssignment", featureProperties.isAiAssignment());
        features.put("jiraIntegration", featureProperties.isJiraIntegration());
        return ResponseEntity.ok(features);
    }
}
