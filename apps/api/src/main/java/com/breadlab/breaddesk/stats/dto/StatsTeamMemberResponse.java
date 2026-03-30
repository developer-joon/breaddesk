package com.breadlab.breaddesk.stats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsTeamMemberResponse {
    private final Long memberId;
    private final String memberName;
    private final long assignedCount;
    private final long completedCount;
    private final double avgProcessingTimeHours;
}
