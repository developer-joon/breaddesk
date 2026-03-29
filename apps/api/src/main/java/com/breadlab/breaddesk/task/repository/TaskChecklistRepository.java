package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskChecklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskChecklistRepository extends JpaRepository<TaskChecklist, Long> {
    List<TaskChecklist> findByTaskIdOrderBySortOrder(Long taskId);
    void deleteByTaskId(Long taskId);
}
