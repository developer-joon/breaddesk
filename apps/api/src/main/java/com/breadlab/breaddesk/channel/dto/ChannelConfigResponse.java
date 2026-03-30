package com.breadlab.breaddesk.channel.dto;

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
public class ChannelConfigResponse {

    private Long id;
    private String channelType;
    private String webhookUrl;
    private Boolean isActive;
    private String config;
    private boolean hasAuthToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
