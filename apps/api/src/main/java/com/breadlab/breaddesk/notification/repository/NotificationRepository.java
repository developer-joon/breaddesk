package com.breadlab.breaddesk.notification.repository;

import com.breadlab.breaddesk.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByMemberId(Long memberId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.member.id = :memberId AND n.read = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.member.id = :memberId AND n.read = false")
    long countUnreadByMemberId(@Param("memberId") Long memberId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.member.id = :memberId")
    void markAllAsReadByMemberId(@Param("memberId") Long memberId);
}
