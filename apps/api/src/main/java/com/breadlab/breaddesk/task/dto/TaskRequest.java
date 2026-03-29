package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Type is required")
    private String type;

    private Task.TaskUrgency urgency;

    private String requesterName;

    private String requesterEmail;

    private Long assigneeId;

    private LocalDate dueDate;

    private Double estimatedHours;

    private List<String> checklist;

    private List<String> tags;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private String title;
        private String description;
        private String type;
        private Task.TaskUrgency urgency;
        private Task.TaskStatus status;
        private Long assigneeId;
        private LocalDate dueDate;
        private Double estimatedHours;
        private Double actualHours;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Assign {
        @NotNull(message = "Assignee ID is required")
        private Long assigneeId;
    }
}
