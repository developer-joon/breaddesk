package com.breadlab.breaddesk.team.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.team.dto.*;
import com.breadlab.breaddesk.team.entity.Team;
import com.breadlab.breaddesk.team.entity.TeamMember;
import com.breadlab.breaddesk.team.repository.TeamMemberRepository;
import com.breadlab.breaddesk.team.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService 테스트")
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private TeamMemberRepository teamMemberRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private TeamService teamService;

    private Team testTeam;
    private Member testMember;
    private TeamMember testTeamMember;
    private TeamRequest testTeamRequest;

    @BeforeEach
    void setUp() {
        testTeam = new Team();
        testTeam.setId(1L);
        testTeam.setName("지원팀");
        testTeam.setDescription("고객 지원 담당");
        testTeam.setActive(true);
        testTeam.setCreatedAt(LocalDateTime.now());
        testTeam.setUpdatedAt(LocalDateTime.now());
        testTeam.setMembers(Arrays.asList());

        testMember = new Member();
        testMember.setId(1L);
        testMember.setName("홍길동");
        testMember.setEmail("hong@example.com");

        testTeamMember = new TeamMember();
        testTeamMember.setId(1L);
        testTeamMember.setTeam(testTeam);
        testTeamMember.setMember(testMember);
        testTeamMember.setRole("MEMBER");
        testTeamMember.setJoinedAt(LocalDateTime.now());

        testTeamRequest = new TeamRequest();
        testTeamRequest.setName("지원팀");
        testTeamRequest.setDescription("고객 지원 담당");
    }

    @Test
    @DisplayName("createTeam - 팀 생성 성공")
    void createTeam_shouldCreateSuccessfully() {
        // given
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team = invocation.getArgument(0);
            team.setId(1L);
            team.setCreatedAt(LocalDateTime.now());
            team.setUpdatedAt(LocalDateTime.now());
            return team;
        });

        // when
        TeamResponse response = teamService.createTeam(testTeamRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("지원팀");
        assertThat(response.getDescription()).isEqualTo("고객 지원 담당");
        assertThat(response.isActive()).isTrue();
        verify(teamRepository).save(any(Team.class));
    }

    @Test
    @DisplayName("getAllTeams - 모든 팀 조회")
    void getAllTeams_shouldReturnAllTeams() {
        // given
        Team team2 = new Team();
        team2.setId(2L);
        team2.setName("개발팀");
        team2.setDescription("개발 담당");
        team2.setActive(true);
        team2.setCreatedAt(LocalDateTime.now());
        team2.setUpdatedAt(LocalDateTime.now());
        team2.setMembers(Arrays.asList());

        when(teamRepository.findAll()).thenReturn(Arrays.asList(testTeam, team2));

        // when
        List<TeamResponse> responses = teamService.getAllTeams();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("지원팀");
        assertThat(responses.get(1).getName()).isEqualTo("개발팀");
        verify(teamRepository).findAll();
    }

    @Test
    @DisplayName("getTeamById - 팀 조회 성공")
    void getTeamById_shouldReturnTeam() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));

        // when
        TeamResponse response = teamService.getTeamById(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("지원팀");
        verify(teamRepository).findById(1L);
    }

    @Test
    @DisplayName("getTeamById - 팀 없을 시 예외 발생")
    void getTeamById_whenNotFound_shouldThrowException() {
        // given
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.getTeamById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team not found");
        verify(teamRepository).findById(999L);
    }

    @Test
    @DisplayName("updateTeam - 팀 수정 성공")
    void updateTeam_shouldUpdateSuccessfully() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));

        TeamRequest updateRequest = new TeamRequest();
        updateRequest.setName("신규 지원팀");
        updateRequest.setDescription("변경된 설명");

        // when
        TeamResponse response = teamService.updateTeam(1L, updateRequest);

        // then
        assertThat(response.getName()).isEqualTo("신규 지원팀");
        assertThat(response.getDescription()).isEqualTo("변경된 설명");
        verify(teamRepository).findById(1L);
    }

    @Test
    @DisplayName("updateTeam - 팀 없을 시 예외 발생")
    void updateTeam_whenNotFound_shouldThrowException() {
        // given
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.updateTeam(999L, testTeamRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team not found");
    }

    @Test
    @DisplayName("deleteTeam - 팀 삭제 성공")
    void deleteTeam_shouldDeleteSuccessfully() {
        // given
        when(teamRepository.existsById(1L)).thenReturn(true);

        // when
        teamService.deleteTeam(1L);

        // then
        verify(teamRepository).existsById(1L);
        verify(teamRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteTeam - 팀 없을 시 예외 발생")
    void deleteTeam_whenNotFound_shouldThrowException() {
        // given
        when(teamRepository.existsById(999L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> teamService.deleteTeam(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team not found");
        verify(teamRepository).existsById(999L);
        verify(teamRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("addMember - 멤버 추가 성공")
    void addMember_shouldAddMemberSuccessfully() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.findByTeamIdAndMemberId(1L, 1L)).thenReturn(Optional.empty());
        when(teamMemberRepository.save(any(TeamMember.class))).thenAnswer(invocation -> {
            TeamMember tm = invocation.getArgument(0);
            tm.setId(1L);
            tm.setJoinedAt(LocalDateTime.now());
            return tm;
        });

        AddTeamMemberRequest request = new AddTeamMemberRequest();
        request.setMemberId(1L);
        request.setRole("MEMBER");

        // when
        TeamMemberResponse response = teamService.addMember(1L, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMemberId()).isEqualTo(1L);
        assertThat(response.getMemberName()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo("MEMBER");
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    @Test
    @DisplayName("addMember - 이미 존재하는 멤버 시 예외 발생")
    void addMember_whenAlreadyExists_shouldThrowException() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(teamMemberRepository.findByTeamIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(testTeamMember));

        AddTeamMemberRequest request = new AddTeamMemberRequest();
        request.setMemberId(1L);
        request.setRole("MEMBER");

        // when & then
        assertThatThrownBy(() -> teamService.addMember(1L, request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Member already in team");
    }

    @Test
    @DisplayName("addMember - 팀 없을 시 예외 발생")
    void addMember_whenTeamNotFound_shouldThrowException() {
        // given
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        AddTeamMemberRequest request = new AddTeamMemberRequest();
        request.setMemberId(1L);
        request.setRole("MEMBER");

        // when & then
        assertThatThrownBy(() -> teamService.addMember(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Team not found");
    }

    @Test
    @DisplayName("addMember - 멤버 없을 시 예외 발생")
    void addMember_whenMemberNotFound_shouldThrowException() {
        // given
        when(teamRepository.findById(1L)).thenReturn(Optional.of(testTeam));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        AddTeamMemberRequest request = new AddTeamMemberRequest();
        request.setMemberId(999L);
        request.setRole("MEMBER");

        // when & then
        assertThatThrownBy(() -> teamService.addMember(1L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("removeMember - 멤버 제거 성공")
    void removeMember_shouldRemoveSuccessfully() {
        // given
        when(teamMemberRepository.findByTeamIdAndMemberId(1L, 1L))
                .thenReturn(Optional.of(testTeamMember));

        // when
        teamService.removeMember(1L, 1L);

        // then
        verify(teamMemberRepository).deleteByTeamIdAndMemberId(1L, 1L);
    }

    @Test
    @DisplayName("removeMember - 멤버 없을 시 예외 발생")
    void removeMember_whenNotFound_shouldThrowException() {
        // given
        when(teamMemberRepository.findByTeamIdAndMemberId(1L, 999L))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> teamService.removeMember(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("TeamMember not found");
    }

    @Test
    @DisplayName("getTeamMembers - 팀 멤버 조회")
    void getTeamMembers_shouldReturnMembers() {
        // given
        TeamMember tm2 = new TeamMember();
        tm2.setId(2L);
        tm2.setTeam(testTeam);
        Member member2 = new Member();
        member2.setId(2L);
        member2.setName("김철수");
        member2.setEmail("kim@example.com");
        tm2.setMember(member2);
        tm2.setRole("LEADER");
        tm2.setJoinedAt(LocalDateTime.now());

        when(teamMemberRepository.findByTeamId(1L))
                .thenReturn(Arrays.asList(testTeamMember, tm2));

        // when
        List<TeamMemberResponse> responses = teamService.getTeamMembers(1L);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getMemberName()).isEqualTo("홍길동");
        assertThat(responses.get(1).getMemberName()).isEqualTo("김철수");
        verify(teamMemberRepository).findByTeamId(1L);
    }
}
