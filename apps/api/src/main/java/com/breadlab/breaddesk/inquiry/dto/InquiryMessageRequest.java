package com.breadlab.breaddesk.inquiry.dto;

import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import jakarta.validation.constraints.NotBlank;
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
public class InquiryMessageRequest {

    @NotNull(message = "Role is required")
    private InquiryMessageRole role;

    @NotBlank(message = "Message is required")
    private String message;
}
