package com.breadlab.breaddesk.personal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalNoteResponse {

    private Long id;
    private Long memberId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
