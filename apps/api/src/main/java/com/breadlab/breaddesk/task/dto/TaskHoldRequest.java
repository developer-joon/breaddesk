package com.breadlab.breaddesk.task.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskHoldRequest {

    @NotBlank
    private String reason;
}
