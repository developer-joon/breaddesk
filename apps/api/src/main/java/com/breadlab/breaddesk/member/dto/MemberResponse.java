package com.breadlab.breaddesk.member.dto;

import com.breadlab.breaddesk.member.entity.MemberRole;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final MemberRole role;
    private final String skills;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
