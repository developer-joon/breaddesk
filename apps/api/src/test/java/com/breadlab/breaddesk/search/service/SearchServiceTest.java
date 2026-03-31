package com.breadlab.breaddesk.search.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.search.dto.SearchResponse;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import com.breadlab.breaddesk.template.repository.ReplyTemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ReplyTemplateRepository replyTemplateRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    @DisplayName("should_searchAll_when_queryProvided")
    void should_searchAll_when_queryProvided() {
        // Given
        Inquiry inquiry = TestDataFactory.createInquiry();
        inquiry.setId(1L);
        Task task = TestDataFactory.createTask();
        task.setId(1L);
        ReplyTemplate template = TestDataFactory.createReplyTemplate();
        template.setId(1L);

        given(inquiryRepository.searchByKeyword("test")).willReturn(List.of(inquiry));
        given(taskRepository.searchByKeyword("test")).willReturn(List.of(task));
        given(replyTemplateRepository.searchByKeyword("test")).willReturn(List.of(template));

        // When
        SearchResponse response = searchService.search("test");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getQuery()).isEqualTo("test");
        assertThat(response.getTotalCount()).isEqualTo(3);
        assertThat(response.getResults()).hasSize(3);
    }

    @Test
    @DisplayName("should_returnEmptyResults_when_noMatches")
    void should_returnEmptyResults_when_noMatches() {
        // Given
        given(inquiryRepository.searchByKeyword("nonexistent")).willReturn(List.of());
        given(taskRepository.searchByKeyword("nonexistent")).willReturn(List.of());
        given(replyTemplateRepository.searchByKeyword("nonexistent")).willReturn(List.of());

        // When
        SearchResponse response = searchService.search("nonexistent");

        // Then
        assertThat(response.getResults()).isEmpty();
        assertThat(response.getTotalCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("should_returnOnlyInquiries_when_onlyInquiriesMatch")
    void should_returnOnlyInquiries_when_onlyInquiriesMatch() {
        // Given
        Inquiry inquiry = TestDataFactory.createInquiry();
        inquiry.setId(1L);

        given(inquiryRepository.searchByKeyword("inquiry")).willReturn(List.of(inquiry));
        given(taskRepository.searchByKeyword("inquiry")).willReturn(List.of());
        given(replyTemplateRepository.searchByKeyword("inquiry")).willReturn(List.of());

        // When
        SearchResponse response = searchService.search("inquiry");

        // Then
        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getType()).isEqualTo("INQUIRY");
    }

    @Test
    @DisplayName("should_truncateSnippet_when_messageIsTooLong")
    void should_truncateSnippet_when_messageIsTooLong() {
        // Given
        Inquiry inquiry = TestDataFactory.createInquiry();
        inquiry.setId(1L);
        inquiry.setMessage("A".repeat(300));

        given(inquiryRepository.searchByKeyword("test")).willReturn(List.of(inquiry));
        given(taskRepository.searchByKeyword("test")).willReturn(List.of());
        given(replyTemplateRepository.searchByKeyword("test")).willReturn(List.of());

        // When
        SearchResponse response = searchService.search("test");

        // Then
        assertThat(response.getResults().get(0).getSnippet()).hasSize(203); // 200 + "..."
        assertThat(response.getResults().get(0).getSnippet()).endsWith("...");
    }

    @Test
    @DisplayName("should_handleNullContent_when_truncating")
    void should_handleNullContent_when_truncating() {
        // Given
        Task task = TestDataFactory.createTask();
        task.setId(1L);
        task.setDescription(null);

        given(inquiryRepository.searchByKeyword("test")).willReturn(List.of());
        given(taskRepository.searchByKeyword("test")).willReturn(List.of(task));
        given(replyTemplateRepository.searchByKeyword("test")).willReturn(List.of());

        // When
        SearchResponse response = searchService.search("test");

        // Then
        assertThat(response.getResults().get(0).getSnippet()).isEqualTo("");
    }

    @Test
    @DisplayName("should_includeStatusForInquiriesAndTasks_when_searching")
    void should_includeStatusForInquiriesAndTasks_when_searching() {
        // Given
        Inquiry inquiry = TestDataFactory.createInquiry();
        inquiry.setId(1L);
        Task task = TestDataFactory.createTask();
        task.setId(1L);

        given(inquiryRepository.searchByKeyword("test")).willReturn(List.of(inquiry));
        given(taskRepository.searchByKeyword("test")).willReturn(List.of(task));
        given(replyTemplateRepository.searchByKeyword("test")).willReturn(List.of());

        // When
        SearchResponse response = searchService.search("test");

        // Then
        assertThat(response.getResults().get(0).getStatus()).isNotNull();
        assertThat(response.getResults().get(1).getStatus()).isNotNull();
    }
}
