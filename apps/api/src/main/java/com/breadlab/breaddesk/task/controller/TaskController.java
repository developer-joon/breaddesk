package com.breadlab.breaddesk.task.controller;

import com.breadlab.breaddesk.ai.AIAssignmentService;
import com.breadlab.breaddesk.auth.AuthUtils;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.task.dto.*;
import com.breadlab.breaddesk.task.service.TaskRelationService;
import com.breadlab.breaddesk.task.service.TaskService;
import com.breadlab.breaddesk.task.service.TaskWatcherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "Task", description = "태스크 관리 API")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final AuthUtils authUtils;
    private final TaskWatcherService watcherService;
    private final TaskRelationService relationService;
    private final AIAssignmentService aiAssignmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "문의에서 태스크 생성", description = "문의를 태스크로 원클릭 전환")
    @PostMapping("/from-inquiry/{inquiryId}")
    public ResponseEntity<ApiResponse<TaskResponse>> createTaskFromInquiry(@PathVariable Long inquiryId) {
        TaskResponse response = taskService.createTaskFromInquiry(inquiryId);
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
    public ResponseEntity<ApiResponse<TaskKanbanResponse>> getKanbanView() {
        TaskKanbanResponse response = taskService.getKanbanView();
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
        Long authorId = authUtils.getMemberId(userDetails);
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

    // ── Watch / Unwatch ──

    @PostMapping("/{taskId}/watch")
    public ResponseEntity<ApiResponse<Void>> watchTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long memberId = authUtils.getMemberId(userDetails);
        watcherService.watch(taskId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{taskId}/watch")
    public ResponseEntity<ApiResponse<Void>> unwatchTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long memberId = authUtils.getMemberId(userDetails);
        watcherService.unwatch(taskId, memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/{taskId}/watching")
    public ResponseEntity<ApiResponse<Boolean>> isWatching(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long memberId = authUtils.getMemberId(userDetails);
        return ResponseEntity.ok(ApiResponse.success(watcherService.isWatching(taskId, memberId)));
    }

    // ── Relations ──

    @PostMapping("/{taskId}/relations")
    public ResponseEntity<ApiResponse<TaskRelationResponse>> addRelation(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRelationRequest request) {
        TaskRelationResponse response = relationService.addRelation(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{taskId}/relations")
    public ResponseEntity<ApiResponse<List<TaskRelationResponse>>> getRelations(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(relationService.getRelations(taskId)));
    }

    @DeleteMapping("/{taskId}/relations/{relationId}")
    public ResponseEntity<ApiResponse<Void>> deleteRelation(
            @PathVariable Long taskId,
            @PathVariable Long relationId) {
        relationService.deleteRelation(taskId, relationId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── AI Assignee Recommendation ──

    @GetMapping("/{taskId}/recommend-assignee")
    public ResponseEntity<ApiResponse<List<AIAssignmentService.AssigneeRecommendation>>> recommendAssignee(
            @PathVariable Long taskId) {
        return ResponseEntity.ok(ApiResponse.success(aiAssignmentService.recommendAssignees(taskId)));
    }

    // ── Internal Comments (ADMIN only) ──

    @GetMapping("/{taskId}/comments/internal")
    public ResponseEntity<ApiResponse<List<TaskCommentResponse>>> getInternalComments(
            @PathVariable Long taskId) {
        // Only returns internal comments — security handled by @PreAuthorize or role check
        List<TaskCommentResponse> responses = taskService.getInternalComments(taskId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
