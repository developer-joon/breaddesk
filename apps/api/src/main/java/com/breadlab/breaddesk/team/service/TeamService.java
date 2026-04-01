package com.breadlab.breaddesk.team.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.team.dto.*;
import com.breadlab.breaddesk.team.entity.Team;
import com.breadlab.breaddesk.team.entity.TeamMember;
import com.breadlab.breaddesk.team.repository.TeamMemberRepository;
import com.breadlab.breaddesk.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        Team team = Team.builder()
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();
        
        team = teamRepository.save(team);
        return toResponse(team);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
        return toResponse(team);
    }

    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + id));
        
        team.setName(request.getName());
        team.setDescription(request.getDescription());
        
        return toResponse(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found: " + id);
        }
        teamRepository.deleteById(id);
    }

    @Transactional
    public TeamMemberResponse addMember(Long teamId, AddTeamMemberRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
        
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + request.getMemberId()));
        
        // 중복 체크
        if (teamMemberRepository.findByTeamIdAndMemberId(teamId, request.getMemberId()).isPresent()) {
            throw new IllegalStateException("Member already in team");
        }
        
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .member(member)
                .role(request.getRole())
                .build();
        
        teamMember = teamMemberRepository.save(teamMember);
        return toMemberResponse(teamMember);
    }

    @Transactional
    public void removeMember(Long teamId, Long memberId) {
        if (!teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId).isPresent()) {
            throw new ResourceNotFoundException("TeamMember not found");
        }
        teamMemberRepository.deleteByTeamIdAndMemberId(teamId, memberId);
    }

    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        return teamMemberRepository.findByTeamId(teamId).stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    private TeamResponse toResponse(Team team) {
        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .active(team.isActive())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .members(team.getMembers().stream()
                        .map(this::toMemberResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private TeamMemberResponse toMemberResponse(TeamMember tm) {
        return TeamMemberResponse.builder()
                .id(tm.getId())
                .memberId(tm.getMember().getId())
                .memberName(tm.getMember().getName())
                .memberEmail(tm.getMember().getEmail())
                .role(tm.getRole())
                .joinedAt(tm.getJoinedAt())
                .build();
    }
}
