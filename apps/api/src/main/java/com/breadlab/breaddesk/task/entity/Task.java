package com.breadlab.breaddesk.task.entity;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 50, nullable = false)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskUrgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "requester_name", length = 100)
    private String requesterName;

    @Column(name = "requester_email", length = 200)
    private String requesterEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private Member assignee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private com.breadlab.breaddesk.team.entity.Team team;

    @OneToOne(mappedBy = "task", fetch = FetchType.LAZY)
    private Inquiry inquiry;

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    private LocalDate dueDate;
    private Float estimatedHours;
    private Float actualHours;

    private LocalDateTime slaResponseDeadline;
    private LocalDateTime slaResolveDeadline;
    private LocalDateTime slaRespondedAt;

    @Column(name = "sla_response_breached", nullable = false)
    private boolean slaResponseBreached;

    @Column(name = "sla_resolve_breached", nullable = false)
    private boolean slaResolveBreached;

    @Column(name = "jira_issue_key", length = 50)
    private String jiraIssueKey;

    @Column(name = "jira_issue_url", length = 500)
    private String jiraIssueUrl;

    @Column(name = "transfer_count", nullable = false)
    private int transferCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskChecklist> checklists = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskComment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskLog> logs = new ArrayList<>();
}
