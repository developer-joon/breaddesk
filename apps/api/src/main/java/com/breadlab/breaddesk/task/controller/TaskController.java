package com.breadlab.breaddesk.task.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.common.dto.PageResponse;
import com.breadlab.breaddesk.task.dto.KanbanResponse;
import com.breadlab.breaddesk.task.dto.TaskRequest;
import com.breadlab.breaddesk.task.dto.TaskResponse;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        return ApiResponse.success("Task created successfully", taskService.createTask(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> getTask(@PathVariable Long id) {
        return ApiResponse.success(taskService.getTask(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<TaskResponse>> getTasks(
            @RequestParam(required = false) Task.TaskStatus status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasks(status, assigneeId, type, pageable);
        return ApiResponse.success(PageResponse.of(tasks));
    }

    @GetMapping("/kanban")
    public ApiResponse<KanbanResponse> getKanban() {
        return ApiResponse.success(taskService.getKanban());
    }

    @PatchMapping("/{id}")
    public ApiResponse<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequest.Update request) {
        return ApiResponse.success("Task updated successfully", taskService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PostMapping("/{id}/assign")
    public ApiResponse<TaskResponse> assignTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest.Assign request) {
        return ApiResponse.success("Task assigned successfully", taskService.assignTask(id, request));
    }
}
