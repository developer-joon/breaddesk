package com.breadlab.breaddesk.task.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanResponse {
    private Map<String, List<TaskResponse>> columns;
    private Integer totalCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Column {
        private String status;
        private List<TaskResponse> tasks;
        private Integer count;
    }
}
