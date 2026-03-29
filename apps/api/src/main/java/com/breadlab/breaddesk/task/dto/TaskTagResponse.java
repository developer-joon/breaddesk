package com.breadlab.breaddesk.task.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskTagResponse {
    private final Long id;
    private final String tag;
}
