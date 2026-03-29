package com.breadlab.breaddesk.inquiry.service;

import com.breadlab.breaddesk.ai.service.InquiryAiService;
import com.breadlab.breaddesk.common.exception.BusinessException;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryResponse;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.task.dto.TaskRequest;
import com.breadlab.breaddesk.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository messageRepository;
    private final InquiryAiService aiService;
    private final TaskService taskService;

    @Transactional
    public InquiryResponse createInquiry(InquiryRequest request) {
        // 문의 생성
        Inquiry inquiry = Inquiry.builder()
                .channel(request.getChannel())
                .channelMeta(request.getChannelMeta())
                .senderName(request.getSenderName())
                .senderEmail(request.getSenderEmail())
                .message(request.getMessage())
                .status(Inquiry.InquiryStatus.OPEN)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);

        // 사용자 메시지 기록
        saveMessage(saved.getId(), InquiryMessage.MessageRole.USER, request.getMessage());

        // AI 자동 답변 생성
        InquiryAiService.AiAnswerResult aiResult = aiService.generateAnswer(request.getMessage());
        saved.setAiResponse(aiResult.getAnswer());
        saved.setAiConfidence(aiResult.getConfidence());

        // 신뢰도에 따른 처리
        if (aiResult.getConfidence() >= 0.8) {
            // 높은 신뢰도: 자동 답변
            saved.setStatus(Inquiry.InquiryStatus.AI_ANSWERED);
            saveMessage(saved.getId(), InquiryMessage.MessageRole.AI, aiResult.getAnswer());
            log.info("High confidence AI answer for inquiry {}", saved.getId());
        } else if (aiResult.getConfidence() >= 0.5) {
            // 중간 신뢰도: 답변 + 담당자 알림
            saved.setStatus(Inquiry.InquiryStatus.AI_ANSWERED);
            saveMessage(saved.getId(), InquiryMessage.MessageRole.AI, aiResult.getAnswer());
            log.warn("Medium confidence AI answer for inquiry {} - human review needed", saved.getId());
            // TODO: 담당자 알림 (Phase 3)
        } else {
            // 낮은 신뢰도: 에스컬레이션
            escalateToTask(saved);
            log.info("Low confidence - escalated inquiry {} to task", saved.getId());
        }

        inquiryRepository.save(saved);
        log.info("Created inquiry: {} from {}", saved.getId(), saved.getSenderName());

        return getInquiry(saved.getId());
    }

    @Transactional(readOnly = true)
    public InquiryResponse getInquiry(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", id));

        List<InquiryMessage> messages = messageRepository.findByInquiryIdOrderByCreatedAt(id);
        return InquiryResponse.from(inquiry, messages);
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponse> getInquiries(Inquiry.InquiryStatus status, Pageable pageable) {
        Page<Inquiry> inquiries = status != null
                ? inquiryRepository.findByStatus(status, pageable)
                : inquiryRepository.findAll(pageable);

        return inquiries.map(InquiryResponse::from);
    }

    @Transactional
    public InquiryResponse replyToInquiry(Long id, InquiryRequest.Reply request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", id));

        if (inquiry.getStatus() == Inquiry.InquiryStatus.CLOSED) {
            throw new BusinessException("Cannot reply to closed inquiry", "INQUIRY_CLOSED");
        }

        // 담당자 답변 기록
        saveMessage(id, InquiryMessage.MessageRole.AGENT, request.getMessage());

        inquiry.setStatus(Inquiry.InquiryStatus.RESOLVED);
        inquiry.setResolvedBy(Inquiry.ResolvedBy.HUMAN);
        inquiry.setResolvedAt(LocalDateTime.now());
        inquiryRepository.save(inquiry);

        log.info("Agent replied to inquiry: {}", id);
        return getInquiry(id);
    }

    @Transactional
    public InquiryResponse submitFeedback(Long id, InquiryRequest.Feedback request) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry", id));

        if (request.getResolved() != null && request.getResolved()) {
            // 해결됨
            inquiry.setStatus(Inquiry.InquiryStatus.RESOLVED);
            if (inquiry.getResolvedBy() == null) {
                inquiry.setResolvedBy(Inquiry.ResolvedBy.AI);
            }
            inquiry.setResolvedAt(LocalDateTime.now());
            log.info("Inquiry {} marked as resolved by user", id);
        } else {
            // 미해결 → 에스컬레이션
            if (inquiry.getTaskId() == null) {
                escalateToTask(inquiry);
                log.info("Inquiry {} escalated after negative feedback", id);
            }
        }

        inquiryRepository.save(inquiry);
        return getInquiry(id);
    }

    private void saveMessage(Long inquiryId, InquiryMessage.MessageRole role, String message) {
        InquiryMessage msg = InquiryMessage.builder()
                .inquiryId(inquiryId)
                .role(role)
                .message(message)
                .build();
        messageRepository.save(msg);
    }

    private void escalateToTask(Inquiry inquiry) {
        // 업무로 자동 전환
        TaskRequest taskRequest = TaskRequest.builder()
                .title(String.format("문의 에스컬레이션 - %s", inquiry.getSenderName()))
                .description(String.format("문의 내용:\n%s\n\nAI 답변:\n%s", inquiry.getMessage(), inquiry.getAiResponse()))
                .type("GENERAL")
                .requesterName(inquiry.getSenderName())
                .requesterEmail(inquiry.getSenderEmail())
                .build();

        var task = taskService.createTask(taskRequest);

        inquiry.setTaskId(task.getId());
        inquiry.setStatus(Inquiry.InquiryStatus.ESCALATED);
        inquiryRepository.save(inquiry);

        log.info("Escalated inquiry {} to task {}", inquiry.getId(), task.getId());
    }
}
