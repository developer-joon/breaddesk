package com.breadlab.breaddesk.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskTransferRequest {

    @NotNull
    private Long toMemberId;

    @NotBlank
    private String reason;
}
