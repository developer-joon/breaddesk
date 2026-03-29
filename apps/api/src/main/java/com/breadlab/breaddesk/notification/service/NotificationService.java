package com.breadlab.breaddesk.notification.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.notification.dto.NotificationResponse;
import com.breadlab.breaddesk.notification.entity.Notification;
import com.breadlab.breaddesk.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public NotificationResponse createNotification(Long memberId, String type, String title, String message, String link) {
        Notification notification = Notification.builder()
                .member(memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found")))
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(notificationRepository.save(notification));
    }

    public Page<NotificationResponse> getNotificationsByMember(Long memberId, Pageable pageable) {
        return notificationRepository.findByMemberId(memberId, pageable).map(this::toResponse);
    }

    public Page<NotificationResponse> getUnreadNotifications(Long memberId, Pageable pageable) {
        return notificationRepository.findUnreadByMemberId(memberId, pageable).map(this::toResponse);
    }

    public long getUnreadCount(Long memberId) {
        return notificationRepository.countUnreadByMemberId(memberId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long memberId) {
        notificationRepository.markAllAsReadByMemberId(memberId);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .memberId(notification.getMember().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
