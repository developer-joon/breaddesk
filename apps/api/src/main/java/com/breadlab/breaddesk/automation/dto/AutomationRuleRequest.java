package com.breadlab.breaddesk.automation.dto;

import com.breadlab.breaddesk.automation.entity.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationRuleRequest {
    private String name;
    private Boolean active;
    private TriggerType triggerType;
    private String conditionJson;
    private String actionJson;
}
