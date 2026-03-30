package com.breadlab.breaddesk.channel.controller;

import com.breadlab.breaddesk.channel.dto.WebhookIncomingRequest;
import com.breadlab.breaddesk.channel.service.WebhookInboundService;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.inquiry.dto.InquiryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookInboundService webhookInboundService;

    /**
     * Receive an incoming inquiry from an external channel (Slack, Teams, Email, etc.).
     * Authentication via X-Webhook-Token header.
     */
    @PostMapping("/incoming")
    public ResponseEntity<ApiResponse<InquiryResponse>> receiveIncoming(
            @Valid @RequestBody WebhookIncomingRequest request,
            @RequestHeader(value = "X-Webhook-Token", required = false) String webhookToken) {

        InquiryResponse response = webhookInboundService.processIncoming(request, webhookToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
