package com.breadlab.breaddesk.inquiry.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.inquiry.dto.*;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final TaskRepository taskRepository;
    private final MemberRepository memberRepository;

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
        return toResponse(saved);
    }

    public Page<InquiryResponse> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable).map(this::toResponse);
    }

    public InquiryResponse getInquiryById(Long id) {
        Inquiry inquiry = findInquiryOrThrow(id);
        return toResponse(inquiry);
    }

    @Transactional
    public InquiryResponse updateInquiryStatus(Long id, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = findInquiryOrThrow(id);
        inquiry.setStatus(request.getStatus());
        
        if (request.getStatus() == InquiryStatus.RESOLVED || request.getStatus() == InquiryStatus.CLOSED) {
            inquiry.setResolvedAt(LocalDateTime.now());
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
