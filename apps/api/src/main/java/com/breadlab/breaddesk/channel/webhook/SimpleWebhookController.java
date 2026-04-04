package com.breadlab.breaddesk.channel.webhook;

import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryResponse;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Simple Webhook Controller for n8n integration
 * 
 * Provides a simplified API endpoint for external systems like n8n
 * to create inquiries without complex authentication flows.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/simple")
@RequiredArgsConstructor
public class SimpleWebhookController {

    private final InquiryService inquiryService;

    @Value("${breaddesk.webhook.default-token:}")
    private String webhookToken;

    /**
     * POST /api/v1/webhooks/simple
     * 
     * Simple webhook endpoint for creating inquiries from external systems
     * 
     * Authentication: X-Webhook-Token header
     * 
     * Request body:
     * {
     *   "channel": "email|kakao|slack|telegram|custom",
     *   "senderName": "김철수",
     *   "senderEmail": "cheolsu@test.com",  // optional
     *   "message": "문의 내용",
     *   "metadata": {}  // optional, additional information
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "data": {
     *     "inquiryId": 123,
     *     "status": "AI_ANSWERED",
     *     "aiResponse": "...",
     *     "aiConfidence": 0.85
     *   }
     * }
     */
    @PostMapping
    public ResponseEntity<?> handleSimpleWebhook(
            @RequestHeader(value = "X-Webhook-Token", required = false) String token,
            @RequestBody SimpleWebhookRequest request) {
        
        // Validate webhook token
        if (webhookToken.isBlank()) {
            log.warn("Webhook token not configured in application.yml");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Webhook token not configured"
                    ));
        }

        if (token == null || !webhookToken.equals(token)) {
            log.warn("Invalid webhook token received");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "success", false,
                            "error", "Invalid or missing X-Webhook-Token"
                    ));
        }

        // Validate required fields
        if (request.channel() == null || request.channel().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Field 'channel' is required"
                    ));
        }

        if (request.senderName() == null || request.senderName().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Field 'senderName' is required"
                    ));
        }

        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "success", false,
                            "error", "Field 'message' is required"
                    ));
        }

        try {
            // Convert to standard InquiryRequest
            InquiryRequest inquiryRequest = new InquiryRequest();
            inquiryRequest.setChannel(request.channel());
            inquiryRequest.setSenderName(request.senderName());
            inquiryRequest.setSenderEmail(request.senderEmail());
            inquiryRequest.setMessage(request.message());
            
            // Include metadata as channelMeta if provided
            if (request.metadata() != null && !request.metadata().isEmpty()) {
                inquiryRequest.setChannelMeta(request.metadata().toString());
            }

            // Create inquiry (AI auto-answer happens inside InquiryService)
            InquiryResponse response = inquiryService.createInquiry(inquiryRequest);

            log.info("Webhook inquiry created: ID={}, channel={}, status={}", 
                    response.getId(), request.channel(), response.getStatus());

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "inquiryId", response.getId(),
                            "status", response.getStatus().name(),
                            "aiResponse", response.getAiResponse() != null ? response.getAiResponse() : "",
                            "aiConfidence", response.getAiConfidence() != null ? response.getAiConfidence() : 0.0
                    )
            ));

        } catch (Exception e) {
            log.error("Failed to process webhook inquiry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Internal server error: " + e.getMessage()
                    ));
        }
    }

    /**
     * Simple webhook request DTO
     */
    public record SimpleWebhookRequest(
            String channel,
            String senderName,
            String senderEmail,
            String message,
            Map<String, Object> metadata
    ) {}
}
