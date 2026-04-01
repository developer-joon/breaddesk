package com.breadlab.breaddesk.analytics;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final InquiryRepository inquiryRepository;
    private final TaskRepository taskRepository;

    /**
     * AI Performance Analytics
     * - Auto-resolve rate
     * - Average confidence
     * - AI vs Human resolution comparison
     */
    public Map<String, Object> getAIPerformanceMetrics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Inquiry> inquiries = inquiryRepository.findAllByCreatedAtBetween(start, end);

        long totalInquiries = inquiries.size();
        long aiResolvedCount = inquiries.stream()
                .filter(i -> i.getAiResponse() != null && !i.getAiResponse().isEmpty())
                .filter(i -> i.getStatus() == InquiryStatus.RESOLVED || i.getStatus() == InquiryStatus.CLOSED)
                .count();

        long humanResolvedCount = inquiries.stream()
                .filter(i -> i.getResolvedBy() != null && !i.getResolvedBy().equalsIgnoreCase("AI"))
                .filter(i -> i.getStatus() == InquiryStatus.RESOLVED || i.getStatus() == InquiryStatus.CLOSED)
                .count();

        double autoResolveRate = totalInquiries > 0 ? (double) aiResolvedCount / totalInquiries * 100 : 0;

        // Average AI confidence
        double avgConfidence = inquiries.stream()
                .filter(i -> i.getAiResponse() != null)
                .mapToDouble(i -> i.getAiConfidence() != null ? i.getAiConfidence() : 0.0)
                .average()
                .orElse(0.0);

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("period", Map.of("start", startDate, "end", endDate));
        metrics.put("totalInquiries", totalInquiries);
        metrics.put("aiResolvedCount", aiResolvedCount);
        metrics.put("humanResolvedCount", humanResolvedCount);
        metrics.put("autoResolveRate", Math.round(autoResolveRate * 100.0) / 100.0);
        metrics.put("averageConfidence", Math.round(avgConfidence * 100.0) / 100.0);

        return metrics;
    }

    /**
     * Agent Productivity Analytics
     * - Tasks completed per agent
     * - Average resolution time
     * - Response time metrics
     */
    public Map<String, Object> getAgentProductivityMetrics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Task> tasks = taskRepository.findAllByCreatedAtBetween(start, end);
        List<Inquiry> inquiries = inquiryRepository.findAllByCreatedAtBetween(start, end);

        // Group tasks by assignee
        Map<String, List<Task>> tasksByAgent = tasks.stream()
                .filter(t -> t.getAssignee() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignee().getName()));

        List<Map<String, Object>> agentStats = new ArrayList<>();

        for (Map.Entry<String, List<Task>> entry : tasksByAgent.entrySet()) {
            String agentName = entry.getKey();
            List<Task> agentTasks = entry.getValue();

            long completedTasks = agentTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE)
                    .count();

            // Average completion time (in hours)
            double avgCompletionTime = agentTasks.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE && t.getCompletedAt() != null)
                    .mapToLong(t -> java.time.Duration.between(t.getCreatedAt(), t.getCompletedAt()).toHours())
                    .average()
                    .orElse(0.0);

            Map<String, Object> stats = new HashMap<>();
            stats.put("agentName", agentName);
            stats.put("totalTasks", agentTasks.size());
            stats.put("completedTasks", completedTasks);
            stats.put("avgCompletionTimeHours", Math.round(avgCompletionTime * 10.0) / 10.0);

            agentStats.add(stats);
        }

        // Sort by completed tasks descending
        agentStats.sort((a, b) -> Long.compare(
                (Long) b.get("completedTasks"),
                (Long) a.get("completedTasks")
        ));

        Map<String, Object> result = new HashMap<>();
        result.put("period", Map.of("start", startDate, "end", endDate));
        result.put("agentStats", agentStats);

        return result;
    }

    /**
     * Weekly Report Summary
     */
    public Map<String, Object> getWeeklyReport(LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekEnd.plusDays(1).atStartOfDay();

        List<Inquiry> inquiries = inquiryRepository.findAllByCreatedAtBetween(start, end);
        List<Task> tasks = taskRepository.findAllByCreatedAtBetween(start, end);

        long totalInquiries = inquiries.size();
        long resolvedInquiries = inquiries.stream()
                .filter(i -> i.getStatus() == InquiryStatus.RESOLVED || i.getStatus() == InquiryStatus.CLOSED)
                .count();

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE)
                .count();

        // Average resolution time
        double avgResolutionHours = inquiries.stream()
                .filter(i -> i.getResolvedAt() != null)
                .mapToLong(i -> java.time.Duration.between(i.getCreatedAt(), i.getResolvedAt()).toHours())
                .average()
                .orElse(0.0);

        Map<String, Object> report = new HashMap<>();
        report.put("weekStart", weekStart);
        report.put("weekEnd", weekEnd);
        report.put("totalInquiries", totalInquiries);
        report.put("resolvedInquiries", resolvedInquiries);
        report.put("totalTasks", totalTasks);
        report.put("completedTasks", completedTasks);
        report.put("avgResolutionHours", Math.round(avgResolutionHours * 10.0) / 10.0);

        return report;
    }
}
