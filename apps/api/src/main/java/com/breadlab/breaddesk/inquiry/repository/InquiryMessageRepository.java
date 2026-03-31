package com.breadlab.breaddesk.inquiry.repository;

import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface InquiryMessageRepository extends JpaRepository<InquiryMessage, Long> {

    @Query("SELECT m FROM InquiryMessage m WHERE m.inquiry.id = :inquiryId ORDER BY m.createdAt ASC")
    List<InquiryMessage> findByInquiryIdOrderByCreatedAtAsc(@Param("inquiryId") Long inquiryId);
}
