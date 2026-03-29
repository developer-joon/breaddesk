package com.breadlab.breaddesk.attachment.repository;

import com.breadlab.breaddesk.attachment.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByEntityTypeAndEntityId(Attachment.EntityType entityType, Long entityId);
    long countByEntityTypeAndEntityId(Attachment.EntityType entityType, Long entityId);
}
