package com.breadlab.breaddesk.template.repository;

import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReplyTemplateRepository extends JpaRepository<ReplyTemplate, Long> {

    Page<ReplyTemplate> findByCategory(String category, Pageable pageable);

    @Query("SELECT DISTINCT r.category FROM ReplyTemplate r WHERE r.category IS NOT NULL")
    List<String> findDistinctCategories();

    @Query("SELECT r FROM ReplyTemplate r WHERE r.title LIKE %:keyword% OR r.content LIKE %:keyword%")
    List<ReplyTemplate> searchByKeyword(@Param("keyword") String keyword);
}
