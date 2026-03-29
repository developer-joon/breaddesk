package com.breadlab.breaddesk.template.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String category;

    @NotBlank(message = "Content is required")
    private String content;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {
        private String title;
        private String category;
        private String content;
    }
}
