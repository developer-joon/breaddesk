package com.breadlab.breaddesk.knowledge.dto;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import java.time.LocalDateTime;

public record KnowledgeConnectorResponse(
        Long id, String name, String sourceType, String config,
        Integer syncIntervalMin, LocalDateTime lastSyncedAt,
        boolean active, LocalDateTime createdAt
) {
    public static KnowledgeConnectorResponse from(KnowledgeConnectorEntity entity) {
        return new KnowledgeConnectorResponse(
                entity.getId(), entity.getName(), entity.getSourceType(),
                entity.getConfig(), entity.getSyncIntervalMin(),
                entity.getLastSyncedAt(), entity.isActive(), entity.getCreatedAt()
        );
    }
}
