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
            + "(:status IS NULL OR i.status = CAST(:status AS com.breadlab.breaddesk.inquiry.entity.InquiryStatus)) "
            + "AND (:category IS NULL OR i.category = :category) "
            + "AND (:assigneeId IS NULL OR i.assignee.id = :assigneeId) "
            + "AND (:teamId IS NULL OR i.team.id = :teamId)")
    Page<Inquiry> findWithFilters(
            @Param("status") String status,
            @Param("category") String category,
            @Param("assigneeId") Long assigneeId,
            @Param("teamId") Long teamId,
            Pageable pageable);
}
