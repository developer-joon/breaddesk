package com.breadlab.breaddesk.team.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddTeamMemberRequest {

    @NotNull(message = "멤버 ID는 필수입니다")
    private Long memberId;

    private String role = "MEMBER"; // LEADER, MEMBER
}
