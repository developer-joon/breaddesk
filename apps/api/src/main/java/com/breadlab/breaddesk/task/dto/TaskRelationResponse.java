package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.TaskRelationType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskRelationResponse {
    private final Long id;
    private final Long sourceTaskId;
    private final String sourceTaskTitle;
    private final Long targetTaskId;
    private final String targetTaskTitle;
    private final TaskRelationType relationType;
}
