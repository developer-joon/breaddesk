package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.notification.service.NotificationService;
import com.breadlab.breaddesk.sla.service.SlaTimerService;
import com.breadlab.breaddesk.task.entity.Task;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EscalationService 테스트")
class EscalationServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SlaTimerService slaTimerService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EscalationService escalationService;

    private Inquiry testInquiry;
    private Member testAdmin;

    @BeforeEach
    void setUp() {
        testInquiry = new Inquiry();
        testInquiry.setId(1L);
        testInquiry.setMessage("배송이 너무 늦어요");
        testInquiry.setSenderName("홍길동");
        testInquiry.setSenderEmail("hong@example.com");
        testInquiry.setChannel("email");
        testInquiry.setStatus(InquiryStatus.OPEN);
        testInquiry.setCreatedAt(LocalDateTime.now());

        testAdmin = new Member();
        testAdmin.setId(1L);
        testAdmin.setName("관리자");
        testAdmin.setEmail("admin@example.com");
        testAdmin.setRole(MemberRole.ADMIN);
        testAdmin.setActive(true);
    }

    @Test
    @DisplayName("escalate - 일반 에스컬레이션 성공")
    void escalate_shouldCreateTaskSuccessfully() {
        // given
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Member> admins = new ArrayList<>();
        admins.add(testAdmin);
        when(memberRepository.findAll()).thenReturn(admins);

        // when
        Task result = escalationService.escalate(testInquiry, TaskUrgency.NORMAL);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).contains("[에스컬레이션]");
        assertThat(result.getUrgency()).isEqualTo(TaskUrgency.NORMAL);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.WAITING);
        assertThat(testInquiry.getStatus()).isEqualTo(InquiryStatus.ESCALATED);
        verify(taskRepository, times(2)).save(any(Task.class)); // 초기 저장 + SLA 후 저장
        verify(slaTimerService).startSla(any(Task.class));
        verify(inquiryRepository).save(testInquiry);
    }

    @Test
    @DisplayName("escalate - 긴급 키워드 감지 시 CRITICAL 설정")
    void escalate_withUrgentKeywords_shouldSetCritical() {
        // given
        testInquiry.setMessage("긴급! 서비스 장애 발생");
        
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        Task result = escalationService.escalate(testInquiry, null); // null = 자동 판단

        // then
        assertThat(result.getUrgency()).isEqualTo(TaskUrgency.CRITICAL);
    }

    @Test
    @DisplayName("escalate - AI 응답 포함 시 설명에 추가")
    void escalate_withAIResponse_shouldIncludeInDescription() {
        // given
        testInquiry.setAiResponse("배송은 2-3일 소요됩니다.");
        testInquiry.setAiConfidence(0.6f);
        
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        Task result = escalationService.escalate(testInquiry, TaskUrgency.NORMAL);

        // then
        assertThat(result.getDescription()).contains("AI 답변");
        assertThat(result.getDescription()).contains("60.0%");
    }

    @Test
    @DisplayName("escalateFromAI - AI confidence 부족 시 자동 에스컬레이션")
    void escalateFromAI_shouldEscalate() {
        // given
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        Task result = escalationService.escalateFromAI(testInquiry);

        // then
        assertThat(result).isNotNull();
        verify(taskRepository, atLeast(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("escalateFromUserDissatisfaction - 사용자 불만족 시 HIGH urgency")
    void escalateFromUserDissatisfaction_shouldSetHighUrgency() {
        // given
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.findAll()).thenReturn(new ArrayList<>());

        // when
        Task result = escalationService.escalateFromUserDissatisfaction(testInquiry);

        // then
        assertThat(result.getUrgency()).isEqualTo(TaskUrgency.HIGH);
    }

    @Test
    @DisplayName("escalate - 관리자 알림 발송")
    void escalate_shouldNotifyAdmins() {
        // given
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Member> admins = new ArrayList<>();
        admins.add(testAdmin);
        when(memberRepository.findAll()).thenReturn(admins);

        // when
        escalationService.escalate(testInquiry, TaskUrgency.NORMAL);

        // then
        verify(notificationService).createNotification(
                eq(1L),
                eq("ESCALATION"),
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    @DisplayName("escalate - 알림 실패해도 에스컬레이션 성공")
    void escalate_whenNotificationFails_shouldStillEscalate() {
        // given
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(1L);
            return task;
        });
        when(inquiryRepository.save(any(Inquiry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        List<Member> admins = new ArrayList<>();
        admins.add(testAdmin);
        when(memberRepository.findAll()).thenReturn(admins);
        
        doThrow(new RuntimeException("Notification failed"))
                .when(notificationService).createNotification(anyLong(), anyString(), anyString(), anyString(), anyString());

        // when
        Task result = escalationService.escalate(testInquiry, TaskUrgency.NORMAL);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }
}
