package com.breadlab.breaddesk.automation.dto;

import com.breadlab.breaddesk.automation.entity.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationRuleResponse {
    private Long id;
    private String name;
    private Boolean active;
    private TriggerType triggerType;
    private String conditionJson;
    private String actionJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
