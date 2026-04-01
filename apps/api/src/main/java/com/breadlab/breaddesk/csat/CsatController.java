package com.breadlab.breaddesk.csat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * CSAT (Customer Satisfaction) Controller
 * Public endpoints - no authentication required
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/csat")
@RequiredArgsConstructor
public class CsatController {

    private final CsatService csatService;

    /**
     * GET /api/v1/csat/{token}
     * Get survey details (for rendering the form)
     */
    @GetMapping("/{token}")
    public ResponseEntity<?> getSurvey(@PathVariable String token) {
        try {
            CsatSurvey survey = csatService.getSurveyByToken(token);
            
            if (survey.getResponded()) {
                return ResponseEntity.ok(new SurveyResponse(
                        "already_completed",
                        "Thank you! You have already completed this survey.",
                        survey.getRating()
                ));
            }

            return ResponseEntity.ok(new SurveyResponse(
                    "pending",
                    "Please rate your experience (1-5 stars)",
                    null
            ));
        } catch (Exception e) {
            log.error("Failed to get survey: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid or expired survey link");
        }
    }

    /**
     * POST /api/v1/csat/{token}/respond
     * Submit customer response (public endpoint)
     */
    @PostMapping("/{token}/respond")
    public ResponseEntity<?> submitResponse(
            @PathVariable String token,
            @RequestBody CsatResponseRequest request) {
        
        try {
            // Validate rating
            if (request.rating() == null || request.rating() < 1 || request.rating() > 5) {
                return ResponseEntity.badRequest().body("Rating must be between 1 and 5");
            }

            CsatSurvey survey = csatService.recordResponse(
                    token,
                    request.rating(),
                    request.feedback()
            );

            return ResponseEntity.ok(new SurveyResponse(
                    "success",
                    "Thank you for your feedback!",
                    survey.getRating()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to record CSAT response: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to submit response");
        }
    }

    /**
     * GET /api/v1/csat/stats
     * Get aggregate CSAT statistics (could be protected in production)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        double avgRating = csatService.getAverageRating();
        return ResponseEntity.ok(new StatsResponse(avgRating));
    }

    // DTOs
    public record CsatResponseRequest(Integer rating, String feedback) {}
    
    public record SurveyResponse(String status, String message, Integer rating) {}
    
    public record StatsResponse(double averageRating) {}
}
