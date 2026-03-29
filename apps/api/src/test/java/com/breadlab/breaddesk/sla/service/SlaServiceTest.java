package com.breadlab.breaddesk.sla.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.sla.dto.SlaRuleResponse;
import com.breadlab.breaddesk.sla.dto.SlaStatsResponse;
import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.sla.repository.SlaRuleRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private SlaRuleRepository slaRuleRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SlaService slaService;

    @Test
    @DisplayName("should_returnAllRules")
    void should_returnAllRules() {
        // Given
        SlaRule rule = TestDataFactory.createSlaRule(TaskUrgency.CRITICAL);
        rule.setId(1L);
        given(slaRuleRepository.findAll()).willReturn(List.of(rule));

        // When
        List<SlaRuleResponse> rules = slaService.getAllRules();

        // Then
        assertThat(rules).hasSize(1);
        assertThat(rules.get(0).getUrgency()).isEqualTo(TaskUrgency.CRITICAL);
    }

    @Test
    @DisplayName("should_calculateStats_withNoTasks")
    void should_calculateStats_withNoTasks() {
        // Given
        given(taskRepository.findAllWithSlaDeadlines()).willReturn(List.of());

        // When
        SlaStatsResponse stats = slaService.getStats();

        // Then
        assertThat(stats.getOverallResponseComplianceRate()).isEqualTo(100.0);
        assertThat(stats.getOverallResolveComplianceRate()).isEqualTo(100.0);
        assertThat(stats.getTotalResponseBreaches()).isZero();
        assertThat(stats.getTotalResolveBreaches()).isZero();
    }

    @Test
    @DisplayName("should_calculateStats_withBreachedTasks")
    void should_calculateStats_withBreachedTasks() {
        // Given
        Task compliant = TestDataFactory.createTask();
        compliant.setSlaResponseDeadline(LocalDateTime.now().plusHours(1));
        compliant.setSlaResolveDeadline(LocalDateTime.now().plusHours(4));
        compliant.setSlaResponseBreached(false);
        compliant.setSlaResolveBreached(false);
        compliant.setUrgency(TaskUrgency.NORMAL);

        Task breached = TestDataFactory.createTask();
        breached.setSlaResponseDeadline(LocalDateTime.now().minusHours(1));
        breached.setSlaResolveDeadline(LocalDateTime.now().minusHours(1));
        breached.setSlaResponseBreached(true);
        breached.setSlaResolveBreached(true);
        breached.setUrgency(TaskUrgency.CRITICAL);

        given(taskRepository.findAllWithSlaDeadlines()).willReturn(List.of(compliant, breached));

        // When
        SlaStatsResponse stats = slaService.getStats();

        // Then
        assertThat(stats.getOverallResponseComplianceRate()).isEqualTo(50.0);
        assertThat(stats.getTotalResponseBreaches()).isEqualTo(1);
        assertThat(stats.getTotalResolveBreaches()).isEqualTo(1);
    }
}
