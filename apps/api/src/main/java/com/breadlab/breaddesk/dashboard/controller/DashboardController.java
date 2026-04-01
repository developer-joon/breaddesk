/**
 * 대시보드 API 컨트롤러
 * 
 * <p>대시보드 화면에 표시할 주요 지표를 제공합니다.</p>
 * 
 * @author BreadDesk Team
 * @since 0.1.0
 */
package com.breadlab.breaddesk.dashboard.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.dashboard.dto.DashboardResponse;
import com.breadlab.breaddesk.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Dashboard", description = "대시보드 API")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 요약 통계를 조회합니다.
     * 
     * @return 문의/업무 현황, AI 자동해결률 등
     */
    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse response = dashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
