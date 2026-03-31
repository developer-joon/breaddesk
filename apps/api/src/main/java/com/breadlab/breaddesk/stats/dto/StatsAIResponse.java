package com.breadlab.breaddesk.stats.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StatsAIResponse {
    private final long totalAIAnswered;
    private final long autoResolvedCount;
    private final double autoResolvedRate;
    private final long escalatedCount;
    private final double escalatedRate;
    private final Map<String, Long> confidenceDistribution; // "HIGH(0.8+)", "MEDIUM(0.5-0.8)", "LOW(<0.5)"
}
