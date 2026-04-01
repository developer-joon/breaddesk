package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI-powered assignee recommendation service
 * Recommends best agent for a task based on workload, skills, and performance
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentRecommendationService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final LLMProvider llmProvider;

    /**
     * Recommend best assignees for a task
     * 
     * @param taskId Task ID
     * @return List of recommended assignees with scores
     */
    public List<AssigneeRecommendation> recommendAssignees(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        List<Member> availableMembers = memberRepository.findAll();
        
        List<AssigneeRecommendation> recommendations = new ArrayList<>();

        for (Member member : availableMembers) {
            double score = calculateAssignmentScore(task, member);
            String reason = generateReason(task, member, score);
            
            recommendations.add(new AssigneeRecommendation(
                    member.getId(),
                    member.getName(),
                    member.getEmail(),
                    score,
                    reason
            ));
        }

        // Sort by score descending
        recommendations.sort(Comparator.comparingDouble(AssigneeRecommendation::score).reversed());

        // Return top 3
        return recommendations.stream().limit(3).collect(Collectors.toList());
    }

    /**
     * Calculate assignment score based on multiple factors
     */
    private double calculateAssignmentScore(Task task, Member member) {
        double score = 100.0;

        // Factor 1: Current workload (reduce score if overloaded)
        long currentTasks = taskRepository.findAll().stream()
                .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(member.getId()))
                .filter(t -> t.getStatus() != com.breadlab.breaddesk.task.entity.TaskStatus.DONE)
                .count();

        score -= (currentTasks * 10); // Penalty for each active task

        // Factor 2: Team match (bonus if same team)
        if (task.getTeam() != null && member.getTeam() != null && 
            task.getTeam().getId().equals(member.getTeam().getId())) {
            score += 20;
        }

        // Factor 3: Role match (simple heuristic - could use AI)
        if (member.getRole() != null && member.getRole().contains("MANAGER")) {
            score += 10; // Managers get slight boost
        }

        // Ensure score is positive
        return Math.max(score, 0);
    }

    /**
     * Generate human-readable reason for recommendation
     */
    private String generateReason(Task task, Member member, double score) {
        List<String> reasons = new ArrayList<>();

        // Count workload
        long workload = taskRepository.findAll().stream()
                .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(member.getId()))
                .filter(t -> t.getStatus() != com.breadlab.breaddesk.task.entity.TaskStatus.DONE)
                .count();

        if (workload == 0) {
            reasons.add("Currently available");
        } else if (workload < 3) {
            reasons.add("Light workload (" + workload + " tasks)");
        } else {
            reasons.add("Moderate workload (" + workload + " tasks)");
        }

        // Team match
        if (task.getTeam() != null && member.getTeam() != null && 
            task.getTeam().getId().equals(member.getTeam().getId())) {
            reasons.add("Same team");
        }

        // Role
        if (member.getRole() != null && member.getRole().contains("MANAGER")) {
            reasons.add("Experienced manager");
        }

        return String.join(" • ", reasons);
    }

    // DTO for recommendation response
    public record AssigneeRecommendation(
            Long memberId,
            String name,
            String email,
            double score,
            String reason
    ) {}
}
