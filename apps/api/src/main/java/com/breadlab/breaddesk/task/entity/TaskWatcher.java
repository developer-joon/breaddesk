package com.breadlab.breaddesk.task.entity;

import com.breadlab.breaddesk.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "task_watchers", uniqueConstraints = @UniqueConstraint(columnNames = {"task_id", "member_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskWatcher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
