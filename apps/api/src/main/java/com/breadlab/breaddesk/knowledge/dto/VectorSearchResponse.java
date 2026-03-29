package com.breadlab.breaddesk.knowledge.dto;

import com.breadlab.breaddesk.knowledge.service.VectorSearchService;

public record VectorSearchResponse(
        Long id, String source, String title,
        String content, String url, double similarity
) {
    public static VectorSearchResponse from(VectorSearchService.SearchResult result) {
        return new VectorSearchResponse(
                result.id(), result.source(), result.title(),
                result.content(), result.url(), result.similarity()
        );
    }
}
