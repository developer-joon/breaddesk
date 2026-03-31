package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.TaskRelationType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRelationRequest {

    @NotNull
    private Long targetTaskId;

    @NotNull
    private TaskRelationType relationType;
}
