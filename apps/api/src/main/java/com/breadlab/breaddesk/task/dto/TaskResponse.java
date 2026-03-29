package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final String type;
    private final TaskUrgency urgency;
    private final TaskStatus status;
    private final String requesterName;
    private final String requesterEmail;
    private final Long assigneeId;
    private final String aiSummary;
    private final LocalDate dueDate;
    private final Float estimatedHours;
    private final Float actualHours;
    private final LocalDateTime slaResponseDeadline;
    private final LocalDateTime slaResolveDeadline;
    private final boolean slaResponseBreached;
    private final boolean slaResolveBreached;
    private final int transferCount;
    private final LocalDateTime createdAt;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final List<TaskChecklistResponse> checklists;
    private final List<TaskTagResponse> tags;
    private final List<TaskCommentResponse> comments;
    private final List<TaskLogResponse> logs;
}
