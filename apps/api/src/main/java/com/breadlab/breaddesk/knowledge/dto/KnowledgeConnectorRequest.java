package com.breadlab.breaddesk.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KnowledgeConnectorRequest(
        String name,
        @NotBlank String sourceType,
        @NotNull Object config,
        Integer syncIntervalMin
) {}
