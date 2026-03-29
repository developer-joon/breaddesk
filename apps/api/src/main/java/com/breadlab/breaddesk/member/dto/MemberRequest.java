package com.breadlab.breaddesk.member.dto;

import com.breadlab.breaddesk.member.entity.MemberRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequest {

    @NotBlank
    @Size(max = 100)
    private String name;

    @Email
    @NotBlank
    private String email;

    @Size(min = 8, max = 100)
    private String password;

    @NotNull
    private MemberRole role;

    private String skills;

    private Boolean active;
}
