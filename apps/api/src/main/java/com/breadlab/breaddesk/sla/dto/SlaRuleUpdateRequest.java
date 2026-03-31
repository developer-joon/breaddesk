package com.breadlab.breaddesk.sla.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SlaRuleUpdateRequest {

    @NotNull
    @Min(1)
    private Integer responseMinutes;

    @NotNull
    @Min(1)
    private Integer resolveMinutes;

    private Boolean active;
}
