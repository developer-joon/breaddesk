package com.breadlab.breaddesk.webchat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebchatMessageResponse {
    private Long messageId;
    private String role;
    private String message;
    private String aiResponse;
    private Float aiConfidence;
    private LocalDateTime createdAt;
}
