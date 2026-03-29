package com.breadlab.breaddesk.inquiry.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Pattern(regexp = "slack|teams|jira|web|email", message = "Invalid channel type")
    @Size(max = 50)
    private String channel;

    private Map<String, Object> channelMeta;

    @NotBlank(message = "Sender name is required")
    @Size(min = 1, max = 100)
    private String senderName;

    @Email(message = "Invalid email format")
    @Size(max = 200)
    private String senderEmail;

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 10000)
    private String message;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Reply {
        @NotBlank(message = "Message is required")
        @Size(min = 1, max = 10000)
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
