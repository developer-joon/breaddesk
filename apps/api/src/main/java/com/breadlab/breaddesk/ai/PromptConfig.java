package com.breadlab.breaddesk.ai;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * AI Prompt Configuration entity
 * Allows customization of system prompts for different AI features
 */
@Entity
@Table(name = "prompt_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String key; // e.g., "ai_answer", "classification", "summary"

    @Column(nullable = false, length = 100)
    private String name; // Human-readable name

    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptTemplate; // The actual prompt with placeholders

    @Column(length = 500)
    private String description; // What this prompt does

    @Column
    private Boolean active = true;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
