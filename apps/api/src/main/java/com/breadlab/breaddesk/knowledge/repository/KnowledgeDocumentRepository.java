package com.breadlab.breaddesk.knowledge.repository;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocumentEntity, Long> {

    Page<KnowledgeDocumentEntity> findBySource(String source, Pageable pageable);

    List<KnowledgeDocumentEntity> findBySourceAndSourceId(String source, String sourceId);

    @Query(value = """
            SELECT kd.*, 1 - (kd.embedding <=> cast(:queryVector as vector)) as similarity
            FROM knowledge_documents kd
            WHERE kd.embedding IS NOT NULL
              AND 1 - (kd.embedding <=> cast(:queryVector as vector)) >= :threshold
            ORDER BY kd.embedding <=> cast(:queryVector as vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findSimilarDocuments(
            @Param("queryVector") String queryVector,
            @Param("threshold") double threshold,
            @Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM KnowledgeDocumentEntity d WHERE d.source = :source AND d.sourceId = :sourceId")
    void deleteBySourceAndSourceId(@Param("source") String source, @Param("sourceId") String sourceId);

    Page<KnowledgeDocumentEntity> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title, String content, Pageable pageable);

    long countByConnectorId(Long connectorId);
}
