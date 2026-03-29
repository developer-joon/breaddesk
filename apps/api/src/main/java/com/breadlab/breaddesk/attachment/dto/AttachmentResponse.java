package com.breadlab.breaddesk.attachment.dto;

import com.breadlab.breaddesk.attachment.entity.Attachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private Attachment.EntityType entityType;
    private Long entityId;
    private String filename;
    private Long fileSize;
    private String mimeType;
    private Long uploadedBy;
    private LocalDateTime createdAt;

    public static AttachmentResponse from(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .entityType(attachment.getEntityType())
                .entityId(attachment.getEntityId())
                .filename(attachment.getFilename())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .uploadedBy(attachment.getUploadedBy())
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
