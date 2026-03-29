package com.breadlab.breaddesk.knowledge.repository;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeConnectorRepository extends JpaRepository<KnowledgeConnectorEntity, Long> {

    List<KnowledgeConnectorEntity> findByActiveTrue();

    List<KnowledgeConnectorEntity> findBySourceType(String sourceType);
}
