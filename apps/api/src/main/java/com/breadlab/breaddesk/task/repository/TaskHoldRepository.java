package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskHold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TaskHoldRepository extends JpaRepository<TaskHold, Long> {

    @Query("SELECT h FROM TaskHold h WHERE h.task.id = :taskId AND h.endedAt IS NULL")
    Optional<TaskHold> findActiveHoldByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT h FROM TaskHold h WHERE h.task.id = :taskId ORDER BY h.startedAt DESC")
    List<TaskHold> findByTaskIdOrderByStartedAtDesc(@Param("taskId") Long taskId);
}
