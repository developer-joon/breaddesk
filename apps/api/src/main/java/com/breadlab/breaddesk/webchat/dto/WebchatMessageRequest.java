package com.breadlab.breaddesk.webchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class WebchatMessageRequest {
    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message must be 5000 characters or less")
    private String message;
}
