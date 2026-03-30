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
public class SlackAdapter implements ChannelAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public String getChannelType() {
        return "slack";
    }

    @Override
    public InquiryRequest receiveMessage(WebhookIncomingRequest payload) {
        return InquiryRequest.builder()
                .channel("slack")
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
        // Format message in Slack mrkdwn style
        String slackMessage = String.format(
                "*BreadDesk 답변* (문의 #%d)\n\n%s\n\n_답변: %s_",
                inquiryId, message, resolvedBy != null ? resolvedBy : "AI");

        // Preserve channel metadata for routing back to the correct Slack channel
        String metadata = channelMeta;
        if (metadata == null) {
            try {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("format", "mrkdwn");
                metadata = objectMapper.writeValueAsString(node);
            } catch (Exception e) {
                metadata = "{}";
            }
        }

        return WebhookOutboundPayload.builder()
                .inquiryId(inquiryId)
                .channel("slack")
                .recipientEmail(recipientEmail)
                .recipientName(recipientName)
                .message(slackMessage)
                .resolvedBy(resolvedBy)
                .metadata(metadata)
                .build();
    }
}
