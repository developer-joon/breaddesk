package com.breadlab.breaddesk.stats.service;

import com.breadlab.breaddesk.stats.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final EntityManager em;

    public StatsOverviewResponse getOverview(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        long totalInquiries = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE created_at BETWEEN ?1 AND ?2", start, end);
        long totalTasks = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE created_at BETWEEN ?1 AND ?2", start, end);
        long totalMembers = countScalarSimple(
                "SELECT COUNT(*) FROM members WHERE is_active = true");

        // AI success rate = AI resolved / total inquiries with AI response
        long aiAnswered = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE ai_response IS NOT NULL AND created_at BETWEEN ?1 AND ?2", start, end);
        long aiResolved = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE resolved_by = 'AI' AND created_at BETWEEN ?1 AND ?2", start, end);
        double aiResolutionRate = aiAnswered > 0 ? (double) aiResolved / aiAnswered * 100 : 0;

        // Avg response time (hours)
        Double avgResponseHours = doubleScalar(
                "SELECT AVG(EXTRACT(EPOCH FROM (sla_responded_at - created_at)) / 3600) " +
                        "FROM tasks WHERE sla_responded_at IS NOT NULL AND created_at BETWEEN ?1 AND ?2", start, end);

        // Avg resolve time (hours)
        Double avgResolveHours = doubleScalar(
                "SELECT AVG(EXTRACT(EPOCH FROM (completed_at - created_at)) / 3600) " +
                        "FROM tasks WHERE completed_at IS NOT NULL AND created_at BETWEEN ?1 AND ?2", start, end);

        // Inquiries by channel
        Map<String, Long> byChannel = mapScalar(
                "SELECT channel, COUNT(*) FROM inquiries WHERE created_at BETWEEN ?1 AND ?2 GROUP BY channel", start, end);

        // Tasks by urgency
        Map<String, Long> byUrgency = mapScalar(
                "SELECT urgency, COUNT(*) FROM tasks WHERE created_at BETWEEN ?1 AND ?2 GROUP BY urgency", start, end);

        // SLA compliance
        long slaTasksTotal = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE sla_response_deadline IS NOT NULL AND created_at BETWEEN ?1 AND ?2", start, end);
        long slaResponseBreached = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE sla_response_breached = true AND created_at BETWEEN ?1 AND ?2", start, end);
        long slaResolveBreached = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE sla_resolve_breached = true AND created_at BETWEEN ?1 AND ?2", start, end);

        double slaResponseRate = slaTasksTotal > 0 ? (double) (slaTasksTotal - slaResponseBreached) / slaTasksTotal * 100 : 100;
        double slaResolveRate = slaTasksTotal > 0 ? (double) (slaTasksTotal - slaResolveBreached) / slaTasksTotal * 100 : 100;

        return StatsOverviewResponse.builder()
                .totalInquiries(totalInquiries)
                .totalTasks(totalTasks)
                .totalMembers(totalMembers)
                .aiResolutionRate(aiResolutionRate / 100.0)
                .avgResponseTime((avgResponseHours != null ? avgResponseHours : 0) * 60)
                .avgResolveTime((avgResolveHours != null ? avgResolveHours : 0) * 60)
                .inquiriesByChannel(byChannel)
                .tasksByUrgency(byUrgency)
                .slaResponseComplianceRate(round2(slaResponseRate))
                .slaResolveComplianceRate(round2(slaResolveRate))
                .build();
    }

    public StatsAIResponse getAIStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        long totalAI = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE ai_response IS NOT NULL AND created_at BETWEEN ?1 AND ?2", start, end);
        long autoResolved = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE resolved_by = 'AI' AND created_at BETWEEN ?1 AND ?2", start, end);
        long escalated = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE status = 'ESCALATED' AND created_at BETWEEN ?1 AND ?2", start, end);

        // Confidence distribution
        long highConf = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE ai_confidence >= 0.8 AND created_at BETWEEN ?1 AND ?2", start, end);
        long medConf = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE ai_confidence >= 0.5 AND ai_confidence < 0.8 AND created_at BETWEEN ?1 AND ?2", start, end);
        long lowConf = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE ai_confidence IS NOT NULL AND ai_confidence < 0.5 AND created_at BETWEEN ?1 AND ?2", start, end);

        Map<String, Long> confDist = new LinkedHashMap<>();
        confDist.put("HIGH(0.8+)", highConf);
        confDist.put("MEDIUM(0.5-0.8)", medConf);
        confDist.put("LOW(<0.5)", lowConf);

        return StatsAIResponse.builder()
                .totalAIAnswered(totalAI)
                .autoResolvedCount(autoResolved)
                .autoResolvedRate(totalAI > 0 ? round2((double) autoResolved / totalAI * 100) : 0)
                .escalatedCount(escalated)
                .escalatedRate(totalAI > 0 ? round2((double) escalated / totalAI * 100) : 0)
                .confidenceDistribution(confDist)
                .build();
    }

    @SuppressWarnings("unchecked")
    public List<StatsTeamMemberResponse> getTeamStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        String sql = """
                SELECT m.id, m.name,
                       COUNT(t.id) AS assigned,
                       COUNT(CASE WHEN t.status = 'DONE' THEN 1 END) AS completed,
                       AVG(CASE WHEN t.completed_at IS NOT NULL
                           THEN EXTRACT(EPOCH FROM (t.completed_at - t.created_at)) / 3600
                           ELSE NULL END) AS avg_hours
                FROM members m
                LEFT JOIN tasks t ON t.assignee_id = m.id AND t.created_at BETWEEN ?1 AND ?2
                WHERE m.is_active = true
                GROUP BY m.id, m.name
                ORDER BY completed DESC
                """;

        Query query = em.createNativeQuery(sql);
        query.setParameter(1, start);
        query.setParameter(2, end);
        List<Object[]> rows = query.getResultList();

        return rows.stream().map(row -> StatsTeamMemberResponse.builder()
                .memberId(((Number) row[0]).longValue())
                .memberName((String) row[1])
                .assignedCount(((Number) row[2]).longValue())
                .completedCount(((Number) row[3]).longValue())
                .avgProcessingTimeHours(row[4] != null ? round2(((Number) row[4]).doubleValue()) : 0)
                .build()
        ).collect(Collectors.toList());
    }

    public WeeklyReportResponse getWeeklyReport() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekEnd.atTime(LocalTime.MAX);

        long newInquiries = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE created_at BETWEEN ?1 AND ?2", start, end);
        long resolvedInquiries = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE resolved_at BETWEEN ?1 AND ?2", start, end);
        long newTasks = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE created_at BETWEEN ?1 AND ?2", start, end);
        long completedTasks = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE completed_at BETWEEN ?1 AND ?2", start, end);

        long aiAnswered = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE ai_response IS NOT NULL AND created_at BETWEEN ?1 AND ?2", start, end);
        long aiResolved = countScalar(
                "SELECT COUNT(*) FROM inquiries WHERE resolved_by = 'AI' AND created_at BETWEEN ?1 AND ?2", start, end);
        double aiRate = aiAnswered > 0 ? (double) aiResolved / aiAnswered * 100 : 0;

        long slaTotal = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE sla_response_deadline IS NOT NULL AND created_at BETWEEN ?1 AND ?2", start, end);
        long slaBreached = countScalar(
                "SELECT COUNT(*) FROM tasks WHERE (sla_response_breached = true OR sla_resolve_breached = true) AND created_at BETWEEN ?1 AND ?2", start, end);
        double slaRate = slaTotal > 0 ? (double) (slaTotal - slaBreached) / slaTotal * 100 : 100;

        // Daily inquiry counts
        @SuppressWarnings("unchecked")
        List<Object[]> dailyRows = em.createNativeQuery(
                        "SELECT DATE(created_at) AS d, COUNT(*) FROM inquiries WHERE created_at BETWEEN ?1 AND ?2 GROUP BY d ORDER BY d")
                .setParameter(1, start).setParameter(2, end)
                .getResultList();

        Map<String, Long> dailyCounts = new LinkedHashMap<>();
        for (Object[] row : dailyRows) {
            dailyCounts.put(row[0].toString(), ((Number) row[1]).longValue());
        }

        List<StatsTeamMemberResponse> topPerformers = getTeamStats(weekStart, weekEnd).stream()
                .limit(5).collect(Collectors.toList());

        return WeeklyReportResponse.builder()
                .weekStart(weekStart)
                .weekEnd(weekEnd)
                .newInquiries(newInquiries)
                .resolvedInquiries(resolvedInquiries)
                .newTasks(newTasks)
                .completedTasks(completedTasks)
                .aiResolutionRate(round2(aiRate))
                .slaComplianceRate(round2(slaRate))
                .dailyInquiryCounts(dailyCounts)
                .topPerformers(topPerformers)
                .build();
    }

    // ── helpers ──

    private long countScalar(String sql, LocalDateTime start, LocalDateTime end) {
        Object result = em.createNativeQuery(sql)
                .setParameter(1, start).setParameter(2, end)
                .getSingleResult();
        return result != null ? ((Number) result).longValue() : 0;
    }

    private Double doubleScalar(String sql, LocalDateTime start, LocalDateTime end) {
        Object result = em.createNativeQuery(sql)
                .setParameter(1, start).setParameter(2, end)
                .getSingleResult();
        return result != null ? ((Number) result).doubleValue() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Long> mapScalar(String sql, LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter(1, start).setParameter(2, end)
                .getResultList();
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], ((Number) row[1]).longValue());
        }
        return map;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private long countScalarSimple(String sql) {
        Object result = em.createNativeQuery(sql).getSingleResult();
        return result != null ? ((Number) result).longValue() : 0;
    }
}
