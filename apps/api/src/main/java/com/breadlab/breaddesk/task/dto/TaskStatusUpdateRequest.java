package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateRequest {
    @NotNull private TaskStatus status;
}
