package com.breadlab.breaddesk.stats.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsOverviewResponse {
    private final long totalInquiries;
    private final long totalTasks;
    private final long totalMembers;
    private final double aiResolutionRate;
    private final double avgResponseTime;
    private final double avgResolveTime;
    private final Map<String, Long> inquiriesByChannel;
    private final Map<String, Long> tasksByUrgency;
    private final double slaResponseComplianceRate;
    private final double slaResolveComplianceRate;
}
