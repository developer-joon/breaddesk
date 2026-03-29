package com.breadlab.breaddesk.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskTagRequest {

    @NotBlank
    @Size(max = 100)
    private String tag;
}
