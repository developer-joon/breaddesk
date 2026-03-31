package com.breadlab.breaddesk.channel.adapter;

import com.breadlab.breaddesk.channel.dto.WebhookIncomingRequest;
import com.breadlab.breaddesk.channel.dto.WebhookOutboundPayload;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailAdapter implements ChannelAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public String getChannelType() {
        return "email";
    }

    @Override
    public InquiryRequest receiveMessage(WebhookIncomingRequest payload) {
        return InquiryRequest.builder()
                .channel("email")
                .channelMeta(payload.getMetadata())
                .senderName(payload.getSenderName())
                .senderEmail(payload.getSenderEmail())
                .message(payload.getMessage())
                .build();
    }

    @Override
    public WebhookOutboundPayload formatOutbound(Long inquiryId, String recipientEmail,
                                                  String recipientName, String message,
                                                  String resolvedBy, String channelMeta) {
        // Format as HTML email body
        String emailMessage = String.format("""
                <h3>BreadDesk 답변 (문의 #%d)</h3>
                <p>안녕하세요 %s님,</p>
                <p>%s</p>
                <hr/>
                <p><small>답변: %s | BreadDesk 고객지원</small></p>
                """,
                inquiryId,
                recipientName != null ? recipientName : "고객",
                message,
                resolvedBy != null ? resolvedBy : "AI");

        String metadata = channelMeta;
        if (metadata == null) {
            try {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("format", "html");
                node.put("subject", "Re: 문의 #" + inquiryId + " 답변");
                metadata = objectMapper.writeValueAsString(node);
            } catch (Exception e) {
                metadata = "{}";
            }
        }

        return WebhookOutboundPayload.builder()
                .inquiryId(inquiryId)
                .channel("email")
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .message(emailMessage)
                .resolvedBy(resolvedBy)
                .metadata(metadata)
                .build();
    }
}
