package com.breadlab.breaddesk.template.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.template.dto.ReplyTemplateRequest;
import com.breadlab.breaddesk.template.dto.ReplyTemplateResponse;
import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import com.breadlab.breaddesk.template.repository.ReplyTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyTemplateService {

    private final ReplyTemplateRepository replyTemplateRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReplyTemplateResponse createTemplate(ReplyTemplateRequest request, Long createdBy) {
        ReplyTemplate template = ReplyTemplate.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .usageCount(0)
                .createdBy(memberRepository.findById(createdBy).orElse(null))
                .build();

        return toResponse(replyTemplateRepository.save(template));
    }

    public Page<ReplyTemplateResponse> getAllTemplates(Pageable pageable) {
        return replyTemplateRepository.findAll(pageable).map(this::toResponse);
    }

    public Page<ReplyTemplateResponse> getTemplatesByCategory(String category, Pageable pageable) {
        return replyTemplateRepository.findByCategory(category, pageable).map(this::toResponse);
    }

    public ReplyTemplateResponse getTemplateById(Long id) {
        ReplyTemplate template = findTemplateOrThrow(id);
        return toResponse(template);
    }

    @Transactional
    public ReplyTemplateResponse updateTemplate(Long id, ReplyTemplateRequest request) {
        ReplyTemplate template = findTemplateOrThrow(id);
        
        template.setTitle(request.getTitle());
        template.setCategory(request.getCategory());
        template.setContent(request.getContent());

        return toResponse(replyTemplateRepository.save(template));
    }

    @Transactional
    public String applyTemplate(Long id, Map<String, String> variables) {
        ReplyTemplate template = findTemplateOrThrow(id);
        
        template.setUsageCount(template.getUsageCount() + 1);
        replyTemplateRepository.save(template);

        String content = template.getContent();
        if (variables != null) {
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                content = content.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }
        }

        return content;
    }

    @Transactional
    public void deleteTemplate(Long id) {
        ReplyTemplate template = findTemplateOrThrow(id);
        replyTemplateRepository.delete(template);
    }

    private ReplyTemplate findTemplateOrThrow(Long id) {
        return replyTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + id));
    }

    private ReplyTemplateResponse toResponse(ReplyTemplate template) {
        return ReplyTemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .category(template.getCategory())
                .content(template.getContent())
                .usageCount(template.getUsageCount())
                .createdBy(template.getCreatedBy() != null ? template.getCreatedBy().getId() : null)
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
