package com.breadlab.breaddesk.task.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.notification.service.NotificationService;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskWatcher;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import com.breadlab.breaddesk.task.repository.TaskWatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskWatcherService {

    private final TaskWatcherRepository watcherRepository;
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Transactional
    public void watch(Long taskId, Long memberId) {
        if (watcherRepository.existsByTaskIdAndMemberId(taskId, memberId)) {
            return; // already watching
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));

        TaskWatcher watcher = TaskWatcher.builder()
                .task(task)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();
        watcherRepository.save(watcher);
    }

    @Transactional
    public void unwatch(Long taskId, Long memberId) {
        watcherRepository.deleteByTaskIdAndMemberId(taskId, memberId);
    }

    @Transactional(readOnly = true)
    public boolean isWatching(Long taskId, Long memberId) {
        return watcherRepository.existsByTaskIdAndMemberId(taskId, memberId);
    }

    @Transactional(readOnly = true)
    public List<Long> getWatcherIds(Long taskId) {
        return watcherRepository.findByTaskId(taskId).stream()
                .map(w -> w.getMember().getId())
                .toList();
    }

    @Transactional
    public void notifyWatchers(Long taskId, String event, String message, Long excludeMemberId) {
        List<TaskWatcher> watchers = watcherRepository.findByTaskId(taskId);
        for (TaskWatcher w : watchers) {
            if (excludeMemberId != null && w.getMember().getId().equals(excludeMemberId)) {
                continue;
            }
            notificationService.createNotification(
                    w.getMember().getId(),
                    "TASK_WATCH",
                    event,
                    message,
                    "/tasks/" + taskId
            );
        }
    }
}
