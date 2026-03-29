package com.breadlab.breaddesk.common;

import com.breadlab.breaddesk.attachment.entity.Attachment;
import com.breadlab.breaddesk.attachment.entity.AttachmentEntityType;
import com.breadlab.breaddesk.inquiry.dto.ConvertToTaskRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryMessageRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryStatusUpdateRequest;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.member.dto.MemberRequest;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.notification.entity.Notification;
import com.breadlab.breaddesk.personal.dto.PersonalNoteRequest;
import com.breadlab.breaddesk.personal.entity.PersonalNote;
import com.breadlab.breaddesk.sla.entity.SlaRule;
import com.breadlab.breaddesk.task.dto.*;
import com.breadlab.breaddesk.task.entity.*;
import com.breadlab.breaddesk.template.dto.ReplyTemplateRequest;
import com.breadlab.breaddesk.template.entity.ReplyTemplate;

import java.time.LocalDateTime;

public final class TestDataFactory {

    private TestDataFactory() {}

    // ===== Member =====

    public static Member createMember() {
        return createMember("Test Agent", "agent@test.com");
    }

    public static Member createMember(String name, String email) {
        return Member.builder()
                .name(name)
                .email(email)
                .passwordHash("$2a$10$encodedPasswordHash")
                .role(MemberRole.AGENT)
                .active(true)
                .build();
    }

    public static Member createAdmin() {
        return Member.builder()
                .name("Admin User")
                .email("admin@test.com")
                .passwordHash("$2a$10$encodedPasswordHash")
                .role(MemberRole.ADMIN)
                .active(true)
                .build();
    }

    public static MemberRequest createMemberRequest() {
        MemberRequest request = new MemberRequest();
        request.setName("New Agent");
        request.setEmail("new@test.com");
        request.setPassword("password123");
        request.setRole(MemberRole.AGENT);
        request.setActive(true);
        return request;
    }

    // ===== Inquiry =====

