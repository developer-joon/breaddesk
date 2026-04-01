package com.breadlab.breaddesk.knowledge.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.service.KnowledgeService;
import com.breadlab.breaddesk.knowledge.service.VectorSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final VectorSearchService vectorSearchService;
    private final com.breadlab.breaddesk.knowledge.service.ConnectorSyncService connectorSyncService;

    @GetMapping("/documents")
    public ResponseEntity<ApiResponse<Page<KnowledgeDocumentEntity>>> getDocuments(
            @RequestParam(required = false) String keyword, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.getDocuments(keyword, pageable)));
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<ApiResponse<KnowledgeDocumentEntity>> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.getDocument(id)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<Object[]>>> searchDocuments(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "");
        int limit = Integer.parseInt(request.getOrDefault("limit", "5"));
        return ResponseEntity.ok(ApiResponse.success(vectorSearchService.search(query, limit)));
    }

    @GetMapping("/connectors")
    public ResponseEntity<ApiResponse<List<KnowledgeConnectorEntity>>> getConnectors() {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.getConnectors()));
    }

    @PostMapping("/connectors")
    public ResponseEntity<ApiResponse<KnowledgeConnectorEntity>> createConnector(
            @RequestBody KnowledgeConnectorEntity connector) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.createConnector(connector)));
    }

    @PutMapping("/connectors/{id}")
    public ResponseEntity<ApiResponse<KnowledgeConnectorEntity>> updateConnector(
            @PathVariable Long id, @RequestBody KnowledgeConnectorEntity connector) {
        return ResponseEntity.ok(ApiResponse.success(knowledgeService.updateConnector(id, connector)));
    }

    @PostMapping("/connectors/{id}/sync")
    public ResponseEntity<ApiResponse<String>> syncConnector(@PathVariable Long id) {
        connectorSyncService.syncConnector(id);
        return ResponseEntity.ok(ApiResponse.success("Sync completed successfully"));
    }
}
