package com.breadlab.breaddesk.attachment.repository;

import com.breadlab.breaddesk.attachment.entity.Attachment;
import com.breadlab.breaddesk.attachment.entity.AttachmentEntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    @Query("SELECT a FROM Attachment a WHERE a.entityType = :entityType AND a.entityId = :entityId ORDER BY a.createdAt DESC")
    List<Attachment> findByEntityTypeAndEntityId(@Param("entityType") AttachmentEntityType entityType, @Param("entityId") Long entityId);

    @Query("SELECT COUNT(a) FROM Attachment a WHERE a.entityType = :entityType AND a.entityId = :entityId")
    long countByEntityTypeAndEntityId(@Param("entityType") AttachmentEntityType entityType, @Param("entityId") Long entityId);
}
