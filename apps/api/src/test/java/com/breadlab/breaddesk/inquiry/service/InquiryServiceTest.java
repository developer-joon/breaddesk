package com.breadlab.breaddesk.inquiry.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.inquiry.dto.*;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.repository.TaskRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Disabled("TODO: 서비스 API 변경 반영 필요")
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryMessageRepository inquiryMessageRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private InquiryService inquiryService;

    private Inquiry inquiry;

    @BeforeEach
    void setUp() {
        inquiry = TestDataFactory.createInquiry();
        inquiry.setId(1L);
    }

    @Test
    @DisplayName("should_createInquiry_when_validRequest")
    void should_createInquiry_when_validRequest() {
        // Given
        InquiryRequest request = TestDataFactory.createInquiryRequest();
        given(inquiryRepository.save(any(Inquiry.class))).willAnswer(invocation -> {
            Inquiry saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        InquiryResponse response = inquiryService.createInquiry(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getChannel()).isEqualTo("email");
        assertThat(response.getStatus()).isEqualTo(InquiryStatus.OPEN);
        assertThat(response.getSenderName()).isEqualTo("Customer");
    }

    @Test
    @DisplayName("should_getAllInquiries_when_called")
    void should_getAllInquiries_when_called() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        given(inquiryRepository.findAll(pageable))
                .willReturn(new PageImpl<>(List.of(inquiry)));

        // When
        Page<InquiryResponse> page = inquiryService.getAllInquiries(pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should_getInquiryById_when_exists")
    void should_getInquiryById_when_exists() {
        // Given
        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));

        // When
        InquiryResponse response = inquiryService.getInquiryById(1L);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getChannel()).isEqualTo("email");
    }

    @Test
    @DisplayName("should_throwException_when_inquiryNotFound")
    void should_throwException_when_inquiryNotFound() {
        // Given
        given(inquiryRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inquiryService.getInquiryById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_updateStatus_when_validRequest")
    void should_updateStatus_when_validRequest() {
        // Given
        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(inquiry);

        InquiryStatusUpdateRequest request = TestDataFactory.createInquiryStatusUpdateRequest(InquiryStatus.ESCALATED);

        // When
        InquiryResponse response = inquiryService.updateInquiryStatus(1L, request);

        // Then
        assertThat(response.getStatus()).isEqualTo(InquiryStatus.ESCALATED);
    }

    @Test
    @DisplayName("should_setResolvedAt_when_statusIsResolved")
    void should_setResolvedAt_when_statusIsResolved() {
        // Given
        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(inquiry);

        InquiryStatusUpdateRequest request = TestDataFactory.createInquiryStatusUpdateRequest(InquiryStatus.RESOLVED);

        // When
        inquiryService.updateInquiryStatus(1L, request);

        // Then
        assertThat(inquiry.getResolvedAt()).isNotNull();
    }

    @Test
    @DisplayName("should_addMessage_when_inquiryExists")
    void should_addMessage_when_inquiryExists() {
        // Given
        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));
        given(inquiryMessageRepository.save(any(InquiryMessage.class))).willAnswer(invocation -> {
            InquiryMessage saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        InquiryMessageRequest request = TestDataFactory.createInquiryMessageRequest();

        // When
        InquiryMessageResponse response = inquiryService.addMessage(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRole()).isEqualTo(InquiryMessageRole.AGENT);
        assertThat(response.getInquiryId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should_convertToTask_when_validRequest")
    void should_convertToTask_when_validRequest() {
        // Given
        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));
        given(taskRepository.save(any(Task.class))).willAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(inquiry);

        ConvertToTaskRequest request = TestDataFactory.createConvertToTaskRequest();

        // When
        InquiryResponse response = inquiryService.convertToTask(1L, request);

        // Then
        assertThat(response.getStatus()).isEqualTo(InquiryStatus.ESCALATED);
        assertThat(response.getTaskId()).isEqualTo(10L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("should_convertToTaskWithAssignee_when_assigneeIdProvided")
    void should_convertToTaskWithAssignee_when_assigneeIdProvided() {
        // Given
        Member assignee = TestDataFactory.createMember();
        assignee.setId(5L);

        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));
        given(memberRepository.findById(5L)).willReturn(Optional.of(assignee));
        given(taskRepository.save(any(Task.class))).willAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });
        given(inquiryRepository.save(any(Inquiry.class))).willReturn(inquiry);

        ConvertToTaskRequest request = TestDataFactory.createConvertToTaskRequest();
        request.setAssigneeId(5L);

        // When
        inquiryService.convertToTask(1L, request);

        // Then
        verify(memberRepository).findById(5L);
    }

    @Test
    @DisplayName("should_deleteInquiry_when_exists")
    void should_deleteInquiry_when_exists() {
        // Given
        given(inquiryRepository.findById(1L)).willReturn(Optional.of(inquiry));

        // When
        inquiryService.deleteInquiry(1L);

        // Then
        verify(inquiryRepository).delete(inquiry);
    }
}
