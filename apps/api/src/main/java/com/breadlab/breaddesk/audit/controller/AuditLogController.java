package com.breadlab.breaddesk.audit.controller;

import com.breadlab.breaddesk.audit.dto.AuditLogResponse;
import com.breadlab.breaddesk.audit.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Get all audit logs (admin only)
     */
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogResponse> logs = auditLogService.getAllLogs(PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs by entity type
     */
    @GetMapping("/entity/{entityType}")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByEntityType(
            @PathVariable String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogResponse> logs = auditLogService.getLogsByEntityType(
            entityType, PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }

    /**
     * Get audit logs by member
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<Page<AuditLogResponse>> getLogsByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLogResponse> logs = auditLogService.getLogsByMember(
            memberId, PageRequest.of(page, size));
        return ResponseEntity.ok(logs);
    }
}
