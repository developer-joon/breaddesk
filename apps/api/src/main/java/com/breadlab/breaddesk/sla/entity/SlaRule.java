package com.breadlab.breaddesk.sla.entity;

import com.breadlab.breaddesk.task.entity.TaskUrgency;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "sla_rules")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private TaskUrgency urgency;

    @Column(name = "response_minutes", nullable = false)
    private Integer responseMinutes;

    @Column(name = "resolve_minutes", nullable = false)
    private Integer resolveMinutes;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
