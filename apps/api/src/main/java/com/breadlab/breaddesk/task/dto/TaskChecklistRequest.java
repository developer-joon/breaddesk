package com.breadlab.breaddesk.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskChecklistRequest {

    @NotBlank
    @Size(max = 500)
    private String itemText;

    private boolean done;

    private Integer sortOrder;
}
