package com.breadlab.breaddesk.portal;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Customer Portal Controller
 * Public endpoints for customers to view inquiry status
 * No authentication required - access via secure token
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;

    /**
     * GET /api/v1/portal/{token}
     * View inquiry status via public token
     * Token is generated when inquiry is created
     */
    @GetMapping("/{token}")
    public ResponseEntity<?> getInquiryStatus(@PathVariable String token) {
        try {
            // In production, store token mapping in database
            // For now, use simple encoding: token = "INQ-{id}-{hash}"
            Long inquiryId = extractInquiryIdFromToken(token);
            
            if (inquiryId == null) {
                return ResponseEntity.badRequest().body("Invalid token");
            }

            Inquiry inquiry = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

            // Validate token (simple hash check)
            if (!validateToken(token, inquiry)) {
                return ResponseEntity.status(403).body("Invalid or expired token");
            }

            List<InquiryMessage> messages = inquiryMessageRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId);

            PortalInquiryView view = buildPortalView(inquiry, messages);
            return ResponseEntity.ok(view);

        } catch (Exception e) {
            log.error("Failed to get inquiry status: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to retrieve inquiry status");
        }
    }

    /**
     * POST /api/v1/portal/{token}/message
     * Add follow-up message from customer
     */
    @PostMapping("/{token}/message")
    public ResponseEntity<?> addMessage(
            @PathVariable String token,
            @RequestBody Map<String, String> request) {
        
        try {
            Long inquiryId = extractInquiryIdFromToken(token);
            Inquiry inquiry = inquiryRepository.findById(inquiryId)
                    .orElseThrow(() -> new IllegalArgumentException("Inquiry not found"));

            if (!validateToken(token, inquiry)) {
                return ResponseEntity.status(403).body("Invalid token");
            }

            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message cannot be empty");
            }

            InquiryMessage msg = InquiryMessage.builder()
                    .inquiry(inquiry)
                    .role(com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole.USER)
                    .message(message)
                    .createdAt(LocalDateTime.now())
                    .build();

            inquiryMessageRepository.save(msg);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Message added"
            ));

        } catch (Exception e) {
            log.error("Failed to add message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to add message");
        }
    }

    /**
     * Generate public portal token for inquiry
     */
    public static String generateToken(Long inquiryId) {
        // Simple token format: INQ-{id}-{hash}
        // In production, use proper UUID stored in database
        String hash = Integer.toHexString((inquiryId.toString() + "breaddesk-secret").hashCode());
        return "INQ-" + inquiryId + "-" + hash;
    }

    private Long extractInquiryIdFromToken(String token) {
        try {
            // Format: INQ-{id}-{hash}
            String[] parts = token.split("-");
            if (parts.length >= 2 && "INQ".equals(parts[0])) {
                return Long.parseLong(parts[1]);
            }
        } catch (Exception e) {
            log.debug("Invalid token format: {}", token);
        }
        return null;
    }

    private boolean validateToken(String token, Inquiry inquiry) {
        String expectedToken = generateToken(inquiry.getId());
        return expectedToken.equals(token);
    }

    private PortalInquiryView buildPortalView(Inquiry inquiry, List<InquiryMessage> messages) {
        return new PortalInquiryView(
                inquiry.getId(),
                inquiry.getStatus().toString(),
                inquiry.getMessage(),
                inquiry.getCreatedAt(),
                inquiry.getResolvedAt(),
                inquiry.getAiResponse(),
                messages.stream()
                        .map(m -> new MessageView(
                                m.getRole() != null ? m.getRole().toString() : "UNKNOWN",
                                m.getMessage(),
                                m.getCreatedAt()
                        ))
                        .collect(Collectors.toList())
        );
    }

    // DTOs
    public record PortalInquiryView(
            Long id,
            String status,
            String originalMessage,
            LocalDateTime createdAt,
            LocalDateTime resolvedAt,
            String response,
            List<MessageView> messages
    ) {}

    public record MessageView(
            String senderType,
            String message,
            LocalDateTime createdAt
    ) {}
}
