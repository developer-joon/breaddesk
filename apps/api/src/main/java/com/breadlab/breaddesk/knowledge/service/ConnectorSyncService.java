package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.knowledge.connector.ConfluenceConnector;
import com.breadlab.breaddesk.knowledge.connector.KnowledgeConnector;
import com.breadlab.breaddesk.knowledge.connector.KnowledgeDocument;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeConnectorRepository;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeDocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * Handles synchronization of knowledge documents from external connectors
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectorSyncService {

    private final KnowledgeConnectorRepository connectorRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Trigger manual sync for a connector
     */
    @Transactional
    public void syncConnector(Long connectorId) {
        KnowledgeConnectorEntity connector = connectorRepository.findById(connectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Connector not found: " + connectorId));

        if (!connector.isActive()) {
            throw new IllegalStateException("Connector is not active: " + connectorId);
        }

        log.info("Starting sync for connector #{} ({})", connectorId, connector.getSourceType());

        try {
            KnowledgeConnector connectorImpl = createConnectorInstance(connector);
            
            // Determine if this is incremental or full sync
            List<KnowledgeDocument> documents;
            if (connector.getLastSyncedAt() != null) {
                Instant lastSync = connector.getLastSyncedAt().toInstant(ZoneOffset.UTC);
                documents = connectorImpl.fetchUpdatedSince(lastSync);
                log.info("Incremental sync: fetched {} documents since {}", documents.size(), lastSync);
            } else {
                documents = connectorImpl.fetchDocuments();
                log.info("Full sync: fetched {} documents", documents.size());
            }

            // Save documents
            int saved = 0;
            int updated = 0;
            int skipped = 0;

            for (KnowledgeDocument doc : documents) {
                try {
                    boolean exists = documentRepository.existsBySourceAndSourceId(
                            doc.source(), 
                            doc.sourceId()
                    );

                    // Generate embedding
                    float[] embedding = embeddingService.embed(doc.title() + "\n\n" + doc.content());
                    String embeddingStr = embeddingService.floatArrayToString(embedding);

                    // Convert tags list to JSON array string
                    String tagsJson = objectMapper.writeValueAsString(doc.tags());

                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime updatedAt = LocalDateTime.ofInstant(doc.updatedAt(), ZoneOffset.UTC);

                    if (exists) {
                        // Update existing document
                        var existingList = documentRepository.findBySource(doc.source());
                        var existing = existingList.stream()
                                .filter(d -> d.getSourceId().equals(doc.sourceId()))
                                .findFirst()
                                .orElse(null);

                        if (existing != null) {
                            existing.setTitle(doc.title());
                            existing.setContent(doc.content());
                            existing.setUrl(doc.url());
                            existing.setTags(tagsJson);
                            existing.setEmbedding(embeddingStr);
                            existing.setSyncedAt(now);
                            documentRepository.save(existing);
                            updated++;
                        }
                    } else {
                        // Create new document
                        KnowledgeDocumentEntity entity = KnowledgeDocumentEntity.builder()
                                .source(doc.source())
                                .sourceId(doc.sourceId())
                                .title(doc.title())
                                .content(doc.content())
                                .url(doc.url())
                                .tags(tagsJson)
                                .embedding(embeddingStr)
                                .connector(connector)
                                .chunkIndex(0)
                                .syncedAt(now)
                                .createdAt(now)
                                .build();
                        documentRepository.save(entity);
                        saved++;
                    }

                } catch (Exception e) {
                    log.error("Failed to save document {} from {}: {}", 
                            doc.sourceId(), connector.getSourceType(), e.getMessage());
                    skipped++;
                }
            }

            // Update connector last synced time
            connector.setLastSyncedAt(LocalDateTime.now());
            connectorRepository.save(connector);

            log.info("Sync completed for connector #{}: {} new, {} updated, {} skipped", 
                    connectorId, saved, updated, skipped);

        } catch (Exception e) {
            log.error("Sync failed for connector #{}: {}", connectorId, e.getMessage(), e);
            throw new RuntimeException("Connector sync failed: " + e.getMessage(), e);
        }
    }

    /**
     * Create connector implementation instance based on type and config
     */
    private KnowledgeConnector createConnectorInstance(KnowledgeConnectorEntity connector) {
        String type = connector.getSourceType();
        
        try {
            Map<String, Object> config = objectMapper.readValue(connector.getConfig(), Map.class);

            return switch (type.toLowerCase()) {
                case "confluence" -> {
                    String baseUrl = (String) config.get("baseUrl");
                    String username = (String) config.get("username");
                    String apiToken = (String) config.get("apiToken");
                    String spaceKey = (String) config.get("spaceKey");
                    
                    if (baseUrl == null || username == null || apiToken == null || spaceKey == null) {
                        throw new IllegalArgumentException(
                                "Missing required config fields for Confluence: baseUrl, username, apiToken, spaceKey");
                    }
                    
                    yield new ConfluenceConnector(baseUrl, username, apiToken, spaceKey);
                }
                default -> throw new IllegalArgumentException("Unsupported connector type: " + type);
            };

        } catch (Exception e) {
            throw new RuntimeException("Failed to create connector instance: " + e.getMessage(), e);
        }
    }
}
