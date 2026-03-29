package com.breadlab.breaddesk.task.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskLogResponse {
    private final Long id;
    private final String action;
    private final Long actorId;
    private final String details;
    private final LocalDateTime createdAt;
}
