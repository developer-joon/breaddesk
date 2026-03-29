package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.knowledge.connector.ConnectorFactory;
import com.breadlab.breaddesk.knowledge.connector.KnowledgeConnector;
import com.breadlab.breaddesk.knowledge.connector.KnowledgeDocument;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeConnectorRepository;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeDocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSyncService {

    private final KnowledgeConnectorRepository connectorRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final ConnectorFactory connectorFactory;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 300_000)
    public void scheduledSync() {
        List<KnowledgeConnectorEntity> activeConnectors = connectorRepository.findByActiveTrue();
        for (KnowledgeConnectorEntity connector : activeConnectors) {
            if (shouldSync(connector)) {
                try {
                    syncConnector(connector.getId());
                } catch (Exception e) {
                    log.error("커넥터 #{} 동기화 실패: {}", connector.getId(), e.getMessage(), e);
                }
            }
        }
    }

    @Transactional
    public int syncConnector(Long connectorId) {
        KnowledgeConnectorEntity connectorEntity = connectorRepository.findById(connectorId)
                .orElseThrow(() -> new IllegalArgumentException("커넥터를 찾을 수 없습니다: " + connectorId));

        KnowledgeConnector connector = connectorFactory.create(connectorEntity);

        List<KnowledgeDocument> documents;
        if (connectorEntity.getLastSyncedAt() != null) {
            var lastSync = connectorEntity.getLastSyncedAt().atZone(ZoneId.systemDefault()).toInstant();
            documents = connector.fetchUpdatedSince(lastSync);
            log.info("커넥터 #{} 증분 동기화: {} 문서", connectorId, documents.size());
        } else {
            documents = connector.fetchDocuments();
            log.info("커넥터 #{} 전체 동기화: {} 문서", connectorId, documents.size());
        }

        int savedCount = 0;
        for (KnowledgeDocument doc : documents) {
            try {
                savedCount += processDocument(doc, connectorEntity);
            } catch (Exception e) {
                log.error("문서 처리 실패 (sourceId: {}): {}", doc.sourceId(), e.getMessage());
            }
        }

        connectorEntity.setLastSyncedAt(LocalDateTime.now());
        connectorRepository.save(connectorEntity);

        log.info("커넥터 #{} 동기화 완료: {} 청크 저장", connectorId, savedCount);
        return savedCount;
    }

    private int processDocument(KnowledgeDocument doc, KnowledgeConnectorEntity connector) {
        documentRepository.deleteBySourceAndSourceId(doc.source(), doc.sourceId());

        List<EmbeddingService.ChunkWithEmbedding> chunks = embeddingService.chunkAndEmbed(doc.content());

        if (chunks.isEmpty()) {
            KnowledgeDocumentEntity entity = KnowledgeDocumentEntity.builder()
                    .source(doc.source()).sourceId(doc.sourceId()).title(doc.title())
                    .content(doc.content()).url(doc.url()).tags(toJsonString(doc.tags()))
                    .connector(connector).chunkIndex(0)
                    .syncedAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                    .build();
            documentRepository.save(entity);
            return 1;
        }

        for (EmbeddingService.ChunkWithEmbedding chunk : chunks) {
            KnowledgeDocumentEntity entity = KnowledgeDocumentEntity.builder()
                    .source(doc.source()).sourceId(doc.sourceId()).title(doc.title())
                    .content(chunk.content()).url(doc.url()).tags(toJsonString(doc.tags()))
                    .embedding(embeddingService.toVectorString(chunk.embedding()))
                    .connector(connector).chunkIndex(chunk.chunkIndex())
                    .syncedAt(LocalDateTime.now()).createdAt(LocalDateTime.now())
                    .build();
            documentRepository.save(entity);
        }

        return chunks.size();
    }

    private boolean shouldSync(KnowledgeConnectorEntity connector) {
        if (connector.getLastSyncedAt() == null) return true;
        LocalDateTime nextSync = connector.getLastSyncedAt().plusMinutes(connector.getSyncIntervalMin());
        return LocalDateTime.now().isAfter(nextSync);
    }

    private String toJsonString(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (Exception e) {
            return "[]";
        }
    }
}
