package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskChecklist;
import com.breadlab.breaddesk.task.entity.TaskTag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private String type;
    private Task.TaskUrgency urgency;
    private Task.TaskStatus status;
    private String requesterName;
    private String requesterEmail;
    private AssigneeInfo assignee;
    private Long inquiryId;
    private String aiSummary;
    private LocalDate dueDate;
    private Double estimatedHours;
    private Double actualHours;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<ChecklistItem> checklist;
    private List<String> tags;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssigneeInfo {
        private Long id;
        private String name;
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChecklistItem {
        private Long id;
        private String itemText;
        private Boolean isDone;
        private Integer sortOrder;

        public static ChecklistItem from(TaskChecklist checklist) {
            return ChecklistItem.builder()
                    .id(checklist.getId())
                    .itemText(checklist.getItemText())
                    .isDone(checklist.getIsDone())
                    .sortOrder(checklist.getSortOrder())
                    .build();
        }
    }

    public static TaskResponse from(Task task, List<TaskChecklist> checklists, List<TaskTag> tags) {
        TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .type(task.getType())
                .urgency(task.getUrgency())
                .status(task.getStatus())
                .requesterName(task.getRequesterName())
                .requesterEmail(task.getRequesterEmail())
                .inquiryId(task.getInquiryId())
                .aiSummary(task.getAiSummary())
                .dueDate(task.getDueDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt());

        if (task.getAssignee() != null) {
            builder.assignee(AssigneeInfo.builder()
                    .id(task.getAssignee().getId())
                    .name(task.getAssignee().getName())
                    .email(task.getAssignee().getEmail())
                    .build());
        }

        if (checklists != null) {
            builder.checklist(checklists.stream()
                    .map(ChecklistItem::from)
                    .toList());
        }

        if (tags != null) {
            builder.tags(tags.stream()
                    .map(TaskTag::getTag)
                    .toList());
        }

        return builder.build();
    }

    public static TaskResponse from(Task task) {
        return from(task, null, null);
    }
}
