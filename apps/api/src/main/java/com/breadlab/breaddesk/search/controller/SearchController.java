package com.breadlab.breaddesk.search.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.search.dto.SearchResponse;
import com.breadlab.breaddesk.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<ApiResponse<SearchResponse>> search(
            @RequestParam("q") String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("검색어를 입력해주세요."));
        }

        SearchResponse response = searchService.search(query.trim());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
