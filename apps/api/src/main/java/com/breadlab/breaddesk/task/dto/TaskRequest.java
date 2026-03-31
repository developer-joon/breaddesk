package com.breadlab.breaddesk.task.dto;

import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

    @NotBlank
    @Size(max = 500)
    private String title;

    private String description;

    @Size(max = 50)
    private String type = "GENERAL";

    @NotNull
    private TaskUrgency urgency;

    private TaskStatus status;

    private String requesterName;
    private String requesterEmail;
    private Long assigneeId;
    private LocalDate dueDate;
    private Float estimatedHours;
    private Float actualHours;
}
