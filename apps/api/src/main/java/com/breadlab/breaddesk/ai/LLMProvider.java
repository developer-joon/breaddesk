package com.breadlab.breaddesk.ai;

import java.util.List;

/**
 * LLM Provider 추상화 인터페이스
 * 어떤 LLM이든 갈아끼울 수 있는 구조
 */
public interface LLMProvider {

    /**
     * 채팅 응답 생성
     */
    LLMResponse chat(String systemPrompt, String userMessage, List<String> contextDocuments);

    /**
     * 텍스트 임베딩 생성
     */
    float[] embed(String text);

    /**
     * 모델 이름
     */
    String getModelName();

    /**
     * 사용 가능 여부
     */
    boolean isAvailable();
}
