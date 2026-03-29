package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.notification.service.NotificationService;
import com.breadlab.breaddesk.sla.service.SlaService;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 에스컬레이션 서비스.
 * AI confidence가 낮거나 사용자 불만족 시 → Task 자동 생성 + SLA + 알림.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscalationService {

    private final InquiryRepository inquiryRepository;
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final SlaService slaService;
    private final NotificationService notificationService;

    /**
     * 문의를 업무로 에스컬레이션합니다.
     */
    @Transactional
    public Task escalate(Inquiry inquiry, TaskUrgency urgency) {
        if (urgency == null) {
            urgency = determineUrgency(inquiry);
        }

        // 업무 생성
        Task task = Task.builder()
                .title("[에스컬레이션] " + truncate(inquiry.getMessage(), 100))
                .description(buildTaskDescription(inquiry))
                .type("ESCALATION")
                .urgency(urgency)
                .status(TaskStatus.WAITING)
                .requesterName(inquiry.getSenderName())
                .requesterEmail(inquiry.getSenderEmail())
                .createdAt(LocalDateTime.now())
                .transferCount(0)
                .slaResponseBreached(false)
                .slaResolveBreached(false)
                .build();

        // SLA 데드라인 계산
        slaService.calculateSlaDeadlines(task);

        Task savedTask = taskRepository.save(task);

        // 문의 상태 업데이트
        inquiry.setTask(savedTask);
        inquiry.setStatus(InquiryStatus.ESCALATED);
        inquiryRepository.save(inquiry);

        // 관리자에게 알림
        notifyAdmins(savedTask, inquiry);

        log.info("문의 #{} → 업무 #{} 에스컬레이션 완료 (urgency: {})",
                inquiry.getId(), savedTask.getId(), urgency);

        return savedTask;
    }

    /**
     * AI confidence 부족 시 자동 에스컬레이션.
     */
    @Transactional
    public Task escalateFromAI(Inquiry inquiry) {
        return escalate(inquiry, null);
    }

    /**
     * 사용자 불만족 시 에스컬레이션.
     */
    @Transactional
    public Task escalateFromUserDissatisfaction(Inquiry inquiry) {
        return escalate(inquiry, TaskUrgency.HIGH);
    }

    private TaskUrgency determineUrgency(Inquiry inquiry) {
        String message = inquiry.getMessage().toLowerCase();

        // 긴급 키워드 기반 urgency 결정
        if (containsAny(message, "긴급", "urgent", "critical", "장애", "다운", "서비스 중단")) {
            return TaskUrgency.CRITICAL;
        }
        if (containsAny(message, "빨리", "급한", "asap", "중요", "important")) {
            return TaskUrgency.HIGH;
        }
        return TaskUrgency.NORMAL;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }

    private String buildTaskDescription(Inquiry inquiry) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 원본 문의\n");
        sb.append("- **채널:** ").append(inquiry.getChannel()).append("\n");
        sb.append("- **발신자:** ").append(inquiry.getSenderName());
        if (inquiry.getSenderEmail() != null) {
            sb.append(" (").append(inquiry.getSenderEmail()).append(")");
        }
        sb.append("\n");
        sb.append("- **내용:** ").append(inquiry.getMessage()).append("\n");

        if (inquiry.getAiResponse() != null) {
            sb.append("\n## AI 답변 (confidence: ")
                    .append(String.format("%.1f%%", inquiry.getAiConfidence() * 100))
                    .append(")\n");
            sb.append(inquiry.getAiResponse()).append("\n");
        }

        return sb.toString();
    }

    private void notifyAdmins(Task task, Inquiry inquiry) {
        List<Member> admins = memberRepository.findAll().stream()
                .filter(m -> m.getRole() == MemberRole.ADMIN && m.isActive())
                .toList();

        for (Member admin : admins) {
            try {
                notificationService.createNotification(
                        admin.getId(),
                        "ESCALATION",
                        "새 에스컬레이션: " + truncate(inquiry.getMessage(), 50),
                        String.format("문의 #%d가 업무 #%d로 에스컬레이션되었습니다. (Urgency: %s)",
                                inquiry.getId(), task.getId(), task.getUrgency()),
                        "/tasks/" + task.getId()
                );
            } catch (Exception e) {
                log.error("관리자 알림 실패 (memberId: {}): {}", admin.getId(), e.getMessage());
            }
        }
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
