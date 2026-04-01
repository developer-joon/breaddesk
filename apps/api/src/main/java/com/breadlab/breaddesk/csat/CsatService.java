package com.breadlab.breaddesk.csat;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsatService {

    private final CsatRepository csatRepository;
    private final InquiryRepository inquiryRepository;

    /**
     * Create and send CSAT survey for an inquiry
     * Called when inquiry is resolved
     */
    @Transactional
    public CsatSurvey createSurvey(Long inquiryId) {
        // Check if survey already exists
        if (csatRepository.existsByInquiryId(inquiryId)) {
            log.info("CSAT survey already exists for inquiry #{}", inquiryId);
            return csatRepository.findByInquiryId(inquiryId).orElse(null);
        }

        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + inquiryId));

        CsatSurvey survey = CsatSurvey.builder()
                .inquiryId(inquiryId)
                .token(UUID.randomUUID().toString())
                .sentAt(LocalDateTime.now())
                .responded(false)
                .build();

        CsatSurvey saved = csatRepository.save(survey);
        log.info("Created CSAT survey for inquiry #{}: token={}", inquiryId, saved.getToken());

        // TODO: Send survey link via channel (email, SMS, etc.)
        // For now, just log the survey URL
        String surveyUrl = "/csat/" + saved.getToken();
        log.info("CSAT Survey URL: {}", surveyUrl);

        return saved;
    }

    /**
     * Record customer response
     */
    @Transactional
    public CsatSurvey recordResponse(String token, Integer rating, String feedback) {
        CsatSurvey survey = csatRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found: " + token));

        if (survey.getResponded()) {
            log.warn("Survey already responded: {}", token);
            throw new IllegalStateException("Survey already completed");
        }

        survey.setRating(rating);
        survey.setFeedback(feedback);
        survey.setRespondedAt(LocalDateTime.now());
        survey.setResponded(true);

        CsatSurvey updated = csatRepository.save(survey);
        log.info("Recorded CSAT response for inquiry #{}: rating={}", survey.getInquiryId(), rating);

        return updated;
    }

    /**
     * Get survey by token
     */
    public CsatSurvey getSurveyByToken(String token) {
        return csatRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Survey not found: " + token));
    }

    /**
     * Get average rating across all surveys
     */
    public double getAverageRating() {
        return csatRepository.findAll().stream()
                .filter(CsatSurvey::getResponded)
                .mapToInt(CsatSurvey::getRating)
                .average()
                .orElse(0.0);
    }
}
