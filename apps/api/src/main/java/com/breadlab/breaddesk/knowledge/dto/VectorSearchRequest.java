package com.breadlab.breaddesk.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

public record VectorSearchRequest(
        @NotBlank String query,
        Integer limit,
        Double threshold
) {
    public int effectiveLimit() {
        return limit != null && limit > 0 ? limit : 5;
    }

    public double effectiveThreshold() {
        return threshold != null && threshold > 0 ? threshold : 0.7;
    }
}
