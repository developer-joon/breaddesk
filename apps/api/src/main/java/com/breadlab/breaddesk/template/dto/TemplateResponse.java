package com.breadlab.breaddesk.template.dto;

import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
    private Long id;
    private String title;
    private String category;
    private String content;
    private Integer usageCount;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TemplateResponse from(ReplyTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .category(template.getCategory())
                .content(template.getContent())
                .usageCount(template.getUsageCount())
                .createdBy(template.getCreatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
