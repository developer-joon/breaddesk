package com.breadlab.breaddesk.csat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CsatRepository extends JpaRepository<CsatSurvey, Long> {
    
    Optional<CsatSurvey> findByToken(String token);
    
    Optional<CsatSurvey> findByInquiryId(Long inquiryId);
    
    boolean existsByInquiryId(Long inquiryId);
}
