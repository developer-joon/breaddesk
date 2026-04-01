package com.breadlab.breaddesk.channel.webhook;

import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.entity.ChannelMessage;
import com.breadlab.breaddesk.channel.entity.ChannelType;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.breadlab.breaddesk.channel.repository.ChannelMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Generic webhook controller with configurable payload mapping
 * Allows custom integrations via webhook
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/inbound")
@RequiredArgsConstructor
public class GenericWebhookController {

    private final ChannelConfigRepository channelConfigRepository;
    private final ChannelMessageRepository channelMessageRepository;
    private final ObjectMapper objectMapper;

    /**
     * Receive generic webhook with configurable mapping
     * Path variable {channelId} identifies the channel configuration
     */
    @PostMapping("/{channelId}")
    public ResponseEntity<?> receiveWebhook(
            @PathVariable Long channelId,
            @RequestBody String payload,
            @RequestHeader(value = "X-Webhook-Secret", required = false) String secret) {
        
        try {
            log.debug("Received webhook for channel {}: {}", channelId, payload);

            // Validate channel config exists
            ChannelConfig config = channelConfigRepository.findById(channelId)
                    .orElseThrow(() -> new IllegalArgumentException("Channel not found: " + channelId));

            if (!config.getIsActive()) {
                return ResponseEntity.badRequest().body("Channel is disabled");
            }

            // Validate secret if configured
            JsonNode credentials = objectMapper.readTree(config.getConfig());
            String expectedSecret = credentials.path("webhookSecret").asText(null);
            if (expectedSecret != null && !expectedSecret.equals(secret)) {
                log.warn("Invalid webhook secret for channel {}", channelId);
                return ResponseEntity.status(401).body("Invalid secret");
            }

            // Parse payload using mapping configuration
            JsonNode root = objectMapper.readTree(payload);
            JsonNode mappingConfig = credentials.path("payloadMapping");

            // Extract fields based on mapping
            String contentPath = mappingConfig.path("contentPath").asText("content");
            String senderNamePath = mappingConfig.path("senderNamePath").asText("sender.name");
            String senderEmailPath = mappingConfig.path("senderEmailPath").asText("sender.email");
            String senderIdPath = mappingConfig.path("senderIdPath").asText("sender.id");

            String content = extractValue(root, contentPath);
            String senderName = extractValue(root, senderNamePath);
            String senderEmail = extractValue(root, senderEmailPath);
            String senderId = extractValue(root, senderIdPath);

            // Build sender info
            var senderInfo = objectMapper.createObjectNode();
            if (senderName != null) senderInfo.put("name", senderName);
            if (senderEmail != null) senderInfo.put("email", senderEmail);
            if (senderId != null) senderInfo.put("id", senderId);

            // Store full payload as metadata
            var metadata = objectMapper.createObjectNode();
            metadata.put("rawPayload", payload);
            metadata.put("channelConfigId", channelId);

            ChannelMessage message = ChannelMessage.builder()
                    .channelType(ChannelType.WEBHOOK)
                    .source("webhook:" + channelId + ":" + senderId)
                    .content(content != null ? content : payload)
                    .senderInfo(senderInfo.toString())
                    .channelMetadata(metadata.toString())
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            channelMessageRepository.save(message);

            return ResponseEntity.ok(objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("messageId", message.getId()));

        } catch (Exception e) {
            log.error("Failed to process webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to process webhook: " + e.getMessage());
        }
    }

    /**
     * Extract value from JSON using dot-notation path
     * e.g., "sender.name" -> root.get("sender").get("name")
     */
    private String extractValue(JsonNode root, String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        String[] parts = path.split("\\.");
        JsonNode current = root;
        
        for (String part : parts) {
            current = current.path(part);
            if (current.isMissingNode()) {
                return null;
            }
        }
        
        return current.isTextual() ? current.asText() : current.toString();
    }
}
