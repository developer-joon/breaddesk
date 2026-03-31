package com.breadlab.breaddesk.channel.adapter;

import com.breadlab.breaddesk.channel.dto.WebhookIncomingRequest;
import com.breadlab.breaddesk.channel.dto.WebhookOutboundPayload;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;

/**
 * Adapter interface for channel-specific message format conversion.
 * Each channel (Slack, Teams, Email, etc.) implements this to handle
 * inbound message normalization and outbound message formatting.
 */
public interface ChannelAdapter {

    /** The channel type this adapter handles (e.g. "slack", "teams", "email"). */
    String getChannelType();

    /** Convert an incoming webhook payload into a normalized InquiryRequest. */
    InquiryRequest receiveMessage(WebhookIncomingRequest payload);

    /** Format an outbound payload for this channel's specific requirements. */
    WebhookOutboundPayload formatOutbound(Long inquiryId, String recipientEmail,
                                           String recipientName, String message,
                                           String resolvedBy, String channelMeta);
}
