package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(Task.TaskStatus status, Pageable pageable);

    Page<Task> findByAssigneeId(Long assigneeId, Pageable pageable);

    Page<Task> findByType(String type, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
            "(:type IS NULL OR t.type = :type)")
    Page<Task> findByFilters(Task.TaskStatus status, Long assigneeId, String type, Pageable pageable);

    List<Task> findByStatusOrderByCreatedAtDesc(Task.TaskStatus status);
}
