package com.breadlab.breaddesk.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_tags", indexes = {
        @Index(name = "idx_task_tags_tag", columnList = "tag")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false, length = 100)
    private String tag;
}
