package com.breadlab.breaddesk.team.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.team.dto.*;
import com.breadlab.breaddesk.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Team", description = "팀 관리 API")
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "팀 생성", description = "새로운 팀을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "모든 팀 조회", description = "모든 팀 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        List<TeamResponse> responses = activeOnly ? 
                teamService.getActiveTeams() : teamService.getAllTeams();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "팀 상세 조회", description = "특정 팀의 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        TeamResponse response = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "팀 수정", description = "팀 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "팀 삭제", description = "팀을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "팀 멤버 추가", description = "팀에 새로운 멤버를 추가합니다.")
    @PostMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> addMember(
            @PathVariable Long teamId,
            @Valid @RequestBody AddTeamMemberRequest request) {
        TeamMemberResponse response = teamService.addMember(teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "팀 멤버 제거", description = "팀에서 멤버를 제거합니다.")
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId) {
        teamService.removeMember(teamId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "팀 멤버 목록 조회", description = "특정 팀의 모든 멤버를 조회합니다.")
    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(@PathVariable Long teamId) {
        List<TeamMemberResponse> responses = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "멤버가 속한 팀 목록 조회", description = "특정 멤버가 속한 모든 팀을 조회합니다.")
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getMemberTeams(@PathVariable Long memberId) {
        List<TeamResponse> responses = teamService.getMemberTeams(memberId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
