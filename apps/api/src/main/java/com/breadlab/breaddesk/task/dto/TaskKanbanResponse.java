package com.breadlab.breaddesk.task.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskKanbanResponse {
    private final List<TaskResponse> waiting;
    private final List<TaskResponse> inProgress;
    private final List<TaskResponse> pending;
    private final List<TaskResponse> review;
    private final List<TaskResponse> done;
}
