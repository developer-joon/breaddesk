package com.breadlab.breaddesk.channel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookOutboundPayload {

    private Long inquiryId;
    private String channel;
    private String recipientEmail;
    private String recipientName;
    private String message;
    private String resolvedBy;
    private String metadata;
}
