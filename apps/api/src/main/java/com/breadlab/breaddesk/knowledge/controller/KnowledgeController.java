package com.breadlab.breaddesk.knowledge.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.knowledge.dto.*;
import com.breadlab.breaddesk.knowledge.entity.*;
import com.breadlab.breaddesk.knowledge.repository.*;
import com.breadlab.breaddesk.knowledge.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeConnectorRepository connectorRepository;
    private final VectorSearchService vectorSearchService;
    private final KnowledgeSyncService syncService;
    private final ObjectMapper objectMapper;

    @GetMapping("/documents")
    public ApiResponse<Page<KnowledgeDocumentResponse>> listDocuments(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<KnowledgeDocumentEntity> docs = q.isBlank() ?
                documentRepository.findAll(pageable) :
                documentRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q, pageable);

        return ApiResponse.success(docs.map(KnowledgeDocumentResponse::from));
    }

    @GetMapping("/documents/{id}")
    public ApiResponse<KnowledgeDocumentResponse> getDocument(@PathVariable Long id) {
        KnowledgeDocumentEntity doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다: " + id));
        return ApiResponse.success(KnowledgeDocumentResponse.from(doc));
    }

    @PostMapping("/search")
    public ApiResponse<List<VectorSearchResponse>> vectorSearch(@Valid @RequestBody VectorSearchRequest request) {
        List<VectorSearchResponse> results = vectorSearchService
                .search(request.query(), request.effectiveLimit(), request.effectiveThreshold())
                .stream().map(VectorSearchResponse::from).toList();
        return ApiResponse.success(results);
    }

    @GetMapping("/connectors")
    public ApiResponse<List<KnowledgeConnectorResponse>> listConnectors() {
        return ApiResponse.success(connectorRepository.findAll().stream()
                .map(KnowledgeConnectorResponse::from).toList());
    }

    @PostMapping("/connectors")
    public ApiResponse<KnowledgeConnectorResponse> createConnector(@Valid @RequestBody KnowledgeConnectorRequest request) {
        try {
            KnowledgeConnectorEntity entity = KnowledgeConnectorEntity.builder()
                    .name(request.name()).sourceType(request.sourceType())
                    .config(objectMapper.writeValueAsString(request.config()))
                    .syncIntervalMin(request.syncIntervalMin() != null ? request.syncIntervalMin() : 60)
                    .active(true).createdAt(LocalDateTime.now()).build();
            entity = connectorRepository.save(entity);
            return ApiResponse.success(KnowledgeConnectorResponse.from(entity));
        } catch (Exception e) {
            throw new RuntimeException("커넥터 생성 실패: " + e.getMessage(), e);
        }
    }

    @PutMapping("/connectors/{id}")
    public ApiResponse<KnowledgeConnectorResponse> updateConnector(
            @PathVariable Long id, @Valid @RequestBody KnowledgeConnectorRequest request) {
        KnowledgeConnectorEntity entity = connectorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("커넥터를 찾을 수 없습니다: " + id));

        try {
            if (request.name() != null) entity.setName(request.name());
            if (request.sourceType() != null) entity.setSourceType(request.sourceType());
            if (request.config() != null) entity.setConfig(objectMapper.writeValueAsString(request.config()));
            if (request.syncIntervalMin() != null) entity.setSyncIntervalMin(request.syncIntervalMin());

            entity = connectorRepository.save(entity);
            return ApiResponse.success(KnowledgeConnectorResponse.from(entity));
        } catch (Exception e) {
            throw new RuntimeException("커넥터 업데이트 실패: " + e.getMessage(), e);
        }
    }

    @PostMapping("/connectors/{id}/sync")
    public ResponseEntity<ApiResponse<String>> triggerSync(@PathVariable Long id) {
        int count = syncService.syncConnector(id);
        return ResponseEntity.ok(ApiResponse.success("동기화 완료: " + count + " 청크 저장"));
    }
}
