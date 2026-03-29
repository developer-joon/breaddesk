package com.breadlab.breaddesk.notification.dto;

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
public class NotificationResponse {

    private Long id;
    private Long memberId;
    private String type;
    private String title;
    private String message;
    private String link;
    private boolean read;
    private LocalDateTime createdAt;
}
