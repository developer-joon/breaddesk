package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

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

    long countByStatus(TaskStatus status);
}
