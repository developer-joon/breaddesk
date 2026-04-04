package com.breadlab.breaddesk.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes notification events for SSE broadcasting
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publish a notification event to all SSE listeners
     */
    public void publishNotification(NotificationEvent event) {
        log.debug("Publishing notification event: type={}, title={}", event.getType(), event.getTitle());
        eventPublisher.publishEvent(event);
    }

    /**
     * Publish notification for new inquiry
     */
    public void publishNewInquiry(Long inquiryId, String senderName, String message) {
        publishNotification(NotificationEvent.forNewInquiry(
                inquiryId,
                String.format("%s님의 새 문의: %s", senderName, truncate(message, 100))
        ));
    }

    /**
     * Publish notification for inquiry status change
     */
    public void publishInquiryStatusChange(Long inquiryId, String status, String senderName) {
        publishNotification(NotificationEvent.forInquiryStatusChange(
                inquiryId,
                status,
                String.format("문의 #%d (%s) 상태가 %s로 변경되었습니다", inquiryId, senderName, status)
        ));
    }

    /**
     * Publish notification for new task
     */
    public void publishNewTask(Long taskId, String taskTitle) {
        publishNotification(NotificationEvent.forNewTask(
                taskId,
                String.format("새 태스크 생성: %s", truncate(taskTitle, 100))
        ));
    }

    /**
     * Publish notification for SLA warning
     */
    public void publishSlaWarning(Long inquiryId, String message) {
        publishNotification(NotificationEvent.forSlaWarning(inquiryId, message));
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
