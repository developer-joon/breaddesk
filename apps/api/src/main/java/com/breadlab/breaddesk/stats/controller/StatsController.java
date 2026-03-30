package com.breadlab.breaddesk.stats.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.stats.dto.*;
import com.breadlab.breaddesk.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<StatsOverviewResponse>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(statsService.getOverview(from, to)));
    }

    @GetMapping("/ai")
    public ResponseEntity<ApiResponse<StatsAIResponse>> getAIStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(statsService.getAIStats(from, to)));
    }

    @GetMapping("/team")
    public ResponseEntity<ApiResponse<List<StatsTeamMemberResponse>>> getTeamStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(statsService.getTeamStats(from, to)));
    }

    @GetMapping("/weekly-report")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getWeeklyReport()));
    }
}
