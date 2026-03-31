package com.breadlab.breaddesk.dashboard.service;

import com.breadlab.breaddesk.dashboard.dto.DashboardResponse;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("should_getDashboard_when_called")
    void should_getDashboard_when_called() {
        // Given
        given(inquiryRepository.count()).willReturn(100L);
        given(inquiryRepository.countUnresolved()).willReturn(30L);
        given(inquiryRepository.countCreatedToday(any())).willReturn(10L);
        given(inquiryRepository.countAiResolved()).willReturn(40L);
        given(inquiryRepository.countByStatus(any(InquiryStatus.class))).willReturn(20L);
        given(taskRepository.countByStatus(any(TaskStatus.class))).willReturn(5L);

        // When
        DashboardResponse response = dashboardService.getDashboard();

        // Then
        assertThat(response.getTotalInquiries()).isEqualTo(100L);
        assertThat(response.getUnresolvedInquiries()).isEqualTo(30L);
        assertThat(response.getTodayInquiries()).isEqualTo(10L);
        assertThat(response.getAiResolutionRate()).isEqualTo(40.0);
        assertThat(response.getInquiriesByStatus()).isNotEmpty();
        assertThat(response.getTasksByStatus()).isNotEmpty();
    }

    @Test
    @DisplayName("should_returnZeroAiRate_when_noInquiries")
    void should_returnZeroAiRate_when_noInquiries() {
        // Given
        given(inquiryRepository.count()).willReturn(0L);
        given(inquiryRepository.countUnresolved()).willReturn(0L);
        given(inquiryRepository.countCreatedToday(any())).willReturn(0L);
        given(inquiryRepository.countAiResolved()).willReturn(0L);
        given(inquiryRepository.countByStatus(any(InquiryStatus.class))).willReturn(0L);
        given(taskRepository.countByStatus(any(TaskStatus.class))).willReturn(0L);

        // When
        DashboardResponse response = dashboardService.getDashboard();

        // Then
        assertThat(response.getAiResolutionRate()).isEqualTo(0.0);
    }
}
