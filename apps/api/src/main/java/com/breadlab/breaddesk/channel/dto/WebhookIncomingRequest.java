package com.breadlab.breaddesk.channel.dto;

import jakarta.validation.constraints.NotBlank;
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
public class WebhookIncomingRequest {

    @NotBlank(message = "Source channel is required")
    private String source;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    private String senderEmail;

    @NotBlank(message = "Message is required")
    private String message;

    /** Arbitrary metadata from the source channel (JSON string) */
    private String metadata;
}
