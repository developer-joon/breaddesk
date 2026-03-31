package com.breadlab.breaddesk.inquiry.dto;

import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
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
public class InquiryMessageResponse {

    private Long id;
    private Long inquiryId;
    private InquiryMessageRole role;
    private String message;
    private LocalDateTime createdAt;
}
