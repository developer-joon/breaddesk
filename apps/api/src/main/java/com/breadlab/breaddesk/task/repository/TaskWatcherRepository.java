package com.breadlab.breaddesk.task.repository;

import com.breadlab.breaddesk.task.entity.TaskWatcher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskWatcherRepository extends JpaRepository<TaskWatcher, Long> {

    @Query("SELECT w FROM TaskWatcher w WHERE w.task.id = :taskId")
    List<TaskWatcher> findByTaskId(@Param("taskId") Long taskId);

    @Query("SELECT w FROM TaskWatcher w WHERE w.task.id = :taskId AND w.member.id = :memberId")
    Optional<TaskWatcher> findByTaskIdAndMemberId(@Param("taskId") Long taskId, @Param("memberId") Long memberId);

    void deleteByTaskIdAndMemberId(Long taskId, Long memberId);

    boolean existsByTaskIdAndMemberId(Long taskId, Long memberId);
}
