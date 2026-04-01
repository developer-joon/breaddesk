package com.breadlab.breaddesk.analytics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * GET /api/v1/analytics/ai-performance
     * Returns AI auto-resolve rate and confidence metrics
     */
    @GetMapping("/ai-performance")
    public ResponseEntity<?> getAIPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Default to last 30 days if not specified
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        Map<String, Object> metrics = analyticsService.getAIPerformanceMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/v1/analytics/agent-productivity
     * Returns per-agent stats
     */
    @GetMapping("/agent-productivity")
    public ResponseEntity<?> getAgentProductivity(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        Map<String, Object> metrics = analyticsService.getAgentProductivityMetrics(startDate, endDate);
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/v1/analytics/weekly-report
     * Returns summary for the week
     */
    @GetMapping("/weekly-report")
    public ResponseEntity<?> getWeeklyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        
        if (weekStart == null) {
            // Default to start of current week (Monday)
            LocalDate today = LocalDate.now();
            weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        }

        Map<String, Object> report = analyticsService.getWeeklyReport(weekStart);
        return ResponseEntity.ok(report);
    }

    /**
     * GET /api/v1/analytics/export?format=csv
     * Export analytics as CSV
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportAnalytics(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        if ("csv".equalsIgnoreCase(format)) {
            return exportCSV(startDate, endDate);
        } else {
            return ResponseEntity.badRequest().body("Unsupported format: " + format);
        }
    }

    private ResponseEntity<?> exportCSV(LocalDate startDate, LocalDate endDate) {
        try {
            Map<String, Object> aiMetrics = analyticsService.getAIPerformanceMetrics(startDate, endDate);
            Map<String, Object> agentMetrics = analyticsService.getAgentProductivityMetrics(startDate, endDate);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(baos);

            // CSV header
            writer.println("Metric,Value");
            writer.println("Period Start," + startDate);
            writer.println("Period End," + endDate);
            writer.println();
            
            // AI metrics
            writer.println("AI Performance");
            writer.println("Total Inquiries," + aiMetrics.get("totalInquiries"));
            writer.println("AI Resolved Count," + aiMetrics.get("aiResolvedCount"));
            writer.println("Human Resolved Count," + aiMetrics.get("humanResolvedCount"));
            writer.println("Auto Resolve Rate %," + aiMetrics.get("autoResolveRate"));
            writer.println("Average Confidence," + aiMetrics.get("averageConfidence"));
            writer.println();

            // Agent stats
            writer.println("Agent Productivity");
            writer.println("Agent Name,Total Tasks,Completed Tasks,Avg Completion Hours");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> agentStats = (List<Map<String, Object>>) agentMetrics.get("agentStats");
            for (Map<String, Object> stats : agentStats) {
                writer.printf("%s,%s,%s,%s%n",
                        stats.get("agentName"),
                        stats.get("totalTasks"),
                        stats.get("completedTasks"),
                        stats.get("avgCompletionTimeHours")
                );
            }

            writer.flush();
            writer.close();

            return ResponseEntity.ok()
                    .header("Content-Type", "text/csv")
                    .header("Content-Disposition", "attachment; filename=analytics_" + startDate + "_" + endDate + ".csv")
                    .body(baos.toByteArray());

        } catch (Exception e) {
            log.error("Failed to export CSV: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Export failed");
        }
    }
}
