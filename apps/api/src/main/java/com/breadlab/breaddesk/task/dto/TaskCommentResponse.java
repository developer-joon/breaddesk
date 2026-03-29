package com.breadlab.breaddesk.task.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskCommentResponse {
    private final Long id;
    private final Long authorId;
    private final String content;
    private final boolean internal;
    private final LocalDateTime createdAt;
}
