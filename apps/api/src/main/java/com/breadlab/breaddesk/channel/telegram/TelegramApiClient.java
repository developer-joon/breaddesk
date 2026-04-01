package com.breadlab.breaddesk.channel.telegram;

import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Telegram Bot API client for sending replies
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramApiClient {

    private final ChannelConfigRepository channelConfigRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";

    /**
     * Send reply via Telegram Bot API
     * 
     * @param configId Channel config ID
     * @param chatId Telegram chat ID
     * @param message Message text
     */
    public void sendReply(Long configId, Long chatId, String message) throws Exception {
        ChannelConfig config = channelConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Channel config not found: " + configId));

        JsonNode credentials = objectMapper.readTree(config.getConfig());
        String botToken = credentials.path("botToken").asText();

        // Build Telegram message payload
        var payload = objectMapper.createObjectNode();
        payload.put("chat_id", chatId);
        payload.put("text", message);
        payload.put("parse_mode", "HTML"); // Support HTML formatting

        String apiUrl = TELEGRAM_API_BASE + botToken + "/sendMessage";

        try {
            String response = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Sent Telegram message to chat {}: {}", chatId, response);
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send reply with keyboard buttons
     */
    public void sendReplyWithKeyboard(Long configId, Long chatId, String message, 
                                     String[][] buttons) throws Exception {
        ChannelConfig config = channelConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Channel config not found: " + configId));

        JsonNode credentials = objectMapper.readTree(config.getConfig());
        String botToken = credentials.path("botToken").asText();

        var payload = objectMapper.createObjectNode();
        payload.put("chat_id", chatId);
        payload.put("text", message);
        payload.put("parse_mode", "HTML");

        // Add inline keyboard
        var keyboard = objectMapper.createObjectNode();
        var keyboardArray = objectMapper.createArrayNode();
        for (String[] row : buttons) {
            var rowArray = objectMapper.createArrayNode();
            for (String btnText : row) {
                var button = objectMapper.createObjectNode();
                button.put("text", btnText);
                button.put("callback_data", btnText);
                rowArray.add(button);
            }
            keyboardArray.add(rowArray);
        }
        keyboard.set("inline_keyboard", keyboardArray);
        payload.set("reply_markup", keyboard);

        String apiUrl = TELEGRAM_API_BASE + botToken + "/sendMessage";

        try {
            webClient.post()
                    .uri(apiUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Sent Telegram message with keyboard to chat {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send Telegram message: {}", e.getMessage(), e);
            throw e;
        }
    }
}
