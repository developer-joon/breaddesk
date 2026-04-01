package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface TaskRepository extends JpaRepository<Task, Long> {

    long countByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.title LIKE %:keyword% OR t.description LIKE %:keyword%")
    java.util.List<Task> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT t FROM Task t WHERE (:status IS NULL OR t.status = :status) "
            + "AND (:type IS NULL OR t.type = :type) "
            + "AND (:urgency IS NULL OR t.urgency = :urgency) "
            + "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId)")
    Page<Task> search(
            @Param("status") TaskStatus status,
            @Param("type") String type,
            @Param("urgency") TaskUrgency urgency,
            @Param("assigneeId") Long assigneeId,
            Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.status <> 'DONE' "
            + "AND (t.slaResponseDeadline IS NOT NULL OR t.slaResolveDeadline IS NOT NULL)")
    java.util.List<Task> findActiveSlaTargets();

    @Query("SELECT t FROM Task t WHERE t.slaResponseDeadline IS NOT NULL OR t.slaResolveDeadline IS NOT NULL")
    java.util.List<Task> findAllWithSlaDeadlines();

    @Query("SELECT t FROM Task t WHERE "
            + "(:status IS NULL OR t.status = CAST(:status AS com.breadlab.breaddesk.task.entity.TaskStatus)) "
            + "AND (:priority IS NULL OR t.urgency = CAST(:priority AS com.breadlab.breaddesk.task.entity.TaskUrgency)) "
            + "AND (:assigneeId IS NULL OR t.assignee.id = :assigneeId) "
            + "AND (:teamId IS NULL OR t.team.id = :teamId) "
            + "AND (CAST(:dueDateFrom AS timestamp) IS NULL OR t.dueDate >= CAST(:dueDateFrom AS timestamp)) "
            + "AND (CAST(:dueDateTo AS timestamp) IS NULL OR t.dueDate <= CAST(:dueDateTo AS timestamp))")
    Page<Task> findWithFilters(
            @Param("status") String status,
            @Param("priority") String priority,
            @Param("assigneeId") Long assigneeId,
            @Param("teamId") Long teamId,
            @Param("dueDateFrom") String dueDateFrom,
            @Param("dueDateTo") String dueDateTo,
            Pageable pageable);
    
    java.util.List<Task> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
