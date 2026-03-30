package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRelationRepository extends JpaRepository<TaskRelation, Long> {

    @Query("SELECT r FROM TaskRelation r WHERE r.sourceTask.id = :taskId OR r.targetTask.id = :taskId")
    List<TaskRelation> findByTaskId(@Param("taskId") Long taskId);
}
