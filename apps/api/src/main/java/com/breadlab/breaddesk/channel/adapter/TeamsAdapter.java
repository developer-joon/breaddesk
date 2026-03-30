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
public class TeamsAdapter implements ChannelAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public String getChannelType() {
        return "teams";
    }

    @Override
    public InquiryRequest receiveMessage(WebhookIncomingRequest payload) {
        return InquiryRequest.builder()
                .channel("teams")
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
        // Format as Teams Adaptive Card-compatible text
        String teamsMessage = String.format(
                "**BreadDesk 답변** (문의 #%d)\n\n%s\n\n*답변: %s*",
                inquiryId, message, resolvedBy != null ? resolvedBy : "AI");

        String metadata = channelMeta;
        if (metadata == null) {
            try {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("format", "markdown");
                metadata = objectMapper.writeValueAsString(node);
            } catch (Exception e) {
                metadata = "{}";
            }
        }

        return WebhookOutboundPayload.builder()
                .inquiryId(inquiryId)
                .channel("teams")
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .message(teamsMessage)
                .resolvedBy(resolvedBy)
                .metadata(metadata)
                .build();
    }
}
