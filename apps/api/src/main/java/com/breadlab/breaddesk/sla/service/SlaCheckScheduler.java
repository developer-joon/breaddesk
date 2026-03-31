package com.breadlab.breaddesk.sla.service;

import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.notification.service.NotificationService;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "breaddesk.sla.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class SlaCheckScheduler {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;
    private final MemberRepository memberRepository;

    @Scheduled(fixedRateString = "${breaddesk.sla.scheduler.interval-ms:60000}")
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();
        List<Task> activeTasks = taskRepository.findActiveSlaTargets();

        for (Task task : activeTasks) {
            checkResponseSla(task, now);
            checkResolveSla(task, now);
        }
    }

    private void checkResponseSla(Task task, LocalDateTime now) {
        if (task.getSlaResponseDeadline() == null || task.getSlaRespondedAt() != null) {
            return;
        }

        Duration total = Duration.between(task.getCreatedAt(), task.getSlaResponseDeadline());
        Duration elapsed = Duration.between(task.getCreatedAt(), now);
        double progress = total.toMinutes() > 0 ? (double) elapsed.toMinutes() / total.toMinutes() : 0;

        if (now.isAfter(task.getSlaResponseDeadline())) {
            if (!task.isSlaResponseBreached()) {
                task.setSlaResponseBreached(true);
                taskRepository.save(task);
                sendBreachNotification(task, "응답");
                log.warn("SLA response breached for task={}", task.getId());
            }
        } else if (progress >= 0.8) {
            long remainingMinutes = Duration.between(now, task.getSlaResponseDeadline()).toMinutes();
            sendWarningNotification(task, "응답", remainingMinutes);
        }
    }

    private void checkResolveSla(Task task, LocalDateTime now) {
        if (task.getSlaResolveDeadline() == null || task.getStatus() == TaskStatus.DONE) {
            return;
        }

        Duration total = Duration.between(task.getCreatedAt(), task.getSlaResolveDeadline());
        Duration elapsed = Duration.between(task.getCreatedAt(), now);
        double progress = total.toMinutes() > 0 ? (double) elapsed.toMinutes() / total.toMinutes() : 0;

        if (now.isAfter(task.getSlaResolveDeadline())) {
            if (!task.isSlaResolveBreached()) {
                task.setSlaResolveBreached(true);
                taskRepository.save(task);
                sendBreachNotification(task, "해결");
                log.warn("SLA resolve breached for task={}", task.getId());
            }
        } else if (progress >= 0.8) {
            long remainingMinutes = Duration.between(now, task.getSlaResolveDeadline()).toMinutes();
            sendWarningNotification(task, "해결", remainingMinutes);
        }
    }

    private void sendWarningNotification(Task task, String slaType, long remainingMinutes) {
        if (task.getAssignee() == null) return;

        notificationService.createNotification(
                task.getAssignee().getId(),
                "SLA_WARNING",
                String.format("[SLA 경고] %s", task.getTitle()),
                String.format("%s 기한 80%% 경과 (남은 시간: %d분)", slaType, remainingMinutes),
                "/tasks/" + task.getId());
    }

    private void sendBreachNotification(Task task, String slaType) {
        if (task.getAssignee() != null) {
            notificationService.createNotification(
                    task.getAssignee().getId(),
                    "SLA_BREACHED",
                    String.format("[SLA 위반] %s", task.getTitle()),
                    String.format("%s SLA가 위반되었습니다.", slaType),
                    "/tasks/" + task.getId());
        }

        List<Member> admins = memberRepository.findByRoleAndActiveTrue(MemberRole.ADMIN);
        for (Member admin : admins) {
            notificationService.createNotification(
                    admin.getId(),
                    "SLA_BREACHED",
                    String.format("[SLA 에스컬레이션] %s", task.getTitle()),
                    String.format("업무 #%d의 %s SLA가 위반되었습니다. 담당자: %s",
                            task.getId(), slaType,
                            task.getAssignee() != null ? task.getAssignee().getName() : "미배정"),
                    "/tasks/" + task.getId());
        }
    }
}
