package com.breadlab.breaddesk.sla.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.sla.repository.SlaRuleRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskHold;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.repository.TaskHoldRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private SlaRuleRepository slaRuleRepository;

    @Mock
    private TaskHoldRepository taskHoldRepository;

    @InjectMocks
    private SlaService slaService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = TestDataFactory.createTask();
        task.setId(1L);
        task.setUrgency(TaskUrgency.CRITICAL);
    }

    @Test
    @DisplayName("should_calculateSlaDeadlines_when_ruleExists")
    void should_calculateSlaDeadlines_when_ruleExists() {
        // Given
        SlaRule rule = TestDataFactory.createSlaRule(TaskUrgency.CRITICAL);
        given(slaRuleRepository.findActiveByUrgency(TaskUrgency.CRITICAL))
                .willReturn(Optional.of(rule));

        // When
        slaService.calculateSlaDeadlines(task);

        // Then
        assertThat(task.getSlaResponseDeadline()).isNotNull();
        assertThat(task.getSlaResolveDeadline()).isNotNull();
    }

    @Test
    @DisplayName("should_notSetDeadlines_when_noRuleExists")
    void should_notSetDeadlines_when_noRuleExists() {
        // Given
        given(slaRuleRepository.findActiveByUrgency(TaskUrgency.CRITICAL))
                .willReturn(Optional.empty());

        // When
        slaService.calculateSlaDeadlines(task);

        // Then
        assertThat(task.getSlaResponseDeadline()).isNull();
        assertThat(task.getSlaResolveDeadline()).isNull();
    }

    @Test
    @DisplayName("should_detectResponseBreach_when_deadlinePassed")
    void should_detectResponseBreach_when_deadlinePassed() {
        // Given
        task.setSlaResponseDeadline(LocalDateTime.now().minusMinutes(10));
        task.setSlaRespondedAt(null);

        // When
        slaService.checkSlaBreaches(task);

        // Then
        assertThat(task.isSlaResponseBreached()).isTrue();
    }

    @Test
    @DisplayName("should_notBreachResponse_when_respondedInTime")
    void should_notBreachResponse_when_respondedInTime() {
        // Given
        task.setSlaResponseDeadline(LocalDateTime.now().plusMinutes(10));
        task.setSlaRespondedAt(null);

        // When
        slaService.checkSlaBreaches(task);

        // Then
        assertThat(task.isSlaResponseBreached()).isFalse();
    }

    @Test
    @DisplayName("should_detectResolveBreach_when_deadlinePassed")
    void should_detectResolveBreach_when_deadlinePassed() {
        // Given
        task.setSlaResolveDeadline(LocalDateTime.now().minusMinutes(10));
        task.setCompletedAt(null);

        // When
        slaService.checkSlaBreaches(task);

        // Then
        assertThat(task.isSlaResolveBreached()).isTrue();
    }

    @Test
    @DisplayName("should_adjustDeadlineForHolds_when_holdsExist")
    void should_adjustDeadlineForHolds_when_holdsExist() {
        // Given
        TaskHold hold = TestDataFactory.createTaskHold(task);
        hold.setSlaPausedMinutes(30);

        given(taskHoldRepository.findByTaskIdOrderByStartedAtDesc(1L))
                .willReturn(List.of(hold));

        LocalDateTime deadline = LocalDateTime.now().plusHours(2);

        // When
        LocalDateTime adjusted = slaService.adjustDeadlineForHolds(1L, deadline);

        // Then
        assertThat(adjusted).isAfter(deadline);
        assertThat(adjusted).isEqualTo(deadline.plusMinutes(30));
    }

    @Test
    @DisplayName("should_returnNull_when_deadlineIsNull")
    void should_returnNull_when_deadlineIsNull() {
        LocalDateTime result = slaService.adjustDeadlineForHolds(1L, null);

        assertThat(result).isNull();
    }
}
