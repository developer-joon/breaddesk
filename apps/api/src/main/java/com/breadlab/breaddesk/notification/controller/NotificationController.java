package com.breadlab.breaddesk.notification.controller;

import com.breadlab.breaddesk.auth.AuthUtils;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.notification.dto.NotificationResponse;
import com.breadlab.breaddesk.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification", description = "알림 API")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthUtils authUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long memberId = authUtils.getMemberId(userDetails);
        Page<NotificationResponse> responses = unreadOnly
                ? notificationService.getUnreadNotifications(memberId, pageable)
                : notificationService.getNotificationsByMember(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long memberId = authUtils.getMemberId(userDetails);
        long count = notificationService.getUnreadCount(memberId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long memberId = authUtils.getMemberId(userDetails);
        notificationService.markAllAsRead(memberId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
