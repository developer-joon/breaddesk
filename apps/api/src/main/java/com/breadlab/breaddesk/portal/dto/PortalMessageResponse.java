package com.breadlab.breaddesk.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalMessageResponse {
    private Long id;
    private String role;
    private String message;
    private LocalDateTime createdAt;
}
