package com.breadlab.breaddesk.knowledge.connector;

import java.time.Instant;
import java.util.List;

/**
 * 통일된 지식 문서 포맷
 */
public record KnowledgeDocument(
    String sourceId,
    String source,
    String title,
    String content,
    String url,
    List<String> tags,
    Instant updatedAt
) {}
