package com.breadlab.breaddesk.task.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.task.dto.TaskRelationRequest;
import com.breadlab.breaddesk.task.dto.TaskRelationResponse;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskRelation;
import com.breadlab.breaddesk.task.repository.TaskRelationRepository;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskRelationService {

    private final TaskRelationRepository relationRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public TaskRelationResponse addRelation(Long sourceTaskId, TaskRelationRequest request) {
        Task source = taskRepository.findById(sourceTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + sourceTaskId));
        Task target = taskRepository.findById(request.getTargetTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + request.getTargetTaskId()));

        TaskRelation relation = TaskRelation.builder()
                .sourceTask(source)
                .targetTask(target)
                .relationType(request.getRelationType())
                .build();

        return toResponse(relationRepository.save(relation));
    }

    public List<TaskRelationResponse> getRelations(Long taskId) {
        return relationRepository.findByTaskId(taskId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteRelation(Long taskId, Long relationId) {
        TaskRelation relation = relationRepository.findById(relationId)
                .orElseThrow(() -> new ResourceNotFoundException("Relation not found: " + relationId));
        relationRepository.delete(relation);
    }

    private TaskRelationResponse toResponse(TaskRelation relation) {
        return TaskRelationResponse.builder()
                .id(relation.getId())
                .sourceTaskId(relation.getSourceTask().getId())
                .sourceTaskTitle(relation.getSourceTask().getTitle())
                .targetTaskId(relation.getTargetTask().getId())
                .targetTaskTitle(relation.getTargetTask().getTitle())
                .relationType(relation.getRelationType())
                .build();
    }
}
