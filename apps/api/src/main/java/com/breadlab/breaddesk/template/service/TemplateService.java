package com.breadlab.breaddesk.template.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.template.dto.TemplateRequest;
import com.breadlab.breaddesk.template.dto.TemplateResponse;
import com.breadlab.breaddesk.template.entity.ReplyTemplate;
import com.breadlab.breaddesk.template.repository.ReplyTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateService {

    private final ReplyTemplateRepository templateRepository;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    @Transactional
    public TemplateResponse createTemplate(TemplateRequest request) {
        ReplyTemplate template = ReplyTemplate.builder()
                .title(request.getTitle())
                .category(request.getCategory())
                .content(request.getContent())
                .build();

        ReplyTemplate saved = templateRepository.save(template);
        log.info("Created template: {} ({})", saved.getId(), saved.getTitle());

        return TemplateResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public TemplateResponse getTemplate(Long id) {
        ReplyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));
        return TemplateResponse.from(template);
    }

    @Transactional(readOnly = true)
    public Page<TemplateResponse> getTemplates(String category, Pageable pageable) {
        Page<ReplyTemplate> templates = category != null
                ? templateRepository.findByCategory(category, pageable)
                : templateRepository.findAll(pageable);

        return templates.map(TemplateResponse::from);
    }

    @Transactional
    public TemplateResponse updateTemplate(Long id, TemplateRequest.Update request) {
        ReplyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));

        if (request.getTitle() != null) {
            template.setTitle(request.getTitle());
        }
        if (request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }
        if (request.getContent() != null) {
            template.setContent(request.getContent());
        }

        ReplyTemplate updated = templateRepository.save(template);
        log.info("Updated template: {}", id);

        return TemplateResponse.from(updated);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Template", id);
        }
        templateRepository.deleteById(id);
        log.info("Deleted template: {}", id);
    }

    /**
     * 템플릿 변수 치환
     * 예: {{이름}} → "홍길동"
     */
    public String renderTemplate(Long id, Map<String, String> variables) {
        ReplyTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));

        String content = template.getContent();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String varName = matcher.group(1);
            String replacement = variables.getOrDefault(varName, "{{" + varName + "}}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        // 사용 횟수 증가
        template.setUsageCount(template.getUsageCount() + 1);
        templateRepository.save(template);

        return result.toString();
    }
}
