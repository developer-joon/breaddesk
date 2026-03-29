package com.breadlab.breaddesk.sla.dto;

import com.breadlab.breaddesk.task.entity.TaskUrgency;
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
public class SlaRuleResponse {

    private Long id;
    private TaskUrgency urgency;
    private int responseMinutes;
    private int resolveMinutes;
    private boolean active;
}
