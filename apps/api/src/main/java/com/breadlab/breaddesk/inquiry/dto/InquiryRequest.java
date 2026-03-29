package com.breadlab.breaddesk.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequest {

    @NotBlank(message = "Channel is required")
    private String channel;

    private Map<String, Object> channelMeta;

    @NotBlank(message = "Sender name is required")
    private String senderName;

    private String senderEmail;

    @NotBlank(message = "Message is required")
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reply {
        @NotBlank(message = "Message is required")
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Feedback {
        private Boolean resolved;
        private String comment;
    }
}
