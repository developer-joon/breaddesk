package com.breadlab.breaddesk.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Notification Event for SSE broadcasting
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    
    private Long memberId;
    private String type;
    private String title;
    private String message;
    private String link;
    private Long entityId; // inquiry/task ID
    
    public static NotificationEvent forNewInquiry(Long inquiryId, String message) {
        return NotificationEvent.builder()
                .type("NEW_INQUIRY")
                .title("새 문의")
                .message(message)
                .link("/inquiries/" + inquiryId)
                .entityId(inquiryId)
                .build();
    }
    
    public static NotificationEvent forInquiryStatusChange(Long inquiryId, String status, String message) {
        return NotificationEvent.builder()
                .type("INQUIRY_STATUS_CHANGE")
                .title("문의 상태 변경")
                .message(message)
                .link("/inquiries/" + inquiryId)
                .entityId(inquiryId)
                .build();
    }
    
    public static NotificationEvent forNewTask(Long taskId, String message) {
        return NotificationEvent.builder()
                .type("NEW_TASK")
                .title("새 태스크")
                .message(message)
                .link("/tasks/" + taskId)
                .entityId(taskId)
                .build();
    }
    
    public static NotificationEvent forSlaWarning(Long inquiryId, String message) {
        return NotificationEvent.builder()
                .type("SLA_WARNING")
                .title("SLA 경고")
                .message(message)
                .link("/inquiries/" + inquiryId)
                .entityId(inquiryId)
                .build();
    }
}
