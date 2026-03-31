package com.breadlab.breaddesk.inquiry.dto;

import jakarta.validation.constraints.Email;
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
public class InquiryRequest {

    @NotBlank(message = "Channel is required")
    private String channel;

    private String channelMeta;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    @Email(message = "Invalid email format")
    private String senderEmail;

    @NotBlank(message = "Message is required")
    private String message;
}
