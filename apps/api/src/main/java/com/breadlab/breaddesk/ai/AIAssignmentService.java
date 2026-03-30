package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskTag;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AIAssignmentService {

    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Getter
    @Builder
    public static class AssigneeRecommendation {
        private final Long memberId;
        private final String memberName;
        private final double score;
        private final String reason;
    }

    public List<AssigneeRecommendation> recommendAssignees(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        // Extract keywords from task
        Set<String> keywords = extractKeywords(task);

        // Get active agents
        List<Member> agents = memberRepository.findByRoleAndActiveTrue(MemberRole.AGENT);
        List<Member> admins = memberRepository.findByRoleAndActiveTrue(MemberRole.ADMIN);
        List<Member> candidates = new ArrayList<>(agents);
        candidates.addAll(admins);

        if (candidates.isEmpty()) {
            return List.of();
        }

        // Count current active tasks per member
        Map<Long, Long> workloadMap = new HashMap<>();
        for (Member m : candidates) {
            long activeCount = taskRepository.search(null, null, null, m.getId(),
                    org.springframework.data.domain.Pageable.unpaged()).stream()
                    .filter(t -> t.getStatus() != TaskStatus.DONE)
                    .count();
            workloadMap.put(m.getId(), activeCount);
        }

        long maxWorkload = workloadMap.values().stream().mapToLong(Long::longValue).max().orElse(1);

        // Score each candidate
        List<AssigneeRecommendation> recommendations = new ArrayList<>();
        for (Member member : candidates) {
            double skillScore = calculateSkillMatch(member, keywords);
            double workloadScore = maxWorkload > 0
                    ? (1.0 - (double) workloadMap.getOrDefault(member.getId(), 0L) / (maxWorkload + 1)) * 50
                    : 50;
            double totalScore = skillScore + workloadScore;

            String reason = buildReason(skillScore, workloadMap.getOrDefault(member.getId(), 0L));

            recommendations.add(AssigneeRecommendation.builder()
                    .memberId(member.getId())
                    .memberName(member.getName())
                    .score(Math.round(totalScore * 100.0) / 100.0)
                    .reason(reason)
                    .build());
        }

        // Sort by score descending
        recommendations.sort(Comparator.comparingDouble(AssigneeRecommendation::getScore).reversed());
        return recommendations.stream().limit(5).collect(Collectors.toList());
    }

    private Set<String> extractKeywords(Task task) {
        Set<String> keywords = new HashSet<>();
        if (task.getTitle() != null) {
            keywords.addAll(Arrays.asList(task.getTitle().toLowerCase().split("\\s+")));
        }
        if (task.getDescription() != null) {
            keywords.addAll(Arrays.asList(task.getDescription().toLowerCase().split("\\s+")));
        }
        if (task.getTags() != null) {
            for (TaskTag tag : task.getTags()) {
                keywords.add(tag.getTag().toLowerCase());
            }
        }
        if (task.getType() != null) {
            keywords.add(task.getType().toLowerCase());
        }
        // Remove common short words
        keywords.removeIf(w -> w.length() < 2);
        return keywords;
    }

    private double calculateSkillMatch(Member member, Set<String> keywords) {
        if (member.getSkills() == null || member.getSkills().isBlank()) {
            return 0;
        }
        try {
            List<String> skills = objectMapper.readValue(member.getSkills(), new TypeReference<List<String>>() {});
            long matchCount = skills.stream()
                    .filter(skill -> keywords.stream().anyMatch(kw -> 
                            skill.toLowerCase().contains(kw) || kw.contains(skill.toLowerCase())))
                    .count();
            return Math.min(matchCount * 15.0, 50.0); // max 50 points from skills
        } catch (Exception e) {
            log.debug("Failed to parse skills for member {}: {}", member.getId(), e.getMessage());
            return 0;
        }
    }

    private String buildReason(double skillScore, long workload) {
        List<String> reasons = new ArrayList<>();
        if (skillScore >= 30) reasons.add("스킬 매칭 높음");
        else if (skillScore >= 15) reasons.add("스킬 부분 매칭");
        if (workload == 0) reasons.add("현재 여유");
        else if (workload <= 3) reasons.add("업무량 적음(" + workload + "건)");
        else reasons.add("업무량 " + workload + "건");
        return String.join(", ", reasons);
    }
}
