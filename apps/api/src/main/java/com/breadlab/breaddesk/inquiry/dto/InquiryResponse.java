package com.breadlab.breaddesk.inquiry.dto;

import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponse {

    private Long id;
    private String channel;
    private String channelMeta;
    private String senderName;
    private String senderEmail;
    private String message;
    private String aiResponse;
    private Float aiConfidence;
    private InquiryStatus status;
    private Long taskId;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private List<InquiryMessageResponse> messages;
}
