package com.breadlab.breaddesk.portal;

import com.breadlab.breaddesk.portal.dto.PortalInquiryResponse;
import com.breadlab.breaddesk.portal.dto.PortalMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    /**
     * Get inquiry by portal token (no auth required)
     */
    @GetMapping("/{token}")
    public ResponseEntity<PortalInquiryResponse> getInquiryByToken(@PathVariable String token) {
        log.info("Portal inquiry lookup: token={}", token);
        PortalInquiryResponse inquiry = portalService.getInquiryByToken(token);
        return ResponseEntity.ok(inquiry);
    }

    /**
     * Add message from customer via portal (no auth required)
     */
    @PostMapping("/{token}/messages")
    public ResponseEntity<Void> addMessage(
            @PathVariable String token,
            @RequestBody PortalMessageRequest request) {
        log.info("Portal message received: token={}, message={}", token, request.getMessage());
        portalService.addMessageViaPortal(token, request.getMessage());
        return ResponseEntity.ok().build();
    }
}
