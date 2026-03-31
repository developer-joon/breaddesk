package com.breadlab.breaddesk.notification.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.notification.dto.NotificationResponse;
import com.breadlab.breaddesk.notification.entity.Notification;
import com.breadlab.breaddesk.notification.repository.NotificationRepository;
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
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Member member;
    private Notification notification;

    @BeforeEach
    void setUp() {
        member = TestDataFactory.createMember();
        member.setId(1L);
        notification = TestDataFactory.createNotification(member);
        notification.setId(1L);
    }

    @Test
    @DisplayName("should_createNotification_when_memberExists")
    void should_createNotification_when_memberExists() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(notificationRepository.save(any(Notification.class))).willAnswer(inv -> {
            Notification saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        NotificationResponse response = notificationService.createNotification(
                1L, "TASK_ASSIGNED", "New Task", "You have a task", "/tasks/1");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getType()).isEqualTo("TASK_ASSIGNED");
        assertThat(response.isRead()).isFalse();
    }

    @Test
    @DisplayName("should_throwException_when_memberNotFoundForNotification")
    void should_throwException_when_memberNotFoundForNotification() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.createNotification(
                999L, "TYPE", "Title", "Msg", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_getNotificationsByMember_when_called")
    void should_getNotificationsByMember_when_called() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        given(notificationRepository.findByMemberId(1L, pageable))
                .willReturn(new PageImpl<>(List.of(notification)));

        // When
        Page<NotificationResponse> page = notificationService.getNotificationsByMember(1L, pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("should_getUnreadNotifications_when_called")
    void should_getUnreadNotifications_when_called() {
        Pageable pageable = PageRequest.of(0, 10);
        given(notificationRepository.findUnreadByMemberId(1L, pageable))
                .willReturn(new PageImpl<>(List.of(notification)));

        Page<NotificationResponse> page = notificationService.getUnreadNotifications(1L, pageable);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("should_getUnreadCount_when_called")
    void should_getUnreadCount_when_called() {
        given(notificationRepository.countUnreadByMemberId(1L)).willReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("should_markAsRead_when_exists")
    void should_markAsRead_when_exists() {
        // Given
        given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);

        // When
        notificationService.markAsRead(1L);

        // Then
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    @DisplayName("should_markAllAsRead_when_called")
    void should_markAllAsRead_when_called() {
        notificationService.markAllAsRead(1L);

        verify(notificationRepository).markAllAsReadByMemberId(1L);
    }

    @Test
    @DisplayName("should_deleteNotification_when_called")
    void should_deleteNotification_when_called() {
        notificationService.deleteNotification(1L);

        verify(notificationRepository).deleteById(1L);
    }
}
