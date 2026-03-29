package com.breadlab.breaddesk.template.repository;

import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReplyTemplateRepository extends JpaRepository<ReplyTemplate, Long> {
    Page<ReplyTemplate> findByCategory(String category, Pageable pageable);
    Page<ReplyTemplate> findAllByOrderByUsageCountDesc(Pageable pageable);
}
