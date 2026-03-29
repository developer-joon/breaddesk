package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    @Query("SELECT c FROM TaskComment c WHERE c.task.id = :taskId ORDER BY c.createdAt DESC")
    List<TaskComment> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") Long taskId);
}
