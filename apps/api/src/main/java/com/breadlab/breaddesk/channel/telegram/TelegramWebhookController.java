package com.breadlab.breaddesk.channel.telegram;

import com.breadlab.breaddesk.channel.entity.ChannelMessage;
import com.breadlab.breaddesk.channel.entity.ChannelType;
import com.breadlab.breaddesk.channel.repository.ChannelMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * Telegram webhook controller
 * Receives updates from Telegram Bot API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/telegram")
@RequiredArgsConstructor
public class TelegramWebhookController {

    private final ChannelMessageRepository channelMessageRepository;
    private final ObjectMapper objectMapper;

    /**
     * Receive Telegram update
     * Telegram sends POST with Update object
     */
    @PostMapping
    public ResponseEntity<?> receiveTelegramUpdate(@RequestBody String payload) {
        try {
            log.debug("Received Telegram webhook: {}", payload);
            
            JsonNode root = objectMapper.readTree(payload);
            JsonNode message = root.path("message");
            
            if (message.isMissingNode()) {
                // Not a message update (could be callback_query, etc.)
                return ResponseEntity.ok("OK");
            }

            Long chatId = message.path("chat").path("id").asLong();
            String text = message.path("text").asText();
            JsonNode from = message.path("from");
            
            Long userId = from.path("id").asLong();
            String username = from.path("username").asText("unknown");
            String firstName = from.path("first_name").asText("");
            String lastName = from.path("last_name").asText("");
            String fullName = (firstName + " " + lastName).trim();

            // Create sender info
            var senderInfo = objectMapper.createObjectNode();
            senderInfo.put("userId", userId);
            senderInfo.put("username", username);
            senderInfo.put("firstName", firstName);
            senderInfo.put("lastName", lastName);
            senderInfo.put("fullName", fullName);

            // Store metadata for reply
            var metadata = objectMapper.createObjectNode();
            metadata.put("chatId", chatId);
            metadata.put("messageId", message.path("message_id").asLong());
            metadata.put("rawPayload", payload);

            ChannelMessage channelMessage = ChannelMessage.builder()
                    .channelType(ChannelType.TELEGRAM)
                    .source("telegram:" + chatId)
                    .content(text)
                    .senderInfo(senderInfo.toString())
                    .channelMetadata(metadata.toString())
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            channelMessageRepository.save(channelMessage);

            // Telegram expects 200 OK
            return ResponseEntity.ok("OK");

        } catch (Exception e) {
            log.error("Failed to process Telegram webhook: {}", e.getMessage(), e);
            // Still return 200 to avoid Telegram retries
            return ResponseEntity.ok("ERROR");
        }
    }
}
