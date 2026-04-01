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

    /**
     * Check and update SLA breach status for all active tasks
     * Should be called periodically (e.g., every 5 minutes by a scheduler)
     */
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();
        
        // Find all active tasks (not DONE)
        var activeTasks = taskRepository.findAll().stream()
                .filter(t -> t.getStatus() != com.breadlab.breaddesk.task.entity.TaskStatus.DONE)
                .toList();

        int responseBreached = 0;
        int resolveBreached = 0;

        for (Task task : activeTasks) {
            boolean changed = false;

            // Check response SLA breach
            if (!task.isSlaResponseBreached() && 
                task.getSlaResponseDeadline() != null && 
                task.getSlaRespondedAt() == null &&
                now.isAfter(task.getSlaResponseDeadline())) {
                
                task.setSlaResponseBreached(true);
                responseBreached++;
                changed = true;
                log.warn("Task #{} breached response SLA (deadline: {})", 
                        task.getId(), task.getSlaResponseDeadline());
            }

            // Check resolve SLA breach
            if (!task.isSlaResolveBreached() && 
                task.getSlaResolveDeadline() != null && 
                task.getCompletedAt() == null &&
                now.isAfter(task.getSlaResolveDeadline())) {
                
                task.setSlaResolveBreached(true);
                resolveBreached++;
                changed = true;
                log.warn("Task #{} breached resolve SLA (deadline: {})", 
                        task.getId(), task.getSlaResolveDeadline());
            }

            if (changed) {
                taskRepository.save(task);
            }
        }

        if (responseBreached > 0 || resolveBreached > 0) {
            log.info("SLA breach check completed: {} response breaches, {} resolve breaches", 
                    responseBreached, resolveBreached);
        }
    }

    /**
     * Get tasks approaching SLA deadlines (within 80% of deadline)
     */
    public java.util.List<Task> getTasksApproachingSla() {
        LocalDateTime now = LocalDateTime.now();
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() != com.breadlab.breaddesk.task.entity.TaskStatus.DONE)
                .filter(t -> {
                    if (t.getSlaResolveDeadline() != null && t.getCompletedAt() == null) {
                        Duration total = Duration.between(t.getCreatedAt(), t.getSlaResolveDeadline());
                        Duration elapsed = Duration.between(t.getCreatedAt(), now);
                        return elapsed.toMinutes() > total.toMinutes() * 0.8;
                    }
                    return false;
                })
                .toList();
    }
}
