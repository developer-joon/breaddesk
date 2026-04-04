package com.breadlab.breaddesk.audit.service;

import com.breadlab.breaddesk.audit.dto.AuditLogResponse;
import com.breadlab.breaddesk.audit.entity.AuditLog;
import com.breadlab.breaddesk.audit.repository.AuditLogRepository;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final MemberRepository memberRepository;

    /**
     * Log an audit event
     */
    @Transactional
    public void log(Long memberId, String action, String entityType, Long entityId, String details) {
        Member member = memberId != null 
            ? memberRepository.findById(memberId).orElse(null)
            : null;

        AuditLog auditLog = AuditLog.builder()
                .member(member)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .createdAt(LocalDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log: action={}, entity={}:{}, member={}", 
            action, entityType, entityId, memberId);
    }

    /**
     * Get all audit logs (paginated)
     */
    public Page<AuditLogResponse> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    /**
     * Get audit logs by entity type
     */
    public Page<AuditLogResponse> getLogsByEntityType(String entityType, Pageable pageable) {
        return auditLogRepository.findByEntityTypeOrderByCreatedAtDesc(entityType, pageable)
                .map(this::toResponse);
    }

    /**
     * Get audit logs by member
     */
    public Page<AuditLogResponse> getLogsByMember(Long memberId, Pageable pageable) {
        return auditLogRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(this::toResponse);
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .memberId(log.getMember() != null ? log.getMember().getId() : null)
                .memberName(log.getMember() != null ? log.getMember().getName() : "System")
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
