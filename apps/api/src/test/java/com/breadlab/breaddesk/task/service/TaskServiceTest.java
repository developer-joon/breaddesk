package com.breadlab.breaddesk.task.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.task.dto.*;
import com.breadlab.breaddesk.task.entity.*;
import com.breadlab.breaddesk.task.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskChecklistRepository taskChecklistRepository;
    @Mock private TaskTagRepository taskTagRepository;
    @Mock private TaskCommentRepository taskCommentRepository;
    @Mock private TaskLogRepository taskLogRepository;
    @Mock private TaskHoldRepository taskHoldRepository;
    @Mock private TaskTransferRepository taskTransferRepository;
    @Mock private MemberRepository memberRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private Member member;

    @BeforeEach
    void setUp() {
        task = TestDataFactory.createTask();
        task.setId(1L);
        member = TestDataFactory.createMember();
        member.setId(1L);
    }

    // ===== CRUD =====

    @Test
    @DisplayName("should_createTask_when_validRequest")
    void should_createTask_when_validRequest() {
        // Given
        TaskRequest request = TestDataFactory.createTaskRequest();
        given(taskRepository.save(any(Task.class))).willAnswer(inv -> {
            Task saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        // When
        TaskResponse response = taskService.createTask(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("New Task");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.WAITING);
    }

    @Test
    @DisplayName("should_createTaskWithAssignee_when_assigneeIdProvided")
    void should_createTaskWithAssignee_when_assigneeIdProvided() {
        // Given
        TaskRequest request = TestDataFactory.createTaskRequest();
        request.setAssigneeId(1L);
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(taskRepository.save(any(Task.class))).willAnswer(inv -> {
            Task saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        // When
        TaskResponse response = taskService.createTask(request);

        // Then
        assertThat(response.getAssigneeId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should_getAllTasks_when_called")
    void should_getAllTasks_when_called() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        given(taskRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(task)));

        // When
        Page<TaskResponse> page = taskService.getAllTasks(pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("should_getTaskById_when_exists")
    void should_getTaskById_when_exists() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        // When
        TaskResponse response = taskService.getTaskById(1L);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Task");
    }

    @Test
    @DisplayName("should_throwException_when_taskNotFound")
    void should_throwException_when_taskNotFound() {
        given(taskRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_updateTask_when_validRequest")
    void should_updateTask_when_validRequest() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        TaskRequest request = TestDataFactory.createTaskRequest();
        request.setTitle("Updated Title");

        // When
        TaskResponse response = taskService.updateTask(1L, request);

        // Then
        assertThat(response.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("should_deleteTask_when_exists")
    void should_deleteTask_when_exists() {
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    // ===== Status Changes =====

    @Test
    @DisplayName("should_updateStatus_when_validRequest")
    void should_updateStatus_when_validRequest() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        TaskStatusUpdateRequest request = TestDataFactory.createTaskStatusUpdateRequest(TaskStatus.IN_PROGRESS);

        // When
        TaskResponse response = taskService.updateTaskStatus(1L, request);

        // Then
        assertThat(response.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("should_setCompletedAt_when_statusIsDone")
    void should_setCompletedAt_when_statusIsDone() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        TaskStatusUpdateRequest request = TestDataFactory.createTaskStatusUpdateRequest(TaskStatus.DONE);

        // When
        taskService.updateTaskStatus(1L, request);

        // Then
        assertThat(task.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("should_notOverwriteStartedAt_when_alreadySet")
    void should_notOverwriteStartedAt_when_alreadySet() {
        // Given
        java.time.LocalDateTime originalStart = java.time.LocalDateTime.of(2025, 1, 1, 10, 0);
        task.setStartedAt(originalStart);
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        TaskStatusUpdateRequest request = TestDataFactory.createTaskStatusUpdateRequest(TaskStatus.IN_PROGRESS);

        // When
        taskService.updateTaskStatus(1L, request);

        // Then
        assertThat(task.getStartedAt()).isEqualTo(originalStart);
    }

    // ===== Kanban =====

    @Test
    @DisplayName("should_getKanbanView_when_called")
    void should_getKanbanView_when_called() {
        // Given
        Task waitingTask = TestDataFactory.createTask("Waiting");
        waitingTask.setId(1L);
        waitingTask.setStatus(TaskStatus.WAITING);

        Task inProgressTask = TestDataFactory.createTask("In Progress");
        inProgressTask.setId(2L);
        inProgressTask.setStatus(TaskStatus.IN_PROGRESS);

        given(taskRepository.findAll()).willReturn(List.of(waitingTask, inProgressTask));

        // When
        TaskKanbanResponse response = taskService.getKanbanView();

        // Then
        assertThat(response).isNotNull();
    }

    // ===== Checklist =====

    @Test
    @DisplayName("should_addChecklist_when_taskExists")
    void should_addChecklist_when_taskExists() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskChecklistRepository.save(any(TaskChecklist.class))).willAnswer(inv -> {
            TaskChecklist saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        TaskChecklistRequest request = TestDataFactory.createTaskChecklistRequest();

        // When
        TaskChecklistResponse response = taskService.addChecklist(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getItemText()).isEqualTo("New checklist item");
        assertThat(response.isDone()).isFalse();
    }

    @Test
    @DisplayName("should_updateChecklist_when_exists")
    void should_updateChecklist_when_exists() {
        // Given
        TaskChecklist checklist = TestDataFactory.createTaskChecklist(task);
        checklist.setId(1L);
        given(taskChecklistRepository.findById(1L)).willReturn(Optional.of(checklist));
        given(taskChecklistRepository.save(any(TaskChecklist.class))).willReturn(checklist);

        TaskChecklistRequest request = TestDataFactory.createTaskChecklistRequest();
        request.setDone(true);

        // When
        TaskChecklistResponse response = taskService.updateChecklist(1L, 1L, request);

        // Then
        assertThat(response.isDone()).isTrue();
    }

    @Test
    @DisplayName("should_deleteChecklist_when_called")
    void should_deleteChecklist_when_called() {
        taskService.deleteChecklist(1L, 1L);
        verify(taskChecklistRepository).deleteById(1L);
    }

    // ===== Tags =====

    @Test
    @DisplayName("should_addTag_when_taskExists")
    void should_addTag_when_taskExists() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskTagRepository.save(any(TaskTag.class))).willAnswer(inv -> {
            TaskTag saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        TaskTagRequest request = TestDataFactory.createTaskTagRequest();

        // When
        TaskTagResponse response = taskService.addTag(1L, request);

        // Then
        assertThat(response.getTag()).isEqualTo("feature");
    }

    @Test
    @DisplayName("should_deleteTag_when_called")
    void should_deleteTag_when_called() {
        taskService.deleteTag(1L, "bug");
        verify(taskTagRepository).deleteByTaskIdAndTag(1L, "bug");
    }

    // ===== Comments =====

    @Test
    @DisplayName("should_addComment_when_taskExists")
    void should_addComment_when_taskExists() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(taskCommentRepository.save(any(TaskComment.class))).willAnswer(inv -> {
            TaskComment saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        TaskCommentRequest request = TestDataFactory.createTaskCommentRequest();

        // When
        TaskCommentResponse response = taskService.addComment(1L, request, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("New comment");
    }

    @Test
    @DisplayName("should_getComments_when_called")
    void should_getComments_when_called() {
        // Given
        TaskComment comment = TestDataFactory.createTaskComment(task, member);
        comment.setId(1L);
        given(taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(1L))
                .willReturn(List.of(comment));

        // When
        List<TaskCommentResponse> responses = taskService.getComments(1L);

        // Then
        assertThat(responses).hasSize(1);
    }

    // ===== Hold / Resume =====

    @Test
    @DisplayName("should_holdTask_when_validRequest")
    void should_holdTask_when_validRequest() {
        // Given
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskHoldRepository.save(any(TaskHold.class))).willReturn(new TaskHold());
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        TaskHoldRequest request = TestDataFactory.createTaskHoldRequest();

        // When
        taskService.holdTask(1L, request);

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    @DisplayName("should_resumeTask_when_activeHoldExists")
    void should_resumeTask_when_activeHoldExists() {
        // Given
        TaskHold hold = TestDataFactory.createTaskHold(task);
        hold.setId(1L);
        task.setStatus(TaskStatus.PENDING);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskHoldRepository.findActiveHoldByTaskId(1L)).willReturn(Optional.of(hold));
        given(taskHoldRepository.save(any(TaskHold.class))).willReturn(hold);
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        // When
        taskService.resumeTask(1L);

        // Then
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(hold.getEndedAt()).isNotNull();
    }

    @Test
    @DisplayName("should_throwException_when_noActiveHold")
    void should_throwException_when_noActiveHold() {
        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(taskHoldRepository.findActiveHoldByTaskId(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.resumeTask(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ===== Transfer =====

    @Test
    @DisplayName("should_transferTask_when_validRequest")
    void should_transferTask_when_validRequest() {
        // Given
        Member toMember = TestDataFactory.createMember("New Agent", "new@test.com");
        toMember.setId(2L);
        task.setAssignee(member);

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(memberRepository.findById(2L)).willReturn(Optional.of(toMember));
        given(taskTransferRepository.save(any(TaskTransfer.class))).willReturn(new TaskTransfer());
        given(taskRepository.save(any(Task.class))).willReturn(task);
        given(taskLogRepository.save(any(TaskLog.class))).willReturn(new TaskLog());

        TaskTransferRequest request = TestDataFactory.createTaskTransferRequest(2L);

        // When
        taskService.transferTask(1L, request);

        // Then
        assertThat(task.getAssignee()).isEqualTo(toMember);
        assertThat(task.getTransferCount()).isEqualTo(1);
    }

    // ===== Logs =====

    @Test
    @DisplayName("should_getLogs_when_called")
    void should_getLogs_when_called() {
        // Given
        TaskLog log = TaskLog.builder()
                .id(1L).task(task).action("CREATED")
                .createdAt(java.time.LocalDateTime.now()).build();
        given(taskLogRepository.findByTaskIdOrderByCreatedAtDesc(1L)).willReturn(List.of(log));

        // When
        List<TaskLogResponse> responses = taskService.getLogs(1L);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAction()).isEqualTo("CREATED");
    }
}
