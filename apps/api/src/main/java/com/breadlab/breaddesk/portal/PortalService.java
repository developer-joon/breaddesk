package com.breadlab.breaddesk.portal;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.portal.dto.PortalInquiryResponse;
import com.breadlab.breaddesk.portal.dto.PortalMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortalService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;

    /**
     * Generate portal token from inquiry ID
     */
    public String generatePortalToken(Long inquiryId) {
        String raw = "INQ-" + inquiryId;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parse inquiry ID from portal token
     */
    private Long parseInquiryIdFromToken(String token) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            if (!decoded.startsWith("INQ-")) {
                throw new IllegalArgumentException("Invalid token format");
            }
            return Long.parseLong(decoded.substring(4));
        } catch (Exception e) {
            log.error("Failed to parse portal token: {}", token, e);
            throw new ResourceNotFoundException("Invalid or expired portal token");
        }
    }

    /**
     * Get inquiry by portal token (public access)
     */
    public PortalInquiryResponse getInquiryByToken(String token) {
        Long inquiryId = parseInquiryIdFromToken(token);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));

        List<PortalMessageResponse> messages = inquiryMessageRepository
                .findByInquiryIdOrderByCreatedAtAsc(inquiryId)
                .stream()
                .map(this::toPortalMessageResponse)
                .collect(Collectors.toList());

        return PortalInquiryResponse.builder()
                .id(inquiry.getId())
                .message(inquiry.getMessage())
                .status(inquiry.getStatus().name())
                .createdAt(inquiry.getCreatedAt())
                .messages(messages)
                .build();
    }

    /**
     * Add message from customer via portal
     */
    @Transactional
    public void addMessageViaPortal(String token, String messageContent) {
        Long inquiryId = parseInquiryIdFromToken(token);
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("Inquiry not found"));

        InquiryMessage message = InquiryMessage.builder()
                .inquiry(inquiry)
                .role(InquiryMessageRole.USER)
                .message(messageContent)
                .createdAt(LocalDateTime.now())
                .build();

        inquiryMessageRepository.save(message);
        log.info("Portal message saved for inquiry #{}", inquiryId);
    }

    private PortalMessageResponse toPortalMessageResponse(InquiryMessage msg) {
        return PortalMessageResponse.builder()
                .id(msg.getId())
                .role(msg.getRole().name())
                .message(msg.getMessage())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
