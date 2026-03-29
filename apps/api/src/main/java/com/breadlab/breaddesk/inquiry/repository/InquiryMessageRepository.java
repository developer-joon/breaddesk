package com.breadlab.breaddesk.inquiry.repository;

import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryMessageRepository extends JpaRepository<InquiryMessage, Long> {
    List<InquiryMessage> findByInquiryIdOrderByCreatedAt(Long inquiryId);
}
