package com.breadlab.breaddesk.stats.service;

import com.breadlab.breaddesk.stats.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatsService 테스트")
class StatsServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query query;

    @InjectMocks
    private StatsService statsService;

    @BeforeEach
    void setUp() {
        lenient().when(em.createNativeQuery(anyString())).thenReturn(query);
        lenient().when(query.setParameter(anyInt(), any())).thenReturn(query);
    }

    @Test
    @DisplayName("getOverview - 전체 현황 통계 조회")
    void getOverview_shouldReturnStatistics() {
        // given
        LocalDate from = LocalDate.now().minusDays(30);
        LocalDate to = LocalDate.now();

        // Mock various queries
        when(query.getSingleResult())
                .thenReturn(100L)  // totalInquiries
                .thenReturn(80L)   // totalTasks
                .thenReturn(10L)   // totalMembers
                .thenReturn(70L)   // aiAnswered
                .thenReturn(50L)   // aiResolved
                .thenReturn(2.5)   // avgResponseHours
                .thenReturn(12.0)  // avgResolveHours
                .thenReturn(60L)   // slaTasksTotal
                .thenReturn(5L)    // slaResponseBreached
                .thenReturn(10L);  // slaResolveBreached

        List<Object[]> channelData = new ArrayList<>();
        channelData.add(new Object[]{"email", 50L});
        channelData.add(new Object[]{"web", 30L});
        
        List<Object[]> urgencyData = new ArrayList<>();
        urgencyData.add(new Object[]{"HIGH", 30L});
        urgencyData.add(new Object[]{"NORMAL", 50L});

        when(query.getResultList())
                .thenReturn(channelData)
                .thenReturn(urgencyData);

        // when
        StatsOverviewResponse response = statsService.getOverview(from, to);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalInquiries()).isEqualTo(100L);
        assertThat(response.getTotalTasks()).isEqualTo(80L);
        assertThat(response.getTotalMembers()).isEqualTo(10L);
        verify(em, atLeastOnce()).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("getAIStats - AI 통계 조회")
    void getAIStats_shouldReturnAIStatistics() {
        // given
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(query.getSingleResult())
                .thenReturn(100L)  // totalAI
                .thenReturn(70L)   // autoResolved
                .thenReturn(20L)   // escalated
                .thenReturn(60L)   // highConf
                .thenReturn(30L)   // medConf
                .thenReturn(10L);  // lowConf

        // when
        StatsAIResponse response = statsService.getAIStats(from, to);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalAIAnswered()).isEqualTo(100L);
        assertThat(response.getAutoResolvedCount()).isEqualTo(70L);
        assertThat(response.getAutoResolvedRate()).isGreaterThan(0);
        assertThat(response.getEscalatedCount()).isEqualTo(20L);
        assertThat(response.getConfidenceDistribution()).containsKeys("HIGH(0.8+)", "MEDIUM(0.5-0.8)", "LOW(<0.5)");
        verify(em, atLeastOnce()).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("getTeamStats - 팀원별 통계 조회")
    void getTeamStats_shouldReturnTeamStatistics() {
        // given
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        List<Object[]> mockTeamData = new ArrayList<>();
        mockTeamData.add(new Object[]{1L, "홍길동", 20L, 15L, 5.5});
        mockTeamData.add(new Object[]{2L, "김철수", 18L, 12L, 6.2});

        when(query.getResultList()).thenReturn(mockTeamData);

        // when
        List<StatsTeamMemberResponse> responses = statsService.getTeamStats(from, to);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getMemberName()).isEqualTo("홍길동");
        assertThat(responses.get(0).getAssignedCount()).isEqualTo(20L);
        assertThat(responses.get(0).getCompletedCount()).isEqualTo(15L);
        assertThat(responses.get(1).getMemberName()).isEqualTo("김철수");
        verify(em).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("getWeeklyReport - 주간 리포트 생성")
    void getWeeklyReport_shouldReturnReport() {
        // given
        when(query.getSingleResult())
                .thenReturn(150L)  // newInquiries
                .thenReturn(120L)  // resolvedInquiries
                .thenReturn(100L)  // newTasks
                .thenReturn(85L)   // completedTasks
                .thenReturn(100L)  // aiAnswered
                .thenReturn(75L)   // aiResolved
                .thenReturn(90L)   // slaTotal
                .thenReturn(10L);  // slaBreached

        // Mock daily counts
        List<Object[]> dailyData = new ArrayList<>();
        dailyData.add(new Object[]{"2026-04-01", 20L});
        dailyData.add(new Object[]{"2026-04-02", 25L});
        
        // Mock team stats query
        List<Object[]> teamData = new ArrayList<>();
        teamData.add(new Object[]{1L, "홍길동", 15L, 12L, 4.5});

        when(query.getResultList())
                .thenReturn(dailyData)
                .thenReturn(teamData);

        // when
        WeeklyReportResponse response = statsService.getWeeklyReport();

        // then
        assertThat(response).isNotNull();
        assertThat(response.getNewInquiries()).isEqualTo(150L);
        assertThat(response.getResolvedInquiries()).isEqualTo(120L);
        assertThat(response.getNewTasks()).isEqualTo(100L);
        assertThat(response.getCompletedTasks()).isEqualTo(85L);
        assertThat(response.getAiResolutionRate()).isGreaterThan(0);
        assertThat(response.getSlaComplianceRate()).isGreaterThan(0);
        assertThat(response.getDailyInquiryCounts()).isNotEmpty();
        assertThat(response.getTopPerformers()).isNotEmpty();
        verify(em, atLeastOnce()).createNativeQuery(anyString());
    }

    @Test
    @DisplayName("getAIStats - AI 답변 없을 시 0% 반환")
    void getAIStats_whenNoData_shouldReturnZero() {
        // given
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(query.getSingleResult()).thenReturn(0L);

        // when
        StatsAIResponse response = statsService.getAIStats(from, to);

        // then
        assertThat(response.getTotalAIAnswered()).isEqualTo(0L);
        assertThat(response.getAutoResolvedRate()).isEqualTo(0);
        assertThat(response.getEscalatedRate()).isEqualTo(0);
    }

    @Test
    @DisplayName("getTeamStats - 팀원 없을 시 빈 목록 반환")
    void getTeamStats_whenNoMembers_shouldReturnEmptyList() {
        // given
        LocalDate from = LocalDate.now().minusDays(7);
        LocalDate to = LocalDate.now();

        when(query.getResultList()).thenReturn(new ArrayList<>());

        // when
        List<StatsTeamMemberResponse> responses = statsService.getTeamStats(from, to);

        // then
        assertThat(responses).isEmpty();
    }
}
