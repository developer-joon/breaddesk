package com.breadlab.breaddesk.inquiry.dto;

import java.util.List;

public record TaskPreviewResponse(
        String title,
        String description,
        List<String> checklist,
        String category,
        String urgency,
        String requesterName,
        String requesterEmail,
        Long inquiryId
) {}
