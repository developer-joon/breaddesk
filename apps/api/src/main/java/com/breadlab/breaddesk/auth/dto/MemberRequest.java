package com.breadlab.breaddesk.auth.dto;

import com.breadlab.breaddesk.auth.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private Member.MemberRole role;

    private Map<String, Double> skills;

    private Boolean isActive;

    public static class Update {
        private String name;
        private String password;
        private Member.MemberRole role;
        private Map<String, Double> skills;
        private Boolean isActive;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public Member.MemberRole getRole() { return role; }
        public void setRole(Member.MemberRole role) { this.role = role; }
        public Map<String, Double> getSkills() { return skills; }
        public void setSkills(Map<String, Double> skills) { this.skills = skills; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    }
}
