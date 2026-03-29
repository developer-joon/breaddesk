package com.breadlab.breaddesk.knowledge.dto;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import java.time.LocalDateTime;

public record KnowledgeDocumentResponse(
        Long id, String source, String sourceId, String title,
        String content, String url, Integer chunkIndex,
        LocalDateTime syncedAt, LocalDateTime createdAt
) {
    public static KnowledgeDocumentResponse from(KnowledgeDocumentEntity entity) {
        return new KnowledgeDocumentResponse(
                entity.getId(), entity.getSource(), entity.getSourceId(),
                entity.getTitle(), entity.getContent(), entity.getUrl(),
                entity.getChunkIndex(), entity.getSyncedAt(), entity.getCreatedAt()
        );
    }
}
