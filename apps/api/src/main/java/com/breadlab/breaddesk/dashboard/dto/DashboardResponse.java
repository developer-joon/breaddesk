package com.breadlab.breaddesk.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private long totalInquiries;
    private long unresolvedInquiries;
    private long todayInquiries;
    private double aiResolutionRate;
    private Map<String, Long> inquiriesByStatus;
    private Map<String, Long> tasksByStatus;
}
