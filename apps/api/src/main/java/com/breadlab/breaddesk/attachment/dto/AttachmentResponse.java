package com.breadlab.breaddesk.attachment.dto;

import com.breadlab.breaddesk.attachment.entity.AttachmentEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {

    private Long id;
    private AttachmentEntityType entityType;
    private Long entityId;
    private String filename;
    private String filePath;
    private long fileSize;
    private String mimeType;
    private Long uploadedBy;
    private LocalDateTime createdAt;
}
