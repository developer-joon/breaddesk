package com.breadlab.breaddesk.inquiry.repository;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    Page<Inquiry> findByStatus(Inquiry.InquiryStatus status, Pageable pageable);
    Page<Inquiry> findBySenderEmail(String senderEmail, Pageable pageable);
}
