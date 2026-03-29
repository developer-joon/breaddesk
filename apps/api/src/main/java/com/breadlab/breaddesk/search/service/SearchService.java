package com.breadlab.breaddesk.search.service;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.search.dto.SearchResponse;
import com.breadlab.breaddesk.search.dto.SearchResponse.SearchResultItem;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import com.breadlab.breaddesk.template.repository.ReplyTemplateRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통합 검색 서비스.
 * Phase 1: LIKE 검색.
 * Phase 2: pgvector 기반 시맨틱 검색으로 전환 예정.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final InquiryRepository inquiryRepository;
    private final TaskRepository taskRepository;
    private final ReplyTemplateRepository replyTemplateRepository;

    public SearchResponse search(String query) {
        List<SearchResultItem> results = new ArrayList<>();

        // 문의 검색
        List<Inquiry> inquiries = inquiryRepository.searchByKeyword(query);
        for (Inquiry inquiry : inquiries) {
            results.add(SearchResultItem.builder()
                    .type("INQUIRY")
                    .id(inquiry.getId())
                    .title(truncate(inquiry.getMessage(), 100))
                    .snippet(truncate(inquiry.getMessage(), 200))
                    .status(inquiry.getStatus().name())
                    .build());
        }

        // 업무 검색
        List<Task> tasks = taskRepository.searchByKeyword(query);
        for (Task task : tasks) {
            results.add(SearchResultItem.builder()
                    .type("TASK")
                    .id(task.getId())
                    .title(task.getTitle())
                    .snippet(truncate(task.getDescription(), 200))
                    .status(task.getStatus().name())
                    .build());
        }

        // 템플릿 검색
        List<ReplyTemplate> templates = replyTemplateRepository.searchByKeyword(query);
        for (ReplyTemplate template : templates) {
            results.add(SearchResultItem.builder()
                    .type("TEMPLATE")
                    .id(template.getId())
                    .title(template.getTitle())
                    .snippet(truncate(template.getContent(), 200))
                    .status(null)
                    .build());
        }

        return SearchResponse.builder()
                .query(query)
                .results(results)
                .totalCount(results.size())
                .build();
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
