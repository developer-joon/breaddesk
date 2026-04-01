package com.breadlab.breaddesk.task.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.task.dto.*;
import com.breadlab.breaddesk.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getAllTasks(Pageable pageable) {
        Page<TaskResponse> responses = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        TaskResponse response = taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/kanban")
    public ResponseEntity<ApiResponse<TaskKanbanResponse>> getKanbanView(
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String urgency,
            @RequestParam(required = false) String type) {
        TaskKanbanResponse response = taskService.getKanbanView(assigneeId, teamId, urgency, type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{taskId}/checklists")
    public ResponseEntity<ApiResponse<TaskChecklistResponse>> addChecklist(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskChecklistRequest request) {
        TaskChecklistResponse response = taskService.addChecklist(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PutMapping("/{taskId}/checklists/{checklistId}")
    public ResponseEntity<ApiResponse<TaskChecklistResponse>> updateChecklist(
            @PathVariable Long taskId,
            @PathVariable Long checklistId,
            @Valid @RequestBody TaskChecklistRequest request) {
        TaskChecklistResponse response = taskService.updateChecklist(taskId, checklistId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{taskId}/checklists/{checklistId}")
    public ResponseEntity<ApiResponse<Void>> deleteChecklist(
            @PathVariable Long taskId,
            @PathVariable Long checklistId) {
        taskService.deleteChecklist(taskId, checklistId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{taskId}/tags")
    public ResponseEntity<ApiResponse<TaskTagResponse>> addTag(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskTagRequest request) {
        TaskTagResponse response = taskService.addTag(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{taskId}/tags/{tag}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Long taskId,
            @PathVariable String tag) {
        taskService.deleteTag(taskId, tag);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{taskId}/comments")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long authorId = Long.parseLong(userDetails.getUsername());
        TaskCommentResponse response = taskService.addComment(taskId, request, authorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{taskId}/comments")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getComments(@PathVariable Long taskId) {
        List<TaskCommentResponse> responses = taskService.getComments(taskId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{taskId}/logs")
    public ResponseEntity<ApiResponse<List<TaskLogResponse>>> getLogs(@PathVariable Long taskId) {
        List<TaskLogResponse> responses = taskService.getLogs(taskId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{taskId}/hold")
    public ResponseEntity<ApiResponse<Void>> holdTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskHoldRequest request) {
        taskService.holdTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{taskId}/resume")
    public ResponseEntity<ApiResponse<Void>> resumeTask(@PathVariable Long taskId) {
        taskService.resumeTask(taskId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/{taskId}/transfer")
    public ResponseEntity<ApiResponse<Void>> transferTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskTransferRequest request) {
        taskService.transferTask(taskId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
