package com.breadlab.breaddesk.task.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.task.dto.*;
import com.breadlab.breaddesk.task.entity.*;
import com.breadlab.breaddesk.task.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskChecklistRepository taskChecklistRepository;
    private final TaskTagRepository taskTagRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskLogRepository taskLogRepository;
    private final TaskHoldRepository taskHoldRepository;
    private final TaskTransferRepository taskTransferRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType() != null ? request.getType() : "GENERAL")
                .urgency(request.getUrgency() != null ? request.getUrgency() : TaskUrgency.NORMAL)
                .status(TaskStatus.WAITING)
                .requesterName(request.getRequesterName())
                .requesterEmail(request.getRequesterEmail())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .createdAt(LocalDateTime.now())
                .transferCount(0)
                .slaResponseBreached(false)
                .slaResolveBreached(false)
                .build();

        if (request.getAssigneeId() != null) {
            task.setAssignee(memberRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found")));
        }

        Task saved = taskRepository.save(task);
        logAction(saved.getId(), "CREATED", null, null);
        return toResponse(saved);
    }

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable).map(this::toResponse);
    }

    public TaskResponse getTaskById(Long id) {
        Task task = findTaskOrThrow(id);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setType(request.getType());
        task.setUrgency(request.getUrgency());
        task.setDueDate(request.getDueDate());
        task.setEstimatedHours(request.getEstimatedHours());

        if (request.getAssigneeId() != null) {
            task.setAssignee(memberRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found")));
        }

        Task saved = taskRepository.save(task);
        logAction(id, "UPDATED", null, null);
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, TaskStatusUpdateRequest request) {
        Task task = findTaskOrThrow(id);
        TaskStatus oldStatus = task.getStatus();
        task.setStatus(request.getStatus());

        if (request.getStatus() == TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
            task.setStartedAt(LocalDateTime.now());
        }

        if (request.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        Task saved = taskRepository.save(task);
        logAction(id, "STATUS_CHANGED", null, Map.of("from", oldStatus.name(), "to", request.getStatus().name()));
        return toResponse(saved);
    }

    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    public TaskKanbanResponse getKanbanView() {
        List<Task> tasks = taskRepository.findAll();
        
        Map<TaskStatus, List<TaskResponse>> grouped = tasks.stream()
                .collect(Collectors.groupingBy(
                        Task::getStatus,
                        Collectors.mapping(this::toResponse, Collectors.toList())
                ));

        return TaskKanbanResponse.builder()
                .waiting(grouped.getOrDefault(TaskStatus.WAITING, List.of()))
                .inProgress(grouped.getOrDefault(TaskStatus.IN_PROGRESS, List.of()))
                .pending(grouped.getOrDefault(TaskStatus.PENDING, List.of()))
                .review(grouped.getOrDefault(TaskStatus.REVIEW, List.of()))
                .done(grouped.getOrDefault(TaskStatus.DONE, List.of()))
                .build();
    }

    @Transactional
    public TaskChecklistResponse addChecklist(Long taskId, TaskChecklistRequest request) {
        Task task = findTaskOrThrow(taskId);
        
        TaskChecklist checklist = TaskChecklist.builder()
                .task(task)
                .itemText(request.getItemText())
                .done(false)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        TaskChecklist saved = taskChecklistRepository.save(checklist);
        return toChecklistResponse(saved);
    }

    @Transactional
    public TaskChecklistResponse updateChecklist(Long taskId, Long checklistId, TaskChecklistRequest request) {
        TaskChecklist checklist = taskChecklistRepository.findById(checklistId)
                .orElseThrow(() -> new ResourceNotFoundException("Checklist not found"));

        checklist.setItemText(request.getItemText());
        checklist.setDone(request.isDone());
        checklist.setSortOrder(request.getSortOrder());

        return toChecklistResponse(taskChecklistRepository.save(checklist));
    }

    @Transactional
    public void deleteChecklist(Long taskId, Long checklistId) {
        taskChecklistRepository.deleteById(checklistId);
    }

    @Transactional
    public TaskTagResponse addTag(Long taskId, TaskTagRequest request) {
        Task task = findTaskOrThrow(taskId);
        
        TaskTag tag = TaskTag.builder()
                .task(task)
                .tag(request.getTag())
                .build();

        return toTagResponse(taskTagRepository.save(tag));
    }

    @Transactional
    public void deleteTag(Long taskId, String tagName) {
        taskTagRepository.deleteByTaskIdAndTag(taskId, tagName);
    }

    @Transactional
    public TaskCommentResponse addComment(Long taskId, TaskCommentRequest request, Long authorId) {
        Task task = findTaskOrThrow(taskId);
        
        TaskComment comment = TaskComment.builder()
                .task(task)
                .author(memberRepository.findById(authorId).orElse(null))
                .content(request.getContent())
                .internal(request.isInternal())
                .createdAt(LocalDateTime.now())
                .build();

        TaskComment saved = taskCommentRepository.save(comment);
        logAction(taskId, "COMMENT_ADDED", authorId, null);
        return toCommentResponse(saved);
    }

    public List<TaskCommentResponse> getComments(Long taskId) {
        return taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }

    public List<TaskLogResponse> getLogs(Long taskId) {
        return taskLogRepository.findByTaskIdOrderByCreatedAtDesc(taskId).stream()
                .map(this::toLogResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void holdTask(Long taskId, TaskHoldRequest request) {
        Task task = findTaskOrThrow(taskId);
        
        TaskHold hold = TaskHold.builder()
                .task(task)
                .reason(request.getReason())
                .startedAt(LocalDateTime.now())
                .build();

        taskHoldRepository.save(hold);
        task.setStatus(TaskStatus.PENDING);
        taskRepository.save(task);
        logAction(taskId, "HELD", null, Map.of("reason", request.getReason()));
    }

    @Transactional
    public void resumeTask(Long taskId) {
        Task task = findTaskOrThrow(taskId);
        
        TaskHold activeHold = taskHoldRepository.findActiveHoldByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("No active hold found"));

        activeHold.setEndedAt(LocalDateTime.now());
        taskHoldRepository.save(activeHold);
        
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);
        logAction(taskId, "RESUMED", null, null);
    }

    @Transactional
    public void transferTask(Long taskId, TaskTransferRequest request) {
        Task task = findTaskOrThrow(taskId);
        
        TaskTransfer transfer = TaskTransfer.builder()
                .task(task)
                .fromMember(task.getAssignee())
                .toMember(memberRepository.findById(request.getToMemberId())
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found")))
                .reason(request.getReason())
                .createdAt(LocalDateTime.now())
                .build();

        taskTransferRepository.save(transfer);
        task.setAssignee(transfer.getToMember());
        task.setTransferCount(task.getTransferCount() + 1);
        taskRepository.save(task);
        logAction(taskId, "TRANSFERRED", null, Map.of("to", request.getToMemberId().toString()));
    }

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + id));
    }

    private void logAction(Long taskId, String action, Long actorId, Map<String, String> details) {
        Task task = findTaskOrThrow(taskId);
        TaskLog log = TaskLog.builder()
                .task(task)
                .action(action)
                .actor(actorId != null ? memberRepository.findById(actorId).orElse(null) : null)
                .details(details != null ? details.toString() : null)
                .createdAt(LocalDateTime.now())
                .build();
        taskLogRepository.save(log);
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .type(task.getType())
                .urgency(task.getUrgency())
                .status(task.getStatus())
                .requesterName(task.getRequesterName())
                .requesterEmail(task.getRequesterEmail())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .dueDate(task.getDueDate())
                .estimatedHours(task.getEstimatedHours())
                .actualHours(task.getActualHours())
                .transferCount(task.getTransferCount())
                .createdAt(task.getCreatedAt())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .build();
    }

    private TaskChecklistResponse toChecklistResponse(TaskChecklist checklist) {
        return TaskChecklistResponse.builder()
                .id(checklist.getId())
                .taskId(checklist.getTask().getId())
                .itemText(checklist.getItemText())
                .done(checklist.isDone())
                .sortOrder(checklist.getSortOrder())
                .build();
    }

    private TaskTagResponse toTagResponse(TaskTag tag) {
        return TaskTagResponse.builder()
                .id(tag.getId())
                .taskId(tag.getTask().getId())
                .tag(tag.getTag())
                .build();
    }

    private TaskCommentResponse toCommentResponse(TaskComment comment) {
        return TaskCommentResponse.builder()
                .id(comment.getId())
                .taskId(comment.getTask().getId())
                .authorId(comment.getAuthor() != null ? comment.getAuthor().getId() : null)
                .content(comment.getContent())
                .internal(comment.isInternal())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private TaskLogResponse toLogResponse(TaskLog log) {
        return TaskLogResponse.builder()
                .id(log.getId())
                .taskId(log.getTask().getId())
                .action(log.getAction())
                .actorId(log.getActor() != null ? log.getActor().getId() : null)
                .details(log.getDetails())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
