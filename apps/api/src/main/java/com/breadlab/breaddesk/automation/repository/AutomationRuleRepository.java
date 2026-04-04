package com.breadlab.breaddesk.automation.repository;

import com.breadlab.breaddesk.automation.entity.AutomationRule;
import com.breadlab.breaddesk.automation.entity.TriggerType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {
    
    List<AutomationRule> findByActiveAndTriggerType(Boolean active, TriggerType triggerType);
    
    List<AutomationRule> findByActive(Boolean active);
}
