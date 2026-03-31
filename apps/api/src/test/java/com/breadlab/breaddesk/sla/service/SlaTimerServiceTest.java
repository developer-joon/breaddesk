package com.breadlab.breaddesk.sla.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.sla.repository.SlaRuleRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskHold;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.repository.TaskHoldRepository;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SlaTimerServiceTest {

    @Mock
    private SlaRuleRepository slaRuleRepository;

    @Mock
    private TaskHoldRepository taskHoldRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SlaTimerService slaTimerService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = TestDataFactory.createTask();
        task.setId(1L);
        task.setUrgency(TaskUrgency.CRITICAL);
        task.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("should set SLA deadlines when rule exists")
    void should_setSlaDeadlines_when_ruleExists() {
        SlaRule rule = TestDataFactory.createSlaRule(TaskUrgency.CRITICAL);
        given(slaRuleRepository.findActiveByUrgency(TaskUrgency.CRITICAL))
                .willReturn(Optional.of(rule));

        slaTimerService.startSla(task);

        assertThat(task.getSlaResponseDeadline()).isNotNull();
        assertThat(task.getSlaResolveDeadline()).isNotNull();
        assertThat(task.getSlaResponseDeadline()).isAfter(task.getCreatedAt());
    }

    @Test
    @DisplayName("should not set deadlines when no rule exists")
    void should_notSetDeadlines_when_noRule() {
        given(slaRuleRepository.findActiveByUrgency(TaskUrgency.CRITICAL))
                .willReturn(Optional.empty());

        slaTimerService.startSla(task);

        assertThat(task.getSlaResponseDeadline()).isNull();
        assertThat(task.getSlaResolveDeadline()).isNull();
    }

    @Test
    @DisplayName("should pause SLA and create hold record")
    void should_pauseSla() {
        given(taskHoldRepository.findActiveHoldByTaskId(1L)).willReturn(Optional.empty());

        slaTimerService.pauseSla(task, "Customer request");

        verify(taskHoldRepository).save(any(TaskHold.class));
    }

    @Test
    @DisplayName("should resume SLA and extend deadlines")
    void should_resumeSla() {
        LocalDateTime holdStart = LocalDateTime.now().minusMinutes(30);
        TaskHold hold = TaskHold.builder()
                .task(task)
                .reason("test")
                .startedAt(holdStart)
                .build();

        task.setSlaResponseDeadline(LocalDateTime.now().plusHours(1));
        task.setSlaResolveDeadline(LocalDateTime.now().plusHours(4));

        LocalDateTime originalResponseDeadline = task.getSlaResponseDeadline();

        given(taskHoldRepository.findActiveHoldByTaskId(1L)).willReturn(Optional.of(hold));

        slaTimerService.resumeSla(task);

        assertThat(hold.getEndedAt()).isNotNull();
        assertThat(hold.getSlaPausedMinutes()).isGreaterThanOrEqualTo(29); // ~30 min
        assertThat(task.getSlaResponseDeadline()).isAfter(originalResponseDeadline);
    }

    @Test
    @DisplayName("should record first response time")
    void should_recordFirstResponse() {
        assertThat(task.getSlaRespondedAt()).isNull();

        slaTimerService.recordResponse(task);

        assertThat(task.getSlaRespondedAt()).isNotNull();
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("should not overwrite existing response time")
    void should_notOverwriteResponse() {
        LocalDateTime existingResponse = LocalDateTime.now().minusHours(1);
        task.setSlaRespondedAt(existingResponse);

        slaTimerService.recordResponse(task);

        assertThat(task.getSlaRespondedAt()).isEqualTo(existingResponse);
    }
}
