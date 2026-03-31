package com.breadlab.breaddesk.search.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SearchResponse {

    private final String query;
    private final List<SearchResultItem> results;
    private final int totalCount;

    @Getter
    @Builder
    public static class SearchResultItem {
        private final String type;      // INQUIRY, TASK, TEMPLATE
        private final Long id;
        private final String title;
        private final String snippet;
        private final String status;
    }
}
