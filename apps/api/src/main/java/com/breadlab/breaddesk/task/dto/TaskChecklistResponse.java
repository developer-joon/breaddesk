package com.breadlab.breaddesk.task.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskChecklistResponse {
    private final Long id;
    private final String itemText;
    private final boolean done;
    private final int sortOrder;
}
