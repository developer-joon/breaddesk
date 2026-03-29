package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskTransferRepository extends JpaRepository<TaskTransfer, Long> {

    @Query("SELECT t FROM TaskTransfer t WHERE t.task.id = :taskId ORDER BY t.createdAt DESC")
    List<TaskTransfer> findByTaskIdOrderByCreatedAtDesc(@Param("taskId") Long taskId);
}
