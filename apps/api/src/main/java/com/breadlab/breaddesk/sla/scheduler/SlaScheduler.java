package com.breadlab.breaddesk.sla.scheduler;

import com.breadlab.breaddesk.sla.service.SlaTimerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for SLA monitoring
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaScheduler {

    private final SlaTimerService slaTimerService;

    /**
     * Check for SLA breaches every 5 minutes
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 60000) // 5 minutes
    public void checkSlaBreaches() {
        log.debug("Running SLA breach check...");
        try {
            slaTimerService.checkSlaBreaches();
        } catch (Exception e) {
            log.error("Error checking SLA breaches: {}", e.getMessage(), e);
        }
    }

    /**
     * Log tasks approaching SLA deadlines every 30 minutes
     */
    @Scheduled(fixedDelay = 1800000, initialDelay = 120000) // 30 minutes
    public void warnApproachingDeadlines() {
        try {
            var approaching = slaTimerService.getTasksApproachingSla();
            if (!approaching.isEmpty()) {
                log.warn("⚠️  {} tasks approaching SLA deadlines (>80% elapsed)", approaching.size());
                approaching.forEach(task -> 
                    log.warn("  - Task #{}: {} (deadline: {})", 
                        task.getId(), task.getTitle(), task.getSlaResolveDeadline())
                );
            }
        } catch (Exception e) {
            log.error("Error checking approaching SLA deadlines: {}", e.getMessage(), e);
        }
    }
}
