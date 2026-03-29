package com.breadlab.breaddesk.knowledge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "knowledge_connectors")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeConnectorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String name;

    @Column(name = "source_type", nullable = false, length = 50)
    private String sourceType;

    @Column(columnDefinition = "jsonb", nullable = false)
    private String config;

    @Column(name = "sync_interval_min", nullable = false)
    @Builder.Default
    private Integer syncIntervalMin = 60;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
