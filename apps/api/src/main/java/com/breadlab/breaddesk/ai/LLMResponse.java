package com.breadlab.breaddesk.ai;

import java.util.Map;

/**
 * LLM 응답
 */
public record LLMResponse(
    String content,
    float confidence,       // 0~1
    Map<String, Object> metadata
) {
    public static LLMResponse of(String content, float confidence) {
        return new LLMResponse(content, confidence, Map.of());
    }
}
