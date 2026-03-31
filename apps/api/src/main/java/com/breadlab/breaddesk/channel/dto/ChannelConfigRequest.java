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
public class ChannelConfigRequest {

    @NotBlank(message = "Channel type is required")
    private String channelType;

    private String webhookUrl;

    private String authToken;

    private Boolean isActive;

    private String config;
}
