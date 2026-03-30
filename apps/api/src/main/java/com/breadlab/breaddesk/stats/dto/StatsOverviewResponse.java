package com.breadlab.breaddesk.stats.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsOverviewResponse {
    private final long totalInquiries;
    private final long totalTasks;
    private final double aiSuccessRate;
    private final double avgResponseTimeHours;
    private final double avgResolveTimeHours;
    private final Map<String, Long> inquiriesByChannel;
    private final Map<String, Long> tasksByUrgency;
    private final double slaResponseComplianceRate;
    private final double slaResolveComplianceRate;
}
