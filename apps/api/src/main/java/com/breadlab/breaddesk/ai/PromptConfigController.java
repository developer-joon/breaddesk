package com.breadlab.breaddesk.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Prompt Configuration Controller
 * CRUD for AI system prompts
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai/prompts")
@RequiredArgsConstructor
public class PromptConfigController {

    private final PromptConfigRepository promptConfigRepository;

    /**
     * GET /api/v1/ai/prompts
     * List all prompt configs
     */
    @GetMapping
    public ResponseEntity<List<PromptConfig>> getAllPrompts() {
        return ResponseEntity.ok(promptConfigRepository.findAll());
    }

    /**
     * GET /api/v1/ai/prompts/{id}
     * Get specific prompt config
     */
    @GetMapping("/{id}")
    public ResponseEntity<PromptConfig> getPromptById(@PathVariable Long id) {
        return promptConfigRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/v1/ai/prompts/key/{key}
     * Get prompt by key
     */
    @GetMapping("/key/{key}")
    public ResponseEntity<PromptConfig> getPromptByKey(@PathVariable String key) {
        return promptConfigRepository.findByKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/v1/ai/prompts
     * Create new prompt config
     */
    @PostMapping
    public ResponseEntity<PromptConfig> createPrompt(@RequestBody PromptConfigRequest request) {
        // Check if key already exists
        if (promptConfigRepository.findByKey(request.key()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        PromptConfig config = PromptConfig.builder()
                .key(request.key())
                .name(request.name())
                .promptTemplate(request.promptTemplate())
                .description(request.description())
                .active(request.active() != null ? request.active() : true)
                .build();

        PromptConfig saved = promptConfigRepository.save(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * PUT /api/v1/ai/prompts/{id}
     * Update prompt config
     */
    @PutMapping("/{id}")
    public ResponseEntity<PromptConfig> updatePrompt(
            @PathVariable Long id,
            @RequestBody PromptConfigRequest request) {
        
        return promptConfigRepository.findById(id)
                .map(existing -> {
                    if (request.name() != null) {
                        existing.setName(request.name());
                    }
                    if (request.promptTemplate() != null) {
                        existing.setPromptTemplate(request.promptTemplate());
                    }
                    if (request.description() != null) {
                        existing.setDescription(request.description());
                    }
                    if (request.active() != null) {
                        existing.setActive(request.active());
                    }
                    return ResponseEntity.ok(promptConfigRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/v1/ai/prompts/{id}
     * Delete prompt config
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrompt(@PathVariable Long id) {
        if (!promptConfigRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        promptConfigRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DTO
    public record PromptConfigRequest(
            String key,
            String name,
            String promptTemplate,
            String description,
            Boolean active
    ) {}
}
