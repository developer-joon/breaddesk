package com.breadlab.breaddesk.task.service;

import com.breadlab.breaddesk.auth.entity.Member;
import com.breadlab.breaddesk.auth.repository.MemberRepository;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.task.dto.KanbanResponse;
import com.breadlab.breaddesk.task.dto.TaskRequest;
import com.breadlab.breaddesk.task.dto.TaskResponse;
import com.breadlab.breaddesk.task.entity.*;
import com.breadlab.breaddesk.task.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskChecklistRepository checklistRepository;
    private final TaskTagRepository tagRepository;
    private final TaskLogRepository logRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .type(request.getType())
                .urgency(request.getUrgency() != null ? request.getUrgency() : Task.TaskUrgency.NORMAL)
                .status(Task.TaskStatus.WAITING)
                .requesterName(request.getRequesterName())
                .requesterEmail(request.getRequesterEmail())
                .dueDate(request.getDueDate())
                .estimatedHours(request.getEstimatedHours())
                .build();

        if (request.getAssigneeId() != null) {
            Member assignee = memberRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member", request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task saved = taskRepository.save(task);

        // 체크리스트 생성
        List<TaskChecklist> checklists = new ArrayList<>();
        if (request.getChecklist() != null && !request.getChecklist().isEmpty()) {
            for (int i = 0; i < request.getChecklist().size(); i++) {
                TaskChecklist checklist = TaskChecklist.builder()
                        .taskId(saved.getId())
                        .itemText(request.getChecklist().get(i))
                        .sortOrder(i)
                        .build();
                checklists.add(checklistRepository.save(checklist));
            }
        }

        // 태그 생성
        List<TaskTag> tags = new ArrayList<>();
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tagName : request.getTags()) {
                TaskTag tag = TaskTag.builder()
                        .taskId(saved.getId())
                        .tag(tagName)
                        .build();
                tags.add(tagRepository.save(tag));
            }
        }

        // 로그 기록
        logTask(saved.getId(), "CREATED", null, Map.of("title", saved.getTitle()));

        log.info("Created task: {} ({})", saved.getId(), saved.getTitle());
        return TaskResponse.from(saved, checklists, tags);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        List<TaskChecklist> checklists = checklistRepository.findByTaskIdOrderBySortOrder(id);
        List<TaskTag> tags = tagRepository.findByTaskId(id);

        return TaskResponse.from(task, checklists, tags);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getTasks(Task.TaskStatus status, Long assigneeId, String type, Pageable pageable) {
        Page<Task> tasks = taskRepository.findByFilters(status, assigneeId, type, pageable);
        return tasks.map(TaskResponse::from);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest.Update request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        Map<String, Object> changes = new HashMap<>();

        if (request.getTitle() != null) {
            changes.put("title", Map.of("old", task.getTitle(), "new", request.getTitle()));
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            changes.put("type", Map.of("old", task.getType(), "new", request.getType()));
            task.setType(request.getType());
        }
        if (request.getUrgency() != null) {
            changes.put("urgency", Map.of("old", task.getUrgency(), "new", request.getUrgency()));
            task.setUrgency(request.getUrgency());
        }
        if (request.getStatus() != null) {
            Task.TaskStatus oldStatus = task.getStatus();
            task.setStatus(request.getStatus());
            changes.put("status", Map.of("old", oldStatus, "new", request.getStatus()));

            // 상태 변경 시 타임스탬프 업데이트
            if (request.getStatus() == Task.TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
                task.setStartedAt(LocalDateTime.now());
            } else if (request.getStatus() == Task.TaskStatus.DONE && task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        }
        if (request.getAssigneeId() != null) {
            Member assignee = memberRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member", request.getAssigneeId()));
            changes.put("assignee", Map.of("old", task.getAssignee() != null ? task.getAssignee().getId() : null, "new", assignee.getId()));
            task.setAssignee(assignee);
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }
        if (request.getActualHours() != null) {
            task.setActualHours(request.getActualHours());
        }

        Task updated = taskRepository.save(task);

        if (!changes.isEmpty()) {
            logTask(id, "UPDATED", null, changes);
        }

        log.info("Updated task: {}", id);
        return getTask(id);
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }

        checklistRepository.deleteByTaskId(id);
        tagRepository.deleteByTaskId(id);
        taskRepository.deleteById(id);

        log.info("Deleted task: {}", id);
    }

    @Transactional
    public TaskResponse assignTask(Long id, TaskRequest.Assign request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        Member assignee = memberRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getAssigneeId()));

        Long oldAssigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;
        task.setAssignee(assignee);
        taskRepository.save(task);

        logTask(id, "ASSIGNED", request.getAssigneeId(), Map.of(
                "old_assignee", oldAssigneeId,
                "new_assignee", assignee.getId(),
                "assignee_name", assignee.getName()
        ));

        log.info("Assigned task {} to member {}", id, assignee.getName());
        return getTask(id);
    }

    @Transactional(readOnly = true)
    public KanbanResponse getKanban() {
        Map<String, List<TaskResponse>> columns = new LinkedHashMap<>();
        int totalCount = 0;

        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            List<Task> tasks = taskRepository.findByStatusOrderByCreatedAtDesc(status);
            List<TaskResponse> responses = tasks.stream()
                    .map(TaskResponse::from)
                    .toList();
            columns.put(status.name(), responses);
            totalCount += tasks.size();
        }

        return KanbanResponse.builder()
                .columns(columns)
                .totalCount(totalCount)
                .build();
    }

    private void logTask(Long taskId, String action, Long actorId, Map<String, Object> details) {
        TaskLog log = TaskLog.builder()
                .taskId(taskId)
                .action(action)
                .actorId(actorId)
                .details(details)
                .build();
        logRepository.save(log);
    }
}
