package com.breadlab.breaddesk.inquiry.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "inquiries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String channel;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> channelMeta;

    @Column(nullable = false, length = 100)
    private String senderName;

    @Column(length = 200)
    private String senderEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(columnDefinition = "TEXT")
    private String aiResponse;

    private Double aiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.OPEN;

    @Column(name = "task_id")
    private Long taskId;

    @Enumerated(EnumType.STRING)
    private ResolvedBy resolvedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum InquiryStatus {
        OPEN,
        AI_ANSWERED,
        ESCALATED,
        RESOLVED,
        CLOSED
    }

    public enum ResolvedBy {
        AI,
        HUMAN
    }
}
