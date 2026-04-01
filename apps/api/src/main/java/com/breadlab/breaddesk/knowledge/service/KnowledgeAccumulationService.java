package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 에이전트 답변을 자동으로 지식베이스에 축적하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeAccumulationService {

    private final KnowledgeDocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    /**
     * 에이전트가 문의에 답변할 때 자동으로 지식 문서 생성
     * 
     * @param inquiry 원본 문의
     * @param agentAnswer 에이전트의 답변
     */
    @Transactional
    public void accumulateFromAgentReply(Inquiry inquiry, String agentAnswer) {
        if (agentAnswer == null || agentAnswer.trim().isEmpty()) {
            log.debug("Empty agent answer, skipping knowledge accumulation");
            return;
        }

        // Check if we already have a document for this inquiry
        boolean exists = documentRepository.existsBySourceAndSourceId("inquiry", inquiry.getId().toString());
        if (exists) {
            log.debug("Knowledge document already exists for inquiry #{}", inquiry.getId());
            return;
        }

        String title = generateTitle(inquiry);
        String content = formatContent(inquiry, agentAnswer);

        try {
            // Generate embedding
            float[] embedding = embeddingService.embed(content);
            String embeddingStr = embeddingService.floatArrayToString(embedding);

            KnowledgeDocumentEntity document = KnowledgeDocumentEntity.builder()
                    .source("inquiry")
                    .sourceId(inquiry.getId().toString())
                    .title(title)
                    .content(content)
                    .url(null) // No external URL for inquiries
                    .tags(buildTagsJson(inquiry))
                    .embedding(embeddingStr)
                    .chunkIndex(0)
                    .syncedAt(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            documentRepository.save(document);
            log.info("Created knowledge document from inquiry #{}: {}", inquiry.getId(), title);
        } catch (Exception e) {
            log.error("Failed to create knowledge document from inquiry #{}: {}", inquiry.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private String generateTitle(Inquiry inquiry) {
        // Use inquiry message as title, truncate if too long
        String message = inquiry.getMessage();
        if (message.length() > 100) {
            return message.substring(0, 97) + "...";
        }
        return message;
    }

    private String formatContent(Inquiry inquiry, String agentAnswer) {
        StringBuilder content = new StringBuilder();
        content.append("Question:\n");
        content.append(inquiry.getMessage());
        content.append("\n\n");
        content.append("Answer:\n");
        content.append(agentAnswer);
        
        // Include sender context for better search
        if (inquiry.getSenderName() != null) {
            content.append("\n\nRequester: ").append(inquiry.getSenderName());
        }
        if (inquiry.getSenderEmail() != null) {
            content.append(" (").append(inquiry.getSenderEmail()).append(")");
        }
        
        return content.toString();
    }

    private String buildTagsJson(Inquiry inquiry) {
        // Simple JSON array for tags
        StringBuilder tags = new StringBuilder("[");
        tags.append("\"inquiry\"");
        
        if (inquiry.getChannel() != null) {
            tags.append(", \"").append(inquiry.getChannel()).append("\"");
        }
        
        tags.append("]");
        return tags.toString();
    }
}
