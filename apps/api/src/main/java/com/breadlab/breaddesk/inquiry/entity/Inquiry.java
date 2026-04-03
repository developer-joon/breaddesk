package com.breadlab.breaddesk.inquiry.entity;

import com.breadlab.breaddesk.task.entity.Task;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "inquiries")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String channel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_meta", columnDefinition = "jsonb")
    private String channelMeta;

    @Column(name = "sender_name", nullable = false, length = 100)
    private String senderName;

    @Column(name = "sender_email", length = 200)
    private String senderEmail;

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(name = "ai_response", columnDefinition = "text")
    private String aiResponse;

    @Column(name = "ai_confidence")
    private Float aiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private com.breadlab.breaddesk.team.entity.Team team;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolved_by", length = 10)
    private InquiryResolvedBy resolvedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryMessage> messages = new ArrayList<>();
}
