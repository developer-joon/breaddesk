package com.breadlab.breaddesk.automation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "automation_rules")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Boolean active;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 50)
    private TriggerType triggerType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_json", columnDefinition = "jsonb", nullable = false)
    private String conditionJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_json", columnDefinition = "jsonb", nullable = false)
    private String actionJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
