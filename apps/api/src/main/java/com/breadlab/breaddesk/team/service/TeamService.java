package com.breadlab.breaddesk.team.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.team.dto.*;
import com.breadlab.breaddesk.team.entity.Team;
import com.breadlab.breaddesk.team.entity.TeamMember;
import com.breadlab.breaddesk.team.entity.TeamMemberRole;
import com.breadlab.breaddesk.team.repository.TeamMemberRepository;
import com.breadlab.breaddesk.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(request.isActive())
                .build();

        Team saved = teamRepository.save(team);
        log.info("팀 생성: {}", saved.getName());
        return toResponse(saved);
    }

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TeamResponse> getActiveTeams() {
        return teamRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TeamResponse getTeamById(Long id) {
        Team team = findTeamOrThrow(id);
        return toResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = findTeamOrThrow(id);
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        team.setActive(request.isActive());
        
        return toResponse(teamRepository.save(team));
    }

    @Transactional
    public void deleteTeam(Long id) {
        Team team = findTeamOrThrow(id);
        teamRepository.delete(team);
        log.info("팀 삭제: {}", team.getName());
    }

    @Transactional
    public TeamMemberResponse addMember(Long teamId, AddTeamMemberRequest request) {
        Team team = findTeamOrThrow(teamId);
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + request.getMemberId()));

        // 이미 멤버인지 확인
        if (teamMemberRepository.existsByTeamIdAndMemberId(teamId, request.getMemberId())) {
            throw new IllegalStateException("Member already in team");
        }

        TeamMemberRole role = TeamMemberRole.valueOf(request.getRole().toUpperCase());

        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .member(member)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        TeamMember saved = teamMemberRepository.save(teamMember);
        log.info("팀 {} 에 멤버 {} 추가 (역할: {})", team.getName(), member.getName(), role);
        
        return toMemberResponse(saved);
    }

    @Transactional
    public void removeMember(Long teamId, Long memberId) {
        if (!teamMemberRepository.existsByTeamIdAndMemberId(teamId, memberId)) {
            throw new ResourceNotFoundException("Team member not found");
        }
        
        teamMemberRepository.deleteByTeamIdAndMemberId(teamId, memberId);
        log.info("팀 {} 에서 멤버 {} 제거", teamId, memberId);
    }

    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        return teamMemberRepository.findByTeamIdWithMember(teamId).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    public List<TeamResponse> getMemberTeams(Long memberId) {
        return teamMemberRepository.findByMemberId(memberId).stream()
                .map(tm -> toResponse(tm.getTeam()))
                .collect(Collectors.toList());
    }

    private Team findTeamOrThrow(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
    }

    private TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .active(team.isActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .memberCount(team.getMembers() != null ? team.getMembers().size() : 0)
                .build();
    }

    private TeamMemberResponse toMemberResponse(TeamMember teamMember) {
        return TeamMemberResponse.builder()
                .id(teamMember.getId())
                .memberId(teamMember.getMember().getId())
                .memberName(teamMember.getMember().getName())
                .memberEmail(teamMember.getMember().getEmail())
                .role(teamMember.getRole().name())
                .joinedAt(teamMember.getJoinedAt())
                .build();
    }
}
