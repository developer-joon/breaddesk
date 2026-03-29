package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {
    List<TaskTag> findByTaskId(Long taskId);
    void deleteByTaskId(Long taskId);
}
