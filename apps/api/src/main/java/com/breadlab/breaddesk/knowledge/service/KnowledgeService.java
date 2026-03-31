package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeConnectorRepository;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeDocumentRepository;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KnowledgeService {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeConnectorRepository connectorRepository;

    public Page<KnowledgeDocumentEntity> getDocuments(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return documentRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        return documentRepository.findAll(pageable);
    }

    public KnowledgeDocumentEntity getDocument(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("문서를 찾을 수 없습니다: " + id));
    }

    public List<KnowledgeConnectorEntity> getConnectors() {
        return connectorRepository.findAll();
    }

    @Transactional
    public KnowledgeConnectorEntity createConnector(KnowledgeConnectorEntity connector) {
        return connectorRepository.save(connector);
    }

    @Transactional
    public KnowledgeConnectorEntity updateConnector(Long id, KnowledgeConnectorEntity update) {
        KnowledgeConnectorEntity connector = connectorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("커넥터를 찾을 수 없습니다: " + id));
        connector.setSourceType(update.getSourceType());
        connector.setConfig(update.getConfig());
        connector.setSyncIntervalMin(update.getSyncIntervalMin());
        connector.setActive(update.isActive());
        return connectorRepository.save(connector);
    }
}
