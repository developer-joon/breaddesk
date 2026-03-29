package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.Task;
import jakarta.validation.constraints.*;
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
    @Size(min = 1, max = 500)
    private String title;

    @Size(max = 10000)
    private String description;

    @NotBlank(message = "Type is required")
    @Size(max = 50)
    private String type;

    private Task.TaskUrgency urgency;

    @Size(max = 100)
    private String requesterName;

    @Email(message = "Invalid email format")
    @Size(max = 200)
    private String requesterEmail;

    @Positive(message = "Assignee ID must be positive")
    private Long assigneeId;

    private LocalDate dueDate;

    @Positive(message = "Estimated hours must be positive")
    @Max(value = 999, message = "Estimated hours cannot exceed 999")
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
