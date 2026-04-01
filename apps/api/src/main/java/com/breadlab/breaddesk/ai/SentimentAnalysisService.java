package com.breadlab.breaddesk.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Sentiment analysis service using LLM
 * Analyzes customer message sentiment: POSITIVE, NEUTRAL, NEGATIVE, ANGRY
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SentimentAnalysisService {

    private final LLMProvider llmProvider;

    /**
     * Analyze sentiment of text
     * 
     * @param text Message text to analyze
     * @return Sentiment: POSITIVE, NEUTRAL, NEGATIVE, ANGRY
     */
    public String analyzeSentiment(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "NEUTRAL";
        }

        try {
            String systemPrompt = "You are a sentiment analysis assistant. Respond with ONLY ONE WORD.";
            String userMessage = buildSentimentPrompt(text);
            LLMResponse response = llmProvider.chat(systemPrompt, userMessage, List.of());
            
            String sentiment = response.content().trim().toUpperCase();
            
            // Validate response
            if (sentiment.contains("ANGRY")) {
                return "ANGRY";
            } else if (sentiment.contains("NEGATIVE")) {
                return "NEGATIVE";
            } else if (sentiment.contains("POSITIVE")) {
                return "POSITIVE";
            } else {
                return "NEUTRAL";
            }
        } catch (Exception e) {
            log.error("Sentiment analysis failed: {}", e.getMessage(), e);
            return "NEUTRAL";
        }
    }

    /**
     * Check if text is angry/frustrated
     */
    public boolean isAngry(String text) {
        String sentiment = analyzeSentiment(text);
        return "ANGRY".equals(sentiment);
    }

    private String buildSentimentPrompt(String text) {
        return String.format("""
                Analyze the sentiment of the following customer message.
                Respond with ONLY ONE WORD: POSITIVE, NEUTRAL, NEGATIVE, or ANGRY.
                
                Rules:
                - ANGRY: Customer is furious, uses aggressive language, makes threats, or demands immediate action
                - NEGATIVE: Customer is unhappy but not aggressive
                - NEUTRAL: Factual inquiry without emotional tone
                - POSITIVE: Customer is satisfied, grateful, or complimentary
                
                Customer message:
                %s""", text);
    }
}
