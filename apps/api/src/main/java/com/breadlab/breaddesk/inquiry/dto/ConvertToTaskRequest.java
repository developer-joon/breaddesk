package com.breadlab.breaddesk.inquiry.dto;

import com.breadlab.breaddesk.task.entity.TaskUrgency;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvertToTaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private String type;

    private TaskUrgency urgency;

    private Long assigneeId;
}
