package com.breadlab.breaddesk.channel.kakao;

import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Kakao API client for sending replies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoApiClient {

    private final ChannelConfigRepository channelConfigRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    /**
     * Send reply via Kakao API
     * 
     * @param configId Channel config ID
     * @param userKey Kakao user key
     * @param message Message content
     */
    public void sendReply(Long configId, String userKey, String message) throws Exception {
        ChannelConfig config = channelConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Channel config not found: " + configId));

        JsonNode credentials = objectMapper.readTree(config.getCredentials());
        String apiUrl = credentials.path("apiUrl").asText();
        String apiKey = credentials.path("apiKey").asText();

        // Build Kakao message payload
        var payload = objectMapper.createObjectNode();
        payload.put("user_key", userKey);
        
        var messageNode = objectMapper.createObjectNode();
        messageNode.put("text", message);
        payload.set("message", messageNode);

        try {
            webClient.post()
                    .uri(apiUrl + "/v1/api/talk/send")
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Sent Kakao message to user: {}", userKey);
        } catch (Exception e) {
            log.error("Failed to send Kakao message: {}", e.getMessage(), e);
            throw e;
        }
    }
}
