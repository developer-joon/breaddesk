package com.breadlab.breaddesk.inquiry.repository;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);

    @Query("SELECT i FROM Inquiry i WHERE i.status = :status AND i.createdAt >= :startDate")
    List<Inquiry> findByStatusAndCreatedAtAfter(@Param("status") InquiryStatus status, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.resolvedBy = 'AI'")
    long countAiResolved();

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status IN ('OPEN', 'AI_ANSWERED', 'ESCALATED')")
    long countUnresolved();

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.createdAt >= :startDate")
    long countCreatedToday(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status = :status")
    long countByStatus(@Param("status") InquiryStatus status);

    @Query("SELECT i FROM Inquiry i WHERE i.message LIKE %:keyword% OR i.senderName LIKE %:keyword%")
    List<Inquiry> searchByKeyword(@Param("keyword") String keyword);

    @Query("SELECT i FROM Inquiry i WHERE "
            + "(:status IS NULL OR i.status = :status) "
            + "AND (:teamId IS NULL OR i.team.id = :teamId)")
    Page<Inquiry> findWithFilters(
            @Param("status") InquiryStatus status,
            @Param("teamId") Long teamId,
            Pageable pageable);
    
    List<Inquiry> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Inquiry> findByChannelAndChannelMeta(String channel, String channelMeta);
}
