package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {

    @Query("SELECT t FROM TaskTag t WHERE t.task.id = :taskId")
    List<TaskTag> findByTaskId(@Param("taskId") Long taskId);

    void deleteByTaskIdAndTag(Long taskId, String tag);
}
