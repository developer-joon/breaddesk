package com.breadlab.breaddesk.task.entity;

import com.breadlab.breaddesk.auth.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String type = "GENERAL";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskUrgency urgency = TaskUrgency.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.WAITING;

    @Column(length = 100)
    private String requesterName;

    @Column(length = 200)
    private String requesterEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Member assignee;

    @Column(name = "inquiry_id")
    private Long inquiryId;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    private LocalDate dueDate;

    private Double estimatedHours;

    private Double actualHours;

    private LocalDateTime slaResponseDeadline;

    private LocalDateTime slaResolveDeadline;

    private LocalDateTime slaRespondedAt;

    @Builder.Default
    private Boolean slaResponseBreached = false;

    @Builder.Default
    private Boolean slaResolveBreached = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TaskStatus {
        WAITING,
        IN_PROGRESS,
        REVIEW,
        DONE
    }

    public enum TaskUrgency {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }
}
