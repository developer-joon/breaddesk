package com.breadlab.breaddesk.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromptConfigRepository extends JpaRepository<PromptConfig, Long> {
    
    Optional<PromptConfig> findByKey(String key);
    
    Optional<PromptConfig> findByKeyAndActiveTrue(String key);
}
