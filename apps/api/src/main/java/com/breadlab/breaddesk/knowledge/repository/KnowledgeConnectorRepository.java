package com.breadlab.breaddesk.knowledge.repository;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeConnectorRepository extends JpaRepository<KnowledgeConnectorEntity, Long> {
    List<KnowledgeConnectorEntity> findByIsActiveTrue();
}
