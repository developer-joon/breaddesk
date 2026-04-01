package com.breadlab.breaddesk.team.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.team.dto.*;
import com.breadlab.breaddesk.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<ApiResponse<TeamResponse>> createTeam(@Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        List<TeamResponse> responses = activeOnly ? 
                teamService.getActiveTeams() : teamService.getAllTeams();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamById(@PathVariable Long id) {
        TeamResponse response = teamService.getTeamById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeamResponse>> updateTeam(
            @PathVariable Long id,
            @Valid @RequestBody TeamRequest request) {
        TeamResponse response = teamService.updateTeam(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    
    @PostMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<TeamMemberResponse>> addMember(
            @PathVariable Long teamId,
            @Valid @RequestBody AddTeamMemberRequest request) {
        TeamMemberResponse response = teamService.addMember(teamId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long memberId) {
        teamService.removeMember(teamId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    
    @GetMapping("/{teamId}/members")
    public ResponseEntity<ApiResponse<List<TeamMemberResponse>>> getTeamMembers(@PathVariable Long teamId) {
        List<TeamMemberResponse> responses = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    
    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getMemberTeams(@PathVariable Long memberId) {
        List<TeamResponse> responses = teamService.getMemberTeams(memberId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
