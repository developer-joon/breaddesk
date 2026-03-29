package com.breadlab.breaddesk.sla.service;

import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.sla.repository.SlaRuleRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.repository.TaskHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SlaService {

    private final SlaRuleRepository slaRuleRepository;
    private final TaskHoldRepository taskHoldRepository;

    public void calculateSlaDeadlines(Task task) {
        SlaRule rule = slaRuleRepository.findActiveByUrgency(task.getUrgency())
                .orElse(null);

        if (rule == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        task.setSlaResponseDeadline(now.plusMinutes(rule.getResponseMinutes()));
        task.setSlaResolveDeadline(now.plusMinutes(rule.getResolveMinutes()));
    }

    public void checkSlaBreaches(Task task) {
        LocalDateTime now = LocalDateTime.now();

        if (task.getSlaResponseDeadline() != null 
            && task.getSlaRespondedAt() == null 
            && now.isAfter(task.getSlaResponseDeadline())) {
            task.setSlaResponseBreached(true);
        }

        if (task.getSlaResolveDeadline() != null 
            && task.getCompletedAt() == null 
            && now.isAfter(task.getSlaResolveDeadline())) {
            task.setSlaResolveBreached(true);
        }
    }

    public LocalDateTime adjustDeadlineForHolds(Long taskId, LocalDateTime deadline) {
        if (deadline == null) {
            return null;
        }

        int totalHoldMinutes = taskHoldRepository.findByTaskIdOrderByStartedAtDesc(taskId).stream()
                .mapToInt(hold -> hold.getSlaPausedMinutes() != null ? hold.getSlaPausedMinutes() : 0)
                .sum();

        return deadline.plusMinutes(totalHoldMinutes);
    }
}
