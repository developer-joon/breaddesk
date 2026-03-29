package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {

    @Query("SELECT l FROM TaskLog l WHERE l.task.id = :taskId ORDER BY l.createdAt DESC")
    List<TaskLog> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") Long taskId);
}
