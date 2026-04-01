package com.breadlab.breaddesk.channel.kakao;

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
 * Kakao webhook controller
 * Receives callbacks from Kakao messaging platform
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/kakao")
@RequiredArgsConstructor
public class KakaoWebhookController {

    private final ChannelMessageRepository channelMessageRepository;
    private final ObjectMapper objectMapper;

    /**
     * Receive Kakao message callback
     * Kakao sends POST with JSON payload
     */
    @PostMapping
    public ResponseEntity<?> receiveKakaoMessage(@RequestBody String payload) {
        try {
            log.debug("Received Kakao webhook: {}", payload);
            
            JsonNode root = objectMapper.readTree(payload);
            
            // Parse Kakao message format
            String userKey = root.path("user_key").asText();
            String content = root.path("content").asText();
            String type = root.path("type").asText("text");

            // Create sender info
            var senderInfo = objectMapper.createObjectNode();
            senderInfo.put("userKey", userKey);
            senderInfo.put("platform", "kakao");

            // Store original payload as metadata
            var metadata = objectMapper.createObjectNode();
            metadata.put("type", type);
            metadata.put("rawPayload", payload);

            ChannelMessage message = ChannelMessage.builder()
                    .channelType(ChannelType.KAKAO)
                    .source("kakao:" + userKey)
                    .content(content)
                    .senderInfo(senderInfo.toString())
                    .channelMetadata(metadata.toString())
                    .createdAt(LocalDateTime.now())
                    .processed(false)
                    .build();

            channelMessageRepository.save(message);

            // Kakao expects success response
            return ResponseEntity.ok(objectMapper.createObjectNode()
                    .put("status", "success")
                    .put("message", "Message received"));

        } catch (Exception e) {
            log.error("Failed to process Kakao webhook: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to process webhook");
        }
    }

    /**
     * Kakao keyboard endpoint (required for some Kakao integrations)
     */
    @GetMapping("/keyboard")
    public ResponseEntity<?> getKeyboard() {
        try {
            var keyboard = objectMapper.createObjectNode();
            keyboard.put("type", "text");
            return ResponseEntity.ok(keyboard);
        } catch (Exception e) {
            log.error("Failed to generate keyboard: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error");
        }
    }
}
