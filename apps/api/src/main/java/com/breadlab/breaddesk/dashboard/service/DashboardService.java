package com.breadlab.breaddesk.dashboard.service;

import com.breadlab.breaddesk.dashboard.dto.DashboardResponse;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 대시보드 서비스
 * 
 * <p>대시보드 화면에 표시할 주요 지표를 제공합니다.</p>
 * 
 * <p>주요 지표:</p>
 * <ul>
 *   <li>전체 문의 수 / 미해결 문의 수 / 오늘 문의 수</li>
 *   <li>AI 자동해결률</li>
 *   <li>문의 상태별 분포</li>
 *   <li>업무 상태별 분포</li>
 * </ul>
 * 
 * @author BreadDesk Team
 * @since 0.1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final InquiryRepository inquiryRepository;
    private final TaskRepository taskRepository;

    /**
     * 대시보드 요약 통계를 조회합니다.
     * 
     * @return 대시보드 주요 지표 (문의/업무 현황, AI 자동해결률 등)
     */
    public DashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);

        long totalInquiries = inquiryRepository.count();
        long unresolvedInquiries = inquiryRepository.countUnresolved();
        long todayInquiries = inquiryRepository.countCreatedToday(startOfDay);
        long aiResolvedCount = inquiryRepository.countAiResolved();

        double aiResolutionRate = totalInquiries > 0 
                ? (double) aiResolvedCount / totalInquiries * 100 
                : 0.0;

        Map<String, Long> inquiriesByStatus = Stream.of(InquiryStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        status -> inquiryRepository.countByStatus(status)
                ));

        Map<String, Long> tasksByStatus = Stream.of(TaskStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        status -> taskRepository.countByStatus(status)
                ));

        return DashboardResponse.builder()
                .totalInquiries(totalInquiries)
                .unresolvedInquiries(unresolvedInquiries)
                .todayInquiries(todayInquiries)
                .aiResolutionRate(aiResolutionRate)
                .inquiriesByStatus(inquiriesByStatus)
                .tasksByStatus(tasksByStatus)
                .build();
    }
}
