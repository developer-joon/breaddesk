package com.breadlab.breaddesk.team.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamMemberResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String role;
    private LocalDateTime joinedAt;
}
