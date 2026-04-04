package com.breadlab.breaddesk.audit.repository;

import com.breadlab.breaddesk.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<AuditLog> findByEntityTypeOrderByCreatedAtDesc(String entityType, Pageable pageable);
    
    Page<AuditLog> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);
}
