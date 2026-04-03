package com.breadlab.breaddesk.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "knowledge_documents")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeDocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String source;

    @Column(name = "source_id", length = 200)
    private String sourceId;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(length = 1000)
    private String url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String tags;

    @Column(columnDefinition = "vector(768)")
    private String embedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "connector_id")
    private KnowledgeConnectorEntity connector;

    @Column(name = "chunk_index")
    @Builder.Default
    private Integer chunkIndex = 0;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
