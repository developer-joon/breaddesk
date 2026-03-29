package com.breadlab.breaddesk.sla.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.sla.dto.SlaRuleResponse;
import com.breadlab.breaddesk.sla.dto.SlaRuleUpdateRequest;
import com.breadlab.breaddesk.sla.dto.SlaStatsResponse;
import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.sla.repository.SlaRuleRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaService {

    private final SlaRuleRepository slaRuleRepository;
    private final TaskRepository taskRepository;

    public List<SlaRuleResponse> getAllRules() {
        return slaRuleRepository.findAll().stream()
                .map(this::toRuleResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SlaRuleResponse updateRule(Long id, SlaRuleUpdateRequest request) {
        SlaRule rule = slaRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SLA rule not found: " + id));

        rule.setResponseMinutes(request.getResponseMinutes());
        rule.setResolveMinutes(request.getResolveMinutes());
        if (request.getActive() != null) {
            rule.setActive(request.getActive());
        }
        return toRuleResponse(slaRuleRepository.save(rule));
    }

    public SlaStatsResponse getStats() {
        List<Task> tasksWithSla = taskRepository.findAllWithSlaDeadlines();

        long totalResponse = tasksWithSla.stream().filter(t -> t.getSlaResponseDeadline() != null).count();
        long totalResolve = tasksWithSla.stream().filter(t -> t.getSlaResolveDeadline() != null).count();
        long responseBreaches = tasksWithSla.stream().filter(Task::isSlaResponseBreached).count();
        long resolveBreaches = tasksWithSla.stream().filter(Task::isSlaResolveBreached).count();

        double responseCompliance = totalResponse > 0
                ? (1.0 - (double) responseBreaches / totalResponse) * 100 : 100.0;
        double resolveCompliance = totalResolve > 0
                ? (1.0 - (double) resolveBreaches / totalResolve) * 100 : 100.0;

        Map<String, Double> responseByUrgency = new HashMap<>();
        Map<String, Double> resolveByUrgency = new HashMap<>();

        for (TaskUrgency urgency : TaskUrgency.values()) {
            List<Task> byUrgency = tasksWithSla.stream()
                    .filter(t -> t.getUrgency() == urgency).toList();

            long ur = byUrgency.stream().filter(t -> t.getSlaResponseDeadline() != null).count();
            long urb = byUrgency.stream().filter(Task::isSlaResponseBreached).count();
            responseByUrgency.put(urgency.name(), ur > 0 ? (1.0 - (double) urb / ur) * 100 : 100.0);

            long uv = byUrgency.stream().filter(t -> t.getSlaResolveDeadline() != null).count();
            long uvb = byUrgency.stream().filter(Task::isSlaResolveBreached).count();
            resolveByUrgency.put(urgency.name(), uv > 0 ? (1.0 - (double) uvb / uv) * 100 : 100.0);
        }

        Double avgResponse = tasksWithSla.stream()
                .filter(t -> t.getSlaRespondedAt() != null && t.getCreatedAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getSlaRespondedAt()).toMinutes())
                .average().stream().mapToObj(d -> d).findFirst().orElse(null);

        Double avgResolve = tasksWithSla.stream()
                .filter(t -> t.getCompletedAt() != null && t.getCreatedAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getCompletedAt()).toMinutes())
                .average().stream().mapToObj(d -> d).findFirst().orElse(null);

        return SlaStatsResponse.builder()
                .overallResponseComplianceRate(responseCompliance)
                .overallResolveComplianceRate(resolveCompliance)
                .responseComplianceByUrgency(responseByUrgency)
                .resolveComplianceByUrgency(resolveByUrgency)
                .avgResponseMinutes(avgResponse)
                .avgResolveMinutes(avgResolve)
                .totalResponseBreaches(responseBreaches)
                .totalResolveBreaches(resolveBreaches)
                .build();
    }

    private SlaRuleResponse toRuleResponse(SlaRule rule) {
        return SlaRuleResponse.builder()
                .id(rule.getId())
                .urgency(rule.getUrgency())
                .responseMinutes(rule.getResponseMinutes())
                .resolveMinutes(rule.getResolveMinutes())
                .active(rule.isActive())
                .build();
    }
}
