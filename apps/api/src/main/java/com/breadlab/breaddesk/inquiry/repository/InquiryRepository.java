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

    // TODO Phase 2: N+1 쿼리 최적화
    // 현재는 InquiryMessage가 별도 테이블이라 Fetch Join 불가
    // 해결 방안: @OneToMany 관계로 변경하거나, Batch Fetch Size 설정
}
