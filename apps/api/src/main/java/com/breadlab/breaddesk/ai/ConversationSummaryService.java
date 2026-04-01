package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI-powered conversation summary service
 * Generates concise summaries of inquiry conversations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationSummaryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final LLMProvider llmProvider;

    /**
     * Generate summary for an inquiry conversation
     * 
     * @param inquiryId Inquiry ID
     * @return Concise summary of the conversation
     */
    public String generateSummary(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + inquiryId));

        List<InquiryMessage> messages = inquiryMessageRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId);

        try {
            String systemPrompt = "You are a concise conversation summarizer for customer support.";
            String userMessage = buildSummaryPrompt(inquiry, messages);
            LLMResponse response = llmProvider.chat(systemPrompt, userMessage, List.of());
            
            return response.content();
        } catch (Exception e) {
            log.error("Summary generation failed for inquiry #{}: {}", inquiryId, e.getMessage(), e);
            return "Summary generation failed.";
        }
    }

    private String buildSummaryPrompt(Inquiry inquiry, List<InquiryMessage> messages) {
        StringBuilder conversation = new StringBuilder();
        conversation.append("Original inquiry:\n");
        conversation.append(inquiry.getMessage()).append("\n\n");

        if (!messages.isEmpty()) {
            conversation.append("Follow-up messages:\n");
            for (InquiryMessage msg : messages) {
                String sender = msg.getRole() != null ? msg.getRole().toString() : "Unknown";
                conversation.append(String.format("[%s] %s\n", sender, msg.getMessage()));
            }
        }

        if (inquiry.getAiResponse() != null && !inquiry.getAiResponse().isEmpty()) {
            conversation.append("\nAI Response:\n");
            conversation.append(inquiry.getAiResponse()).append("\n");
        }

        return String.format("""
                Summarize the following customer support conversation in 2-3 sentences.
                Focus on:
                1. What the customer's issue was
                2. How it was resolved (or current status)
                3. Key actions taken
                
                Conversation:
                %s
                
                Summary:""", conversation.toString());
    }

    /**
     * Generate bullet-point summary
     */
    public String generateBulletSummary(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("Inquiry not found: " + inquiryId));

        List<InquiryMessage> messages = inquiryMessageRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId);

        try {
            String systemPrompt = "You are a bullet-point summarizer for customer support conversations.";
            String userMessage = buildBulletPrompt(inquiry, messages);
            LLMResponse response = llmProvider.chat(systemPrompt, userMessage, List.of());
            
            return response.content();
        } catch (Exception e) {
            log.error("Bullet summary generation failed: {}", e.getMessage(), e);
            return "• Summary generation failed";
        }
    }

    private String buildBulletPrompt(Inquiry inquiry, List<InquiryMessage> messages) {
        StringBuilder conversation = new StringBuilder();
        conversation.append(inquiry.getMessage()).append("\n");
        
        for (InquiryMessage msg : messages) {
            conversation.append(msg.getMessage()).append("\n");
        }

        if (inquiry.getAiResponse() != null) {
            conversation.append(inquiry.getAiResponse()).append("\n");
        }

        return String.format("""
                Create a bullet-point summary of this support conversation.
                Format: Use bullet points (•) for each key point.
                Include:
                • Customer issue
                • Resolution steps
                • Outcome/status
                
                Conversation:
                %s
                
                Summary:""", conversation.toString());
    }
}
