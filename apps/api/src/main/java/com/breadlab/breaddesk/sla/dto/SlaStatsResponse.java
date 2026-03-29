package com.breadlab.breaddesk.sla.dto;

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
public class SlaStatsResponse {

    private double overallResponseComplianceRate;
    private double overallResolveComplianceRate;
    private Map<String, Double> responseComplianceByUrgency;
    private Map<String, Double> resolveComplianceByUrgency;
    private Double avgResponseMinutes;
    private Double avgResolveMinutes;
    private long totalResponseBreaches;
    private long totalResolveBreaches;
}
