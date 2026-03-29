package com.breadlab.breaddesk.inquiry.dto;

import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import jakarta.validation.constraints.NotNull;
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
public class InquiryStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private InquiryStatus status;
}
