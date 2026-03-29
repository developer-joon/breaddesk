package com.breadlab.breaddesk.task.controller;

import com.breadlab.breaddesk.auth.JwtAuthenticationFilter;
import com.breadlab.breaddesk.auth.JwtTokenProvider;
import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.task.dto.*;
import com.breadlab.breaddesk.task.entity.TaskStatus;
import com.breadlab.breaddesk.task.entity.TaskUrgency;
import com.breadlab.breaddesk.task.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private UserDetailsService userDetailsService;

    private TaskResponse sampleResponse() {
        return TaskResponse.builder()
                .id(1L)
                .title("Test Task")
                .description("Description")
                .type("GENERAL")
                .urgency(TaskUrgency.NORMAL)
                .status(TaskStatus.WAITING)
                .transferCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("should_createTask_when_authenticated")
    void should_createTask_when_authenticated() throws Exception {
        // Given
        given(taskService.createTask(any(TaskRequest.class))).willReturn(sampleResponse());

        TaskRequest request = TestDataFactory.createTaskRequest();

        // When & Then
        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Task"));
    }

    @Test
    @WithMockUser
    @DisplayName("should_getAllTasks_when_authenticated")
    void should_getAllTasks_when_authenticated() throws Exception {
        given(taskService.getAllTasks(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("should_getTaskById_when_authenticated")
    void should_getTaskById_when_authenticated() throws Exception {
        given(taskService.getTaskById(1L)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("should_updateTask_when_authenticated")
    void should_updateTask_when_authenticated() throws Exception {
        TaskResponse updated = TaskResponse.builder()
                .id(1L).title("Updated").type("GENERAL")
                .urgency(TaskUrgency.HIGH).status(TaskStatus.IN_PROGRESS)
                .transferCount(0).createdAt(LocalDateTime.now()).build();

        given(taskService.updateTask(anyLong(), any(TaskRequest.class))).willReturn(updated);

        TaskRequest request = TestDataFactory.createTaskRequest();
        request.setTitle("Updated");

        mockMvc.perform(put("/api/v1/tasks/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated"));
    }

    @Test
    @WithMockUser
    @DisplayName("should_updateTaskStatus_when_authenticated")
    void should_updateTaskStatus_when_authenticated() throws Exception {
        TaskResponse updated = TaskResponse.builder()
                .id(1L).title("Task").type("GENERAL")
                .urgency(TaskUrgency.NORMAL).status(TaskStatus.IN_PROGRESS)
                .transferCount(0).createdAt(LocalDateTime.now()).build();

        given(taskService.updateTaskStatus(anyLong(), any(TaskStatusUpdateRequest.class)))
                .willReturn(updated);

        TaskStatusUpdateRequest request = TestDataFactory.createTaskStatusUpdateRequest(TaskStatus.IN_PROGRESS);

        mockMvc.perform(patch("/api/v1/tasks/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_PROGRESS"));
    }

    @Test
    @WithMockUser
    @DisplayName("should_deleteTask_when_authenticated")
    void should_deleteTask_when_authenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("should_getKanbanView_when_authenticated")
    void should_getKanbanView_when_authenticated() throws Exception {
        TaskKanbanResponse kanban = TaskKanbanResponse.builder()
                .waiting(List.of(sampleResponse()))
                .inProgress(List.of())
                .pending(List.of())
                .review(List.of())
                .done(List.of())
                .build();

        given(taskService.getKanbanView()).willReturn(kanban);

        mockMvc.perform(get("/api/v1/tasks/kanban"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("should_returnUnauthorized_when_noAuth")
    void should_returnUnauthorized_when_noAuth() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }
}
