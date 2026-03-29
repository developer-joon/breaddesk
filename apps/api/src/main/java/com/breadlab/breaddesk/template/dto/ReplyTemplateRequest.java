package com.breadlab.breaddesk.template.dto;

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
public class ReplyTemplateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String category;

    @NotBlank(message = "Content is required")
    private String content;
}
