package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.email.EmailSenderService;
import com.breadlab.breaddesk.channel.entity.ChannelType;
import com.breadlab.breaddesk.channel.kakao.KakaoApiClient;
import com.breadlab.breaddesk.channel.telegram.TelegramApiClient;
import com.breadlab.breaddesk.channel.webchat.ChatWebSocketHandler;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Routes replies back to the original channel
 * Detects channel type from inquiry and sends via appropriate service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelReplyService {

    private final EmailSenderService emailSenderService;
    private final TelegramApiClient telegramApiClient;
    private final KakaoApiClient kakaoApiClient;
    private final ChatWebSocketHandler chatWebSocketHandler;
    private final ObjectMapper objectMapper;

    /**
     * Send reply to customer via original channel
     * 
     * @param inquiry Original inquiry
     * @param message Reply message
     * @param resolvedBy Who resolved it (agent name or "AI")
     */
    public void sendReply(Inquiry inquiry, String message, String resolvedBy) {
        if (inquiry.getChannel() == null) {
            log.warn("Inquiry #{} has no channel, cannot send reply", inquiry.getId());
            return;
        }

        try {
            String channel = inquiry.getChannel().toLowerCase();
            
            if (channel.startsWith("email")) {
                sendEmailReply(inquiry, message, resolvedBy);
            } else if (channel.startsWith("telegram")) {
                sendTelegramReply(inquiry, message, resolvedBy);
            } else if (channel.startsWith("kakao")) {
                sendKakaoReply(inquiry, message, resolvedBy);
            } else if (channel.startsWith("webchat") || channel.startsWith("web_chat")) {
                sendWebChatReply(inquiry, message, resolvedBy);
            } else if (channel.startsWith("webhook")) {
                sendWebhookReply(inquiry, message, resolvedBy);
            } else {
                log.warn("Unknown channel type for inquiry #{}: {}", inquiry.getId(), channel);
            }
        } catch (Exception e) {
            log.error("Failed to send reply for inquiry #{}: {}", inquiry.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to send reply", e);
        }
    }

    private void sendEmailReply(Inquiry inquiry, String message, String resolvedBy) throws Exception {
        String recipientEmail = inquiry.getSenderEmail();
        String recipientName = inquiry.getSenderName();
        
        if (recipientEmail == null || recipientEmail.isEmpty()) {
            log.warn("No email address for inquiry #{}", inquiry.getId());
            return;
        }

        // Extract config ID from channelMeta if available
        Long configId = extractConfigId(inquiry.getChannelMeta());
        
        String subject = "Re: 문의 #" + inquiry.getId();
        String body = formatEmailBody(inquiry, message, resolvedBy);
        
        // Extract In-Reply-To from metadata
        String inReplyTo = null;
        if (inquiry.getChannelMeta() != null) {
            JsonNode meta = objectMapper.readTree(inquiry.getChannelMeta());
            inReplyTo = meta.path("messageId").asText(null);
        }

        if (configId != null) {
            emailSenderService.sendReply(configId, recipientEmail, recipientName, subject, body, inReplyTo);
        } else {
            log.warn("No email config ID found for inquiry #{}", inquiry.getId());
        }
    }

    private void sendTelegramReply(Inquiry inquiry, String message, String resolvedBy) throws Exception {
        Long chatId = extractTelegramChatId(inquiry.getChannelMeta());
        Long configId = extractConfigId(inquiry.getChannelMeta());
        
        if (chatId == null || configId == null) {
            log.warn("Missing Telegram chatId or configId for inquiry #{}", inquiry.getId());
            return;
        }

        String formattedMessage = String.format(
                "<b>BreadDesk 답변 (문의 #%d)</b>\n\n%s\n\n<i>답변: %s</i>",
                inquiry.getId(),
                message,
                resolvedBy != null ? resolvedBy : "AI"
        );

        telegramApiClient.sendReply(configId, chatId, formattedMessage);
    }

    private void sendKakaoReply(Inquiry inquiry, String message, String resolvedBy) throws Exception {
        String userKey = extractKakaoUserKey(inquiry.getChannelMeta());
        Long configId = extractConfigId(inquiry.getChannelMeta());
        
        if (userKey == null || configId == null) {
            log.warn("Missing Kakao userKey or configId for inquiry #{}", inquiry.getId());
            return;
        }

        String formattedMessage = String.format(
                "[BreadDesk 답변 #%d]\n\n%s\n\n답변: %s",
                inquiry.getId(),
                message,
                resolvedBy != null ? resolvedBy : "AI"
        );

        kakaoApiClient.sendReply(configId, userKey, formattedMessage);
    }

    private void sendWebChatReply(Inquiry inquiry, String message, String resolvedBy) {
        String sessionId = extractWebChatSessionId(inquiry.getChannelMeta());
        
        if (sessionId == null) {
            log.warn("No webchat sessionId for inquiry #{}", inquiry.getId());
            return;
        }

        chatWebSocketHandler.sendToSession(sessionId, Map.of(
                "type", "reply",
                "inquiryId", inquiry.getId(),
                "message", message,
                "resolvedBy", resolvedBy != null ? resolvedBy : "AI"
        ));
    }

    private void sendWebhookReply(Inquiry inquiry, String message, String resolvedBy) {
        // For webhook, typically we don't send back — it's one-way
        // Or could use WebhookOutboundService if configured
        log.info("Webhook reply for inquiry #{} (not implemented - one-way)", inquiry.getId());
    }

    private String formatEmailBody(Inquiry inquiry, String message, String resolvedBy) {
        return String.format("""
                <html>
                <body>
                <h3>BreadDesk 답변 (문의 #%d)</h3>
                <p>안녕하세요 %s님,</p>
                <p>%s</p>
                <hr/>
                <p><small>답변: %s | BreadDesk 고객지원</small></p>
                </body>
                </html>
                """,
                inquiry.getId(),
                inquiry.getSenderName() != null ? inquiry.getSenderName() : "고객",
                message.replace("\n", "<br/>"),
                resolvedBy != null ? resolvedBy : "AI"
        );
    }

    private Long extractConfigId(String channelMeta) {
        try {
            if (channelMeta != null) {
                JsonNode meta = objectMapper.readTree(channelMeta);
                return meta.path("channelConfigId").asLong(1L); // Default to 1 if not found
            }
        } catch (Exception e) {
            log.debug("Could not extract configId: {}", e.getMessage());
        }
        return 1L; // Default fallback
    }

    private Long extractTelegramChatId(String channelMeta) {
        try {
            if (channelMeta != null) {
                JsonNode meta = objectMapper.readTree(channelMeta);
                return meta.path("chatId").asLong();
            }
        } catch (Exception e) {
            log.debug("Could not extract chatId: {}", e.getMessage());
        }
        return null;
    }

    private String extractKakaoUserKey(String channelMeta) {
        try {
            if (channelMeta != null) {
                JsonNode meta = objectMapper.readTree(channelMeta);
                return meta.path("userKey").asText(null);
            }
        } catch (Exception e) {
            log.debug("Could not extract userKey: {}", e.getMessage());
        }
        return null;
    }

    private String extractWebChatSessionId(String channelMeta) {
        try {
            if (channelMeta != null) {
                JsonNode meta = objectMapper.readTree(channelMeta);
                return meta.path("sessionId").asText(null);
            }
        } catch (Exception e) {
            log.debug("Could not extract sessionId: {}", e.getMessage());
        }
        return null;
    }
}
