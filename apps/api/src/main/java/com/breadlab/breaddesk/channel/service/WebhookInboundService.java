package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.dto.WebhookIncomingRequest;
import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.breadlab.breaddesk.common.exception.UnauthorizedException;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryResponse;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookInboundService {

    private final ChannelConfigRepository channelConfigRepository;
    private final ChannelAdapterRegistry adapterRegistry;
    private final InquiryService inquiryService;

    /**
     * Process an incoming webhook: validate token, normalize via adapter, create inquiry.
     */
    public InquiryResponse processIncoming(WebhookIncomingRequest request, String webhookToken) {
        String source = request.getSource().toLowerCase();

        // Validate webhook token against channel config
        channelConfigRepository.findByChannelType(source)
                .filter(ChannelConfig::getIsActive)
                .ifPresent(config -> {
                    if (config.getAuthToken() != null && !config.getAuthToken().isBlank()) {
                        if (!config.getAuthToken().equals(webhookToken)) {
                            throw new UnauthorizedException("Invalid webhook token for channel: " + source);
                        }
                    }
                });

        // Use adapter to normalize the message, or build a generic InquiryRequest
        InquiryRequest inquiryRequest = adapterRegistry.getAdapter(source)
                .map(adapter -> adapter.receiveMessage(request))
                .orElseGet(() -> InquiryRequest.builder()
                        .channel(source)
                        .channelMeta(request.getMetadata())
                        .senderName(request.getSenderName())
                        .senderEmail(request.getSenderEmail())
                        .message(request.getMessage())
                        .build());

        log.info("Processing incoming webhook from '{}': sender={}", source, request.getSenderName());

        // Create inquiry (triggers AI auto-answer + escalation)
        return inquiryService.createInquiry(inquiryRequest);
    }
}
