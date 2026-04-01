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

/**
 * 통계 API 컨트롤러
 * 
 * <p>시스템 전체 통계, AI 성능, 팀원별 현황, 주간 리포트를 제공합니다.</p>
 * 
 * @author BreadDesk Team
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    /**
     * 전체 시스템 현황을 조회합니다.
     * 
     * @param from 조회 시작일 (optional, 기본값: 30일 전)
     * @param to 조회 종료일 (optional, 기본값: 오늘)
     * @return 전체 현황 통계
     */
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<StatsOverviewResponse>> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(statsService.getOverview(from, to)));
    }

    /**
     * AI 자동답변 성능 통계를 조회합니다.
     * 
     * @param from 조회 시작일 (optional)
     * @param to 조회 종료일 (optional)
     * @return AI 성능 통계
     */
    @GetMapping("/ai")
    public ResponseEntity<ApiResponse<StatsAIResponse>> getAIStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(statsService.getAIStats(from, to)));
    }

    /**
     * 팀원별 업무 처리 현황을 조회합니다.
     * 
     * @param from 조회 시작일 (optional)
     * @param to 조회 종료일 (optional)
     * @return 팀원별 통계 목록
     */
    @GetMapping("/team")
    public ResponseEntity<ApiResponse<List<StatsTeamMemberResponse>>> getTeamStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.success(statsService.getTeamStats(from, to)));
    }

    /**
     * 주간 리포트를 조회합니다.
     * 
     * @return 이번 주(월~일) 집계 리포트
     */
    @GetMapping("/weekly-report")
    public ResponseEntity<ApiResponse<WeeklyReportResponse>> getWeeklyReport() {
        return ResponseEntity.ok(ApiResponse.success(statsService.getWeeklyReport()));
    }
}
