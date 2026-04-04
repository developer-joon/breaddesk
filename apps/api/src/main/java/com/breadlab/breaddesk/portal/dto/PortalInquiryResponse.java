package com.breadlab.breaddesk.portal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortalInquiryResponse {
    private Long id;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private List<PortalMessageResponse> messages;
}
