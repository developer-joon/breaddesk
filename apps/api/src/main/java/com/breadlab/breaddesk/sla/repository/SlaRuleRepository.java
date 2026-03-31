package com.breadlab.breaddesk.sla.repository;

import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SlaRuleRepository extends JpaRepository<SlaRule, Long> {

    @Query("SELECT s FROM SlaRule s WHERE s.urgency = :urgency AND s.active = true")
    Optional<SlaRule> findActiveByUrgency(@Param("urgency") TaskUrgency urgency);
}
