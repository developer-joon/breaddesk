package com.breadlab.breaddesk.inquiry.service;

import com.breadlab.breaddesk.ai.AIAnswerService;
import com.breadlab.breaddesk.ai.AITaskGenerationService;
import com.breadlab.breaddesk.ai.EscalationService;
import com.breadlab.breaddesk.audit.service.AuditLogService;
import com.breadlab.breaddesk.channel.service.WebhookOutboundService;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.csat.CsatService;
import com.breadlab.breaddesk.inquiry.dto.*;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.notification.NotificationEventPublisher;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final AIAnswerService aiAnswerService;
    private final AITaskGenerationService taskGenerationService;
    private final EscalationService escalationService;
    private final WebhookOutboundService webhookOutboundService;
    private final NotificationEventPublisher notificationEventPublisher;
    private final CsatService csatService;
    private final AuditLogService auditLogService;

    @Transactional
    public InquiryResponse createInquiry(InquiryRequest request) {
        Inquiry inquiry = Inquiry.builder()
                .channel(request.getChannel())
                .channelMeta(request.getChannelMeta())
                .senderName(request.getSenderName())
                .senderEmail(request.getSenderEmail())
                .message(request.getMessage())
                .status(InquiryStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);

        // Audit log
        auditLogService.log(null, "INQUIRY_CREATED", "INQUIRY", saved.getId(), 
            "New inquiry from " + saved.getSenderName());

        // Publish SSE notification for new inquiry
        notificationEventPublisher.publishNewInquiry(
                saved.getId(),
                saved.getSenderName(),
                saved.getMessage()
        );

        // AI 자동 답변 시도
        try {
            boolean aiResolved = aiAnswerService.tryAutoAnswer(saved);
            if (!aiResolved) {
                // AI confidence 부족 → 자동 에스컬레이션
                escalationService.escalateFromAI(saved);
            }
            // 최신 상태 다시 로드
            saved = inquiryRepository.findById(saved.getId()).orElse(saved);

            // AI가 답변을 생성했으면 원래 채널로 역전달
            if (aiResolved && saved.getAiResponse() != null) {
                try {
                    webhookOutboundService.sendResponse(saved, saved.getAiResponse(), "AI");
                } catch (Exception outboundEx) {
                    log.error("Outbound webhook failed for inquiry #{}: {}",
                            saved.getId(), outboundEx.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("AI 답변/에스컬레이션 처리 중 오류 (문의 #{}): {}", saved.getId(), e.getMessage(), e);
            // AI 실패해도 문의 자체는 정상 생성
        }

        return toResponse(saved);
    }

    public Page<InquiryResponse> getAllInquiries(String status, Long teamId, Pageable pageable) {
        InquiryStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = InquiryStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}", status);
            }
        }
        return inquiryRepository.findWithFilters(statusEnum, teamId, pageable)
                .map(this::toResponse);
    }

    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public InquiryResponse getInquiryById(Long id) {
        Inquiry inquiry = findInquiryOrThrow(id);
        return toResponse(inquiry);
    }

    @Transactional
    public InquiryResponse updateInquiryStatus(Long id, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = findInquiryOrThrow(id);
        InquiryStatus oldStatus = inquiry.getStatus();
        inquiry.setStatus(request.getStatus());
        
        // Audit log
        auditLogService.log(null, "INQUIRY_STATUS_CHANGED", "INQUIRY", id, 
            oldStatus + " -> " + request.getStatus());
        
        // Publish SSE notification for status change
        notificationEventPublisher.publishInquiryStatusChange(
                id,
                request.getStatus().name(),
                inquiry.getSenderName()
        );
        
        if (request.getStatus() == InquiryStatus.RESOLVED || request.getStatus() == InquiryStatus.CLOSED) {
            inquiry.setResolvedAt(LocalDateTime.now());

            // Send outbound webhook for resolved inquiries
            String response = inquiry.getAiResponse() != null ? inquiry.getAiResponse() : inquiry.getMessage();
            String resolvedBy = inquiry.getResolvedBy() != null ? inquiry.getResolvedBy().name() : "HUMAN";
            try {
                webhookOutboundService.sendResponse(inquiry, response, resolvedBy);
            } catch (Exception e) {
                log.error("Outbound webhook failed for inquiry #{}: {}", id, e.getMessage());
            }

            // Create and send CSAT survey
            try {
                var survey = csatService.createSurvey(id);
                log.info("CSAT survey created for inquiry #{}: /survey/{}", id, survey.getToken());
                // n8n will pick up the survey URL from logs or a separate webhook
            } catch (Exception e) {
                log.error("Failed to create CSAT survey for inquiry #{}: {}", id, e.getMessage());
            }
        }
        
        return toResponse(inquiryRepository.save(inquiry));
    }

    @Transactional
    public InquiryMessageResponse addMessage(Long inquiryId, InquiryMessageRequest request) {
        Inquiry inquiry = findInquiryOrThrow(inquiryId);
        
        InquiryMessage message = InquiryMessage.builder()
                .inquiry(inquiry)
                .role(request.getRole())
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        InquiryMessage saved = inquiryMessageRepository.save(message);
        return toMessageResponse(saved);
    }

    /**
     * AI 태스크 생성 미리보기
     */
    public TaskPreviewResponse generateTaskPreview(Long inquiryId) {
        Inquiry inquiry = findInquiryOrThrow(inquiryId);
        var generated = taskGenerationService.generateTaskFromInquiry(inquiry);

        return new TaskPreviewResponse(
                generated.title(),
                generated.description(),
                generated.checklist(),
                generated.category(),
                generated.urgency(),
                inquiry.getSenderName(),
                inquiry.getSenderEmail(),
                inquiry.getId()
        );
    }

    @Transactional
    public InquiryResponse convertToTask(Long inquiryId, ConvertToTaskRequest request) {
        Inquiry inquiry = findInquiryOrThrow(inquiryId);

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription() != null ? request.getDescription() : inquiry.getMessage())
                .type(request.getType() != null ? request.getType() : "GENERAL")
                .urgency(request.getUrgency() != null ? request.getUrgency() : com.breadlab.breaddesk.task.entity.TaskUrgency.NORMAL)
                .status(TaskStatus.WAITING)
                .requesterName(inquiry.getSenderName())
                .requesterEmail(inquiry.getSenderEmail())
                .createdAt(LocalDateTime.now())
                .transferCount(0)
                .slaResponseBreached(false)
                .slaResolveBreached(false)
                .build();

        if (request.getAssigneeId() != null) {
            task.setAssignee(memberRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + request.getAssigneeId())));
        }

        // Copy team from inquiry if available
        if (inquiry.getTeam() != null) {
            task.setTeam(inquiry.getTeam());
        }

        Task savedTask = taskRepository.save(task);

        inquiry.setTask(savedTask);
        inquiry.setStatus(InquiryStatus.ESCALATED);
        
        return toResponse(inquiryRepository.save(inquiry));
    }

    @Transactional
    public void deleteInquiry(Long id) {
        Inquiry inquiry = findInquiryOrThrow(id);
        inquiryRepository.delete(inquiry);
    }

    private Inquiry findInquiryOrThrow(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found: " + id));
    }

    private InquiryResponse toResponse(Inquiry inquiry) {
        return InquiryResponse.builder()
                .id(inquiry.getId())
                .channel(inquiry.getChannel())
                .channelMeta(inquiry.getChannelMeta())
                .senderName(inquiry.getSenderName())
                .senderEmail(inquiry.getSenderEmail())
                .message(inquiry.getMessage())
                .aiResponse(inquiry.getAiResponse())
                .aiConfidence(inquiry.getAiConfidence())
                .status(inquiry.getStatus())
                .taskId(inquiry.getTask() != null ? inquiry.getTask().getId() : null)
                .resolvedBy(inquiry.getResolvedBy() != null ? inquiry.getResolvedBy().name() : null)
                .createdAt(inquiry.getCreatedAt())
                .resolvedAt(inquiry.getResolvedAt())
                .build();
    }

    private InquiryMessageResponse toMessageResponse(InquiryMessage message) {
        return InquiryMessageResponse.builder()
                .id(message.getId())
                .inquiryId(message.getInquiry().getId())
                .role(message.getRole())
                .message(message.getMessage())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
