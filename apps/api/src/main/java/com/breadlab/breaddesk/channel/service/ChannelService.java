package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.entity.ChannelMessage;
import com.breadlab.breaddesk.channel.entity.ChannelType;
import com.breadlab.breaddesk.channel.repository.ChannelMessageRepository;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Unified channel service - receive messages from any channel and create inquiries
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelMessageRepository channelMessageRepository;
    private final InquiryService inquiryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Receive a message from any channel and process it
     */
    @Transactional
    public ChannelMessage receiveMessage(ChannelType channelType, String source, String content, 
                                         String senderInfo, String channelMetadata) {
        // Save channel message
        ChannelMessage message = ChannelMessage.builder()
                .channelType(channelType)
                .source(source)
                .content(content)
                .senderInfo(senderInfo)
                .channelMetadata(channelMetadata)
                .createdAt(LocalDateTime.now())
                .processed(false)
                .build();

        ChannelMessage saved = channelMessageRepository.save(message);

        // Process immediately: create inquiry
        try {
            processMessage(saved);
        } catch (Exception e) {
            log.error("Failed to process channel message #{}: {}", saved.getId(), e.getMessage(), e);
        }

        return saved;
    }

    /**
     * Process a channel message by creating an inquiry
     */
    @Transactional
    public void processMessage(ChannelMessage message) {
        if (message.isProcessed()) {
            log.debug("Message #{} already processed", message.getId());
            return;
        }

        try {
            // Parse sender info
            Map<String, String> senderMap = objectMapper.readValue(
                    message.getSenderInfo(), 
                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class)
            );

            String senderName = senderMap.getOrDefault("name", "Unknown");
            String senderEmail = senderMap.get("email");

            // Create inquiry request
            InquiryRequest inquiryRequest = InquiryRequest.builder()
                    .channel(message.getChannelType().name().toLowerCase())
                    .channelMeta(message.getChannelMetadata())
                    .senderName(senderName)
                    .senderEmail(senderEmail)
                    .message(message.getContent())
                    .build();

            var inquiry = inquiryService.createInquiry(inquiryRequest);

            // Link back to channel message
            message.setInquiryId(inquiry.getId());
            message.setProcessed(true);
            message.setProcessedAt(LocalDateTime.now());
            channelMessageRepository.save(message);

            log.info("Created inquiry #{} from {} message #{}", 
                    inquiry.getId(), message.getChannelType(), message.getId());

        } catch (Exception e) {
            log.error("Failed to create inquiry from message #{}: {}", message.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process message", e);
        }
    }

    /**
     * Process all unprocessed messages
     */
    @Transactional
    public void processUnprocessedMessages() {
        var unprocessed = channelMessageRepository.findByProcessedFalseOrderByCreatedAtAsc();
        log.info("Processing {} unprocessed channel messages", unprocessed.size());

        for (ChannelMessage message : unprocessed) {
            try {
                processMessage(message);
            } catch (Exception e) {
                log.error("Failed to process message #{}: {}", message.getId(), e.getMessage());
            }
        }
    }
}
