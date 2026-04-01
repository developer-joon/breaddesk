package com.breadlab.breaddesk.channel.webchat;

import com.breadlab.breaddesk.channel.entity.ChannelMessage;
import com.breadlab.breaddesk.channel.entity.ChannelType;
import com.breadlab.breaddesk.channel.repository.ChannelMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Web chat REST controller for anonymous public chat
 * No authentication required
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class WebChatController {

    private final ChannelMessageRepository channelMessageRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/messages")
    public ResponseEntity<?> receiveMessage(@RequestBody WebChatMessageRequest request) {
        try {
            // Generate session ID if not provided
            String sessionId = request.getSessionId() != null ? 
                    request.getSessionId() : UUID.randomUUID().toString();

            // Create sender info
            var senderInfo = objectMapper.createObjectNode();
            senderInfo.put("sessionId", sessionId);
            senderInfo.put("name", request.getName() != null ? request.getName() : "Anonymous");
            if (request.getEmail() != null) {
                senderInfo.put("email", request.getEmail());
            }

            // Create metadata
            var metadata = objectMapper.createObjectNode();
            metadata.put("sessionId", sessionId);
            metadata.put("userAgent", request.getUserAgent());
            metadata.put("ipAddress", request.getIpAddress());

            ChannelMessage message = ChannelMessage.builder()
                    .channelType(ChannelType.WEB_CHAT)
                    .source("webchat:" + sessionId)
                    .content(request.getMessage())
                    .senderInfo(senderInfo.toString())
                    .channelMetadata(metadata.toString())
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            channelMessageRepository.save(message);

            return ResponseEntity.ok(new WebChatMessageResponse(
                    sessionId,
                    "Message received",
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Failed to process web chat message: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to process message");
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getHistory(@RequestParam String sessionId) {
        try {
            String source = "webchat:" + sessionId;
            List<ChannelMessage> messages = channelMessageRepository
                    .findBySourceOrderByCreatedAtAsc(source);

            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            log.error("Failed to get chat history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to get history");
        }
    }

    /**
     * Health check endpoint for chat widget
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok("OK");
    }

    // DTOs
    public record WebChatMessageRequest(
            String sessionId,
            String name,
            String email,
            String message,
            String userAgent,
            String ipAddress
    ) {}

    public record WebChatMessageResponse(
            String sessionId,
            String status,
            LocalDateTime timestamp
    ) {}
}
