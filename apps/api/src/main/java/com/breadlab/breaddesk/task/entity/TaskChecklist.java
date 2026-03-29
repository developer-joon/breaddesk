package com.breadlab.breaddesk.task.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_checklists")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskChecklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long taskId;

    @Column(nullable = false, length = 500)
    private String itemText;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isDone = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer sortOrder = 0;
}
