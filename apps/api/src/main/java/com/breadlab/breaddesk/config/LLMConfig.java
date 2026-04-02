package com.breadlab.breaddesk.config;

import com.breadlab.breaddesk.ai.ClaudeLLMProvider;
import com.breadlab.breaddesk.ai.LLMProvider;
import com.breadlab.breaddesk.ai.OllamaLLMProvider;
import com.breadlab.breaddesk.ai.OpenAILLMProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * LLM Provider 설정
 * breaddesk.llm.provider 값에 따라 올바른 provider를 선택합니다.
 * 
 * 우선순위:
 * 1. 환경변수로 지정한 provider (ollama/openai/claude)
 * 2. 사용 가능한 첫 번째 provider (API 키가 설정된 것)
 * 3. Fallback: Ollama (로컬, API 키 불필요)
 */
@Slf4j
@Configuration
public class LLMConfig {

    @Bean
    @Primary
    public LLMProvider llmProvider(
            @Value("${breaddesk.llm.provider:ollama}") String providerName,
            @Value("${breaddesk.llm.ollama.url:http://localhost:11434}") String ollamaUrl,
            @Value("${breaddesk.llm.ollama.model:llama3.1:8b}") String ollamaModel,
            @Value("${OPENAI_API_KEY:}") String openaiApiKey,
            @Value("${breaddesk.llm.openai.model:gpt-4o}") String openaiModel,
            @Value("${breaddesk.llm.openai.embedding-model:text-embedding-3-small}") String openaiEmbeddingModel,
            @Value("${ANTHROPIC_API_KEY:}") String anthropicApiKey,
            @Value("${breaddesk.llm.claude.model:claude-sonnet-4-5-20250514}") String claudeModel
    ) {
        log.info("LLM Provider 초기화 중... (선택: {})", providerName);

        LLMProvider provider = switch (providerName.toLowerCase()) {
            case "openai" -> {
                if (openaiApiKey != null && !openaiApiKey.isBlank()) {
                    log.info("✅ OpenAI Provider 활성화 (model: {})", openaiModel);
                    yield new OpenAILLMProvider(openaiApiKey, openaiModel, openaiEmbeddingModel);
                } else {
                    log.warn("⚠️ OpenAI API 키가 설정되지 않음 → Ollama로 fallback");
                    yield new OllamaLLMProvider(ollamaUrl, ollamaModel);
                }
            }
            case "claude" -> {
                if (anthropicApiKey != null && !anthropicApiKey.isBlank()) {
                    log.info("✅ Claude Provider 활성화 (model: {})", claudeModel);
                    yield new ClaudeLLMProvider(anthropicApiKey, claudeModel);
                } else {
                    log.warn("⚠️ Anthropic API 키가 설정되지 않음 → Ollama로 fallback");
                    yield new OllamaLLMProvider(ollamaUrl, ollamaModel);
                }
            }
            default -> {
                log.info("✅ Ollama Provider 활성화 (model: {}, url: {})", ollamaModel, ollamaUrl);
                yield new OllamaLLMProvider(ollamaUrl, ollamaModel);
            }
        };

        // Provider 가용성 체크
        if (provider.isAvailable()) {
            log.info("🚀 LLM Provider 준비 완료: {} (model: {})", providerName, provider.getModelName());
        } else {
            log.error("❌ LLM Provider를 사용할 수 없습니다. 설정을 확인하세요.");
            log.error("   - Ollama: {}", ollamaUrl);
            log.error("   - OpenAI API Key: {}", openaiApiKey != null && !openaiApiKey.isBlank() ? "설정됨" : "없음");
            log.error("   - Anthropic API Key: {}", anthropicApiKey != null && !anthropicApiKey.isBlank() ? "설정됨" : "없음");
        }

        return provider;
    }
}
