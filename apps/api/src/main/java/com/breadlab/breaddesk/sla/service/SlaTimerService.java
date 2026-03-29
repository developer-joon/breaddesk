package com.breadlab.breaddesk.sla.service;

import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.sla.repository.SlaRuleRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskHold;
import com.breadlab.breaddesk.task.repository.TaskHoldRepository;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaTimerService {

    private final SlaRuleRepository slaRuleRepository;
    private final TaskHoldRepository taskHoldRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public void startSla(Task task) {
        SlaRule rule = slaRuleRepository.findActiveByUrgency(task.getUrgency())
                .orElse(null);
        if (rule == null) {
            log.debug("No active SLA rule for urgency={}, skipping", task.getUrgency());
            return;
        }

        LocalDateTime baseTime = task.getCreatedAt() != null ? task.getCreatedAt() : LocalDateTime.now();
        task.setSlaResponseDeadline(baseTime.plusMinutes(rule.getResponseMinutes()));
        task.setSlaResolveDeadline(baseTime.plusMinutes(rule.getResolveMinutes()));
        log.info("SLA started for task={}: response by {}, resolve by {}",
                task.getId(), task.getSlaResponseDeadline(), task.getSlaResolveDeadline());
    }

    @Transactional
    public void pauseSla(Task task, String reason) {
        if (taskHoldRepository.findActiveHoldByTaskId(task.getId()).isPresent()) {
            log.debug("Task {} already has an active hold, skipping pause", task.getId());
            return;
        }

        TaskHold hold = TaskHold.builder()
                .task(task)
                .reason(reason != null ? reason : "SLA paused")
                .startedAt(LocalDateTime.now())
                .build();
        taskHoldRepository.save(hold);
        log.info("SLA paused for task={}", task.getId());
    }

    @Transactional
    public void resumeSla(Task task) {
        TaskHold activeHold = taskHoldRepository.findActiveHoldByTaskId(task.getId())
                .orElse(null);
        if (activeHold == null) {
            log.debug("No active hold for task={}, skipping resume", task.getId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        activeHold.setEndedAt(now);
        int pausedMinutes = (int) Duration.between(activeHold.getStartedAt(), now).toMinutes();
        activeHold.setSlaPausedMinutes(pausedMinutes);
        taskHoldRepository.save(activeHold);

        if (task.getSlaResponseDeadline() != null && task.getSlaRespondedAt() == null) {
            task.setSlaResponseDeadline(task.getSlaResponseDeadline().plusMinutes(pausedMinutes));
        }
        if (task.getSlaResolveDeadline() != null && task.getCompletedAt() == null) {
            task.setSlaResolveDeadline(task.getSlaResolveDeadline().plusMinutes(pausedMinutes));
        }
        taskRepository.save(task);
        log.info("SLA resumed for task={}, paused {} minutes", task.getId(), pausedMinutes);
    }

    @Transactional
    public void recordResponse(Task task) {
        if (task.getSlaRespondedAt() != null) {
            return;
        }
        task.setSlaRespondedAt(LocalDateTime.now());
        taskRepository.save(task);
        log.info("SLA response recorded for task={}", task.getId());
    }
}
