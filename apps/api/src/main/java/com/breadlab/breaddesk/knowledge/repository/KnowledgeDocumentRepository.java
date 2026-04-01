package com.breadlab.breaddesk.knowledge.repository;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, Long> {

    Page<KnowledgeDocumentEntity> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    List<KnowledgeDocumentEntity> findBySource(String source);

    @Query(value = "SELECT *, 1 - (embedding <=> cast(:queryVector as vector)) as similarity " +
            "FROM knowledge_documents WHERE embedding IS NOT NULL " +
            "ORDER BY embedding <=> cast(:queryVector as vector) LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findSimilarDocuments(@Param("queryVector") String queryVector, @Param("limit") int limit);

    boolean existsBySourceAndSourceId(String source, String sourceId);
}
