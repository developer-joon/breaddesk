package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.adapter.ChannelAdapter;
import com.breadlab.breaddesk.channel.dto.WebhookOutboundPayload;
import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Sends outbound webhook responses back to the originating channel
 * via n8n or direct webhook URL configured per channel.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookOutboundService {

    private final ChannelConfigRepository channelConfigRepository;
    private final ChannelAdapterRegistry adapterRegistry;
    private final WebClient.Builder webClientBuilder;

    /**
     * Send a response back to the originating channel.
     * Called when an inquiry is resolved (by AI or human).
     */
    public void sendResponse(Inquiry inquiry, String responseMessage, String resolvedBy) {
        String channel = inquiry.getChannel();
        if (channel == null || channel.equalsIgnoreCase("web")) {
            // Web channel inquiries don't need outbound webhook
            log.debug("Skipping outbound webhook for web channel inquiry #{}", inquiry.getId());
            return;
        }

        channelConfigRepository.findByChannelType(channel.toLowerCase())
                .filter(ChannelConfig::getIsActive)
                .ifPresentOrElse(
                        config -> doSend(config, inquiry, responseMessage, resolvedBy),
                        () -> log.warn("No active channel config for '{}', skipping outbound for inquiry #{}",
                                channel, inquiry.getId()));
    }

    private void doSend(ChannelConfig config, Inquiry inquiry,
                        String responseMessage, String resolvedBy) {
        if (config.getWebhookUrl() == null || config.getWebhookUrl().isBlank()) {
            log.warn("Channel '{}' has no webhook URL configured, skipping outbound for inquiry #{}",
                    config.getChannelType(), inquiry.getId());
            return;
        }

        // Use adapter to format the outbound message
        ChannelAdapter adapter = adapterRegistry.getAdapter(config.getChannelType())
                .orElse(null);

        WebhookOutboundPayload payload;
        if (adapter != null) {
            payload = adapter.formatOutbound(
                    inquiry.getId(),
                    inquiry.getSenderEmail(),
                    inquiry.getSenderName(),
                    responseMessage,
                    resolvedBy,
                    inquiry.getChannelMeta());
        } else {
            // Generic payload without adapter formatting
            payload = WebhookOutboundPayload.builder()
                    .inquiryId(inquiry.getId())
                    .channel(inquiry.getChannel())
                    .recipientEmail(inquiry.getSenderEmail())
                    .recipientName(inquiry.getSenderName())
                    .message(responseMessage)
                    .resolvedBy(resolvedBy)
                    .metadata(inquiry.getChannelMeta())
                    .build();
        }

        WebClient client = webClientBuilder.build();

        client.post()
                .uri(config.getWebhookUrl())
                .header("X-Webhook-Token", config.getAuthToken())
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(resp -> log.info("Outbound webhook sent for inquiry #{} to {} → {}",
                        inquiry.getId(), config.getChannelType(), resp))
                .doOnError(err -> log.error("Outbound webhook failed for inquiry #{} to {}: {}",
                        inquiry.getId(), config.getChannelType(), err.getMessage()))
                .onErrorResume(err -> Mono.empty())
                .subscribe();
    }
}
