package com.breadlab.breaddesk.team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddTeamMemberRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    private String role = "MEMBER"; // LEADER or MEMBER
}
