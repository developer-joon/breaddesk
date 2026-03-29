package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.TaskStatus;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskKanbanResponse {
    private final TaskStatus status;
    private final List<TaskResponse> tasks;
}
