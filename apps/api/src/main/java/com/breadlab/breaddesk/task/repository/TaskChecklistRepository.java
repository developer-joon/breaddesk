package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskChecklist;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskChecklistRepository extends JpaRepository<TaskChecklist, Long> {
    List<TaskChecklist> findByTaskIdOrderBySortOrderAsc(Long taskId);
}
