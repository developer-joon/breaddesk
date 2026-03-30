package com.breadlab.breaddesk.stats.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeeklyReportResponse {
    private final LocalDate weekStart;
    private final LocalDate weekEnd;
    private final long newInquiries;
    private final long resolvedInquiries;
    private final long newTasks;
    private final long completedTasks;
    private final double aiResolutionRate;
    private final double slaComplianceRate;
    private final Map<String, Long> dailyInquiryCounts; // "2026-03-23" -> 5
    private final List<StatsTeamMemberResponse> topPerformers;
}
