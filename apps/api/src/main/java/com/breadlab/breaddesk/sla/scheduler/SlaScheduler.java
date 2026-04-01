package com.breadlab.breaddesk.sla.scheduler;

import com.breadlab.breaddesk.sla.service.SlaCheckScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for SLA monitoring
 * Delegates to SlaCheckScheduler which has the actual scheduled methods
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlaScheduler {

    private final SlaCheckScheduler slaCheckScheduler;

    // Note: Actual scheduling is now handled by SlaCheckScheduler
    // This class kept for backward compatibility if needed
}