    public static Inquiry createInquiry() {
        return Inquiry.builder()
                .channel("email")
                .senderName("Customer")
                .senderEmail("customer@example.com")
                .message("I have a question")
                .status(InquiryStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static InquiryRequest createInquiryRequest() {
        return InquiryRequest.builder()
                .channel("email")
                .senderName("Customer")
                .senderEmail("customer@example.com")
                .message("I have a question about the product")
                .build();
    }

    public static InquiryStatusUpdateRequest createInquiryStatusUpdateRequest(InquiryStatus status) {
        return InquiryStatusUpdateRequest.builder()
                .status(status)
                .build();
    }

    public static InquiryMessageRequest createInquiryMessageRequest() {
        return InquiryMessageRequest.builder()
                .role(InquiryMessageRole.AGENT)
                .message("Thank you for your inquiry")
                .build();
    }

    public static InquiryMessage createInquiryMessage(Inquiry inquiry) {
        return InquiryMessage.builder()
                .inquiry(inquiry)
                .role(InquiryMessageRole.AGENT)
                .message("Response message")
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ConvertToTaskRequest createConvertToTaskRequest() {
        return ConvertToTaskRequest.builder()
                .title("Task from inquiry")
                .description("Converted from inquiry")
                .type("SUPPORT")
                .urgency(TaskUrgency.NORMAL)
                .build();
    }

    // ===== Task =====

    public static Task createTask() {
        return createTask("Test Task");
    }

    public static Task createTask(String title) {
        return Task.builder()
                .title(title)
                .description("Test description")
                .type("GENERAL")
                .urgency(TaskUrgency.NORMAL)
                .status(TaskStatus.WAITING)
                .requesterName("Requester")
                .requesterEmail("req@test.com")
                .createdAt(LocalDateTime.now())
                .transferCount(0)
                .slaResponseBreached(false)
                .slaResolveBreached(false)
                .build();
    }

    public static TaskRequest createTaskRequest() {
        TaskRequest request = new TaskRequest();
        request.setTitle("New Task");
        request.setDescription("Task description");
        request.setType("GENERAL");
        request.setUrgency(TaskUrgency.NORMAL);
        request.setRequesterName("Requester");
        request.setRequesterEmail("req@test.com");
        return request;
    }

    public static TaskStatusUpdateRequest createTaskStatusUpdateRequest(TaskStatus status) {
        TaskStatusUpdateRequest request = new TaskStatusUpdateRequest();
        request.setStatus(status);
        return request;
    }

    public static TaskChecklist createTaskChecklist(Task task) {
        return TaskChecklist.builder()
                .task(task)
                .itemText("Check item")
                .done(false)
                .sortOrder(0)
                .build();
    }

    public static TaskChecklistRequest createTaskChecklistRequest() {
        TaskChecklistRequest request = new TaskChecklistRequest();
        request.setItemText("New checklist item");
        request.setSortOrder(0);
        return request;
    }

    public static TaskComment createTaskComment(Task task, Member author) {
        return TaskComment.builder()
                .task(task)
                .author(author)
                .content("Test comment")
                .internal(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static TaskCommentRequest createTaskCommentRequest() {
        TaskCommentRequest request = new TaskCommentRequest();
        request.setContent("New comment");
        request.setInternal(false);
        return request;
    }

    public static TaskTag createTaskTag(Task task) {
        return TaskTag.builder()
                .task(task)
                .tag("bug")
                .build();
    }

    public static TaskTagRequest createTaskTagRequest() {
        TaskTagRequest request = new TaskTagRequest();
        request.setTag("feature");
        return request;
    }

    public static TaskHoldRequest createTaskHoldRequest() {
        TaskHoldRequest request = new TaskHoldRequest();
        request.setReason("Waiting for customer response");
        return request;
    }

    public static TaskHold createTaskHold(Task task) {
        return TaskHold.builder()
                .task(task)
                .reason("Hold reason")
                .startedAt(LocalDateTime.now())
                .build();
    }

    public static TaskTransferRequest createTaskTransferRequest(Long toMemberId) {
        TaskTransferRequest request = new TaskTransferRequest();
        request.setToMemberId(toMemberId);
        request.setReason("Specialist needed");
        return request;
    }

    // ===== Template =====

    public static ReplyTemplate createReplyTemplate() {
        return ReplyTemplate.builder()
                .title("Welcome Template")
                .category("greeting")
                .content("Hello {{name}}, welcome!")
                .usageCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static ReplyTemplateRequest createReplyTemplateRequest() {
        return ReplyTemplateRequest.builder()
                .title("New Template")
                .category("support")
                .content("Thank you for contacting us, {{name}}")
                .build();
    }

    // ===== Notification =====

    public static Notification createNotification(Member member) {
        return Notification.builder()
                .member(member)
                .type("TASK_ASSIGNED")
                .title("New task assigned")
                .message("You have been assigned a new task")
                .link("/tasks/1")
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ===== PersonalNote =====

    public static PersonalNote createPersonalNote(Member member) {
        return PersonalNote.builder()
                .member(member)
                .content("My personal note")
                .build();
    }

    public static PersonalNoteRequest createPersonalNoteRequest() {
        return PersonalNoteRequest.builder()
                .content("New note content")
                .build();
    }

    // ===== SlaRule =====

    public static SlaRule createSlaRule(TaskUrgency urgency) {
        return SlaRule.builder()
                .urgency(urgency)
                .responseMinutes(urgency == TaskUrgency.CRITICAL ? 15 : 60)
                .resolveMinutes(urgency == TaskUrgency.CRITICAL ? 120 : 480)
                .active(true)
                .build();
    }

    // ===== Attachment =====

    public static Attachment createAttachment() {
        return Attachment.builder()
                .entityType(AttachmentEntityType.TASK)
                .entityId(1L)
                .filename("test.pdf")
                .filePath("/tmp/test.pdf")
                .fileSize(1024L)
                .mimeType("application/pdf")
                .createdAt(LocalDateTime.now())
                .build();
    }
}
