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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final InquiryRepository inquiryRepository;
    private final TaskRepository taskRepository;

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
