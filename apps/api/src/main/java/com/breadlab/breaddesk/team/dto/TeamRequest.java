package com.breadlab.breaddesk.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamRequest {

    @NotBlank(message = "팀 이름은 필수입니다")
    @Size(max = 100, message = "팀 이름은 100자 이하여야 합니다")
    private String name;

    private String description;
}
