package com.breadlab.breaddesk.team.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TeamMemberResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String role;
    private LocalDateTime joinedAt;
}
