package com.breadlab.breaddesk.csat;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Customer Satisfaction Survey entity
 */
@Entity
@Table(name = "csat_surveys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsatSurvey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long inquiryId;

    @Column(nullable = false, unique = true)
    private String token; // UUID for public access

    @Column
    private Integer rating; // 1-5 stars

    @Column(length = 1000)
    private String feedback; // Optional text feedback

    @Column
    private LocalDateTime sentAt;

    @Column
    private LocalDateTime respondedAt;

    @Column
    private Boolean responded = false;

    @Column
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }
}
