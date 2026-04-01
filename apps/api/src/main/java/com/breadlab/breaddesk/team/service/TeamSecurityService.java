package com.breadlab.breaddesk.team.service;

import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.team.entity.TeamMember;
import com.breadlab.breaddesk.team.entity.TeamMemberRole;
import com.breadlab.breaddesk.team.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 팀 기반 보안 및 권한 체크 서비스
 * Spring Security @PreAuthorize에서 사용
 */
@Slf4j
@Service("teamSecurityService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamSecurityService {

    private final TeamMemberRepository teamMemberRepository;

    /**
     * 사용자가 특정 팀의 멤버인지 확인
     */
    public boolean isMemberOfTeam(Long teamId, Long memberId) {
        if (teamId == null || memberId == null) {
            return false;
        }
        return teamMemberRepository.existsByTeamIdAndMemberId(teamId, memberId);
    }

    /**
     * 사용자가 특정 팀의 리더인지 확인
     */
    public boolean isTeamLeader(Long teamId, Long memberId) {
        if (teamId == null || memberId == null) {
            return false;
        }
        Optional<TeamMember> teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId);
        return teamMember.isPresent() && teamMember.get().getRole() == TeamMemberRole.LEADER;
    }

    /**
     * 사용자가 관리자 권한을 가지고 있는지 확인
     * (Spring Security의 역할 체크와 연동)
     */
    public boolean isAdmin(String role) {
        return role != null && role.equals("ROLE_ADMIN");
    }

    /**
     * 사용자가 특정 팀에 대한 관리 권한이 있는지 확인
     * (관리자 또는 팀 리더)
     */
    public boolean canManageTeam(Long teamId, Long memberId, String role) {
        return isAdmin(role) || isTeamLeader(teamId, memberId);
    }
}
