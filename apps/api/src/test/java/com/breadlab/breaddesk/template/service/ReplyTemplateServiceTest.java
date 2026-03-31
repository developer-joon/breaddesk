package com.breadlab.breaddesk.template.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.template.dto.ReplyTemplateRequest;
import com.breadlab.breaddesk.template.dto.ReplyTemplateResponse;
import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import com.breadlab.breaddesk.template.repository.ReplyTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReplyTemplateServiceTest {

    @Mock
    private ReplyTemplateRepository replyTemplateRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ReplyTemplateService replyTemplateService;

    private ReplyTemplate template;
    private Member member;

    @BeforeEach
    void setUp() {
        template = TestDataFactory.createReplyTemplate();
        template.setId(1L);
        member = TestDataFactory.createMember();
        member.setId(1L);
    }

    @Test
    @DisplayName("should_createTemplate_when_validRequest")
    void should_createTemplate_when_validRequest() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(replyTemplateRepository.save(any(ReplyTemplate.class))).willAnswer(inv -> {
            ReplyTemplate saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ReplyTemplateRequest request = TestDataFactory.createReplyTemplateRequest();

        // When
        ReplyTemplateResponse response = replyTemplateService.createTemplate(request, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("New Template");
        assertThat(response.getUsageCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("should_getAllTemplates_when_called")
    void should_getAllTemplates_when_called() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        given(replyTemplateRepository.findAll(pageable))
                .willReturn(new PageImpl<>(List.of(template)));

        // When
        Page<ReplyTemplateResponse> page = replyTemplateService.getAllTemplates(pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("should_getTemplatesByCategory_when_called")
    void should_getTemplatesByCategory_when_called() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        given(replyTemplateRepository.findByCategory("greeting", pageable))
                .willReturn(new PageImpl<>(List.of(template)));

        // When
        Page<ReplyTemplateResponse> page = replyTemplateService.getTemplatesByCategory("greeting", pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("should_getTemplateById_when_exists")
    void should_getTemplateById_when_exists() {
        given(replyTemplateRepository.findById(1L)).willReturn(Optional.of(template));

        ReplyTemplateResponse response = replyTemplateService.getTemplateById(1L);

        assertThat(response.getTitle()).isEqualTo("Welcome Template");
    }

    @Test
    @DisplayName("should_throwException_when_templateNotFound")
    void should_throwException_when_templateNotFound() {
        given(replyTemplateRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> replyTemplateService.getTemplateById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_updateTemplate_when_exists")
    void should_updateTemplate_when_exists() {
        // Given
        given(replyTemplateRepository.findById(1L)).willReturn(Optional.of(template));
        given(replyTemplateRepository.save(any(ReplyTemplate.class))).willReturn(template);

        ReplyTemplateRequest request = TestDataFactory.createReplyTemplateRequest();
        request.setTitle("Updated Title");

        // When
        ReplyTemplateResponse response = replyTemplateService.updateTemplate(1L, request);

        // Then
        assertThat(response.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("should_incrementUsageCount_when_templateApplied")
    void should_incrementUsageCount_when_templateApplied() {
        // Given
        given(replyTemplateRepository.findById(1L)).willReturn(Optional.of(template));
        given(replyTemplateRepository.save(any(ReplyTemplate.class))).willReturn(template);

        // When
        replyTemplateService.applyTemplate(1L, null);

        // Then
        assertThat(template.getUsageCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("should_replaceVariables_when_templateApplied")
    void should_replaceVariables_when_templateApplied() {
        // Given
        given(replyTemplateRepository.findById(1L)).willReturn(Optional.of(template));
        given(replyTemplateRepository.save(any(ReplyTemplate.class))).willReturn(template);

        // When
        String result = replyTemplateService.applyTemplate(1L, Map.of("name", "John"));

        // Then
        assertThat(result).isEqualTo("Hello John, welcome!");
    }

    @Test
    @DisplayName("should_deleteTemplate_when_exists")
    void should_deleteTemplate_when_exists() {
        given(replyTemplateRepository.findById(1L)).willReturn(Optional.of(template));

        replyTemplateService.deleteTemplate(1L);

        verify(replyTemplateRepository).delete(template);
    }
}
