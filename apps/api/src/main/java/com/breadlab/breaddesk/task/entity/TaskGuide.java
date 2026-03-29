package com.breadlab.breaddesk.task.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@Table(name = "task_guides")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskGuide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "checklist_json", columnDefinition = "jsonb")
    private String checklistJson;

    @Column(name = "related_docs_json", columnDefinition = "jsonb")
    private String relatedDocsJson;

    @Column(name = "similar_tasks_json", columnDefinition = "jsonb")
    private String similarTasksJson;

    @Column(columnDefinition = "text")
    private String guidelines;

    @Column(name = "estimated_hours")
    private Float estimatedHours;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}
