package com.breadlab.breaddesk.inquiry.controller;

import com.breadlab.breaddesk.auth.JwtTokenProvider;
import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.inquiry.dto.*;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
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

@WebMvcTest(value = InquiryController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {com.breadlab.breaddesk.auth.JwtAuthenticationFilter.class,
                        com.breadlab.breaddesk.auth.SecurityConfig.class}))
class InquiryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InquiryService inquiryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private com.breadlab.breaddesk.config.RateLimitInterceptor rateLimitInterceptor;

    private InquiryResponse sampleResponse() {
        return InquiryResponse.builder()
                .id(1L)
                .channel("email")
                .senderName("Customer")
                .senderEmail("customer@example.com")
                .message("Question")
                .status(InquiryStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("should_createInquiry_when_noAuth")
    void should_createInquiry_when_noAuth() throws Exception {
        // POST /api/v1/inquiries is permitAll
        given(inquiryService.createInquiry(any(InquiryRequest.class))).willReturn(sampleResponse());

        InquiryRequest request = TestDataFactory.createInquiryRequest();

        mockMvc.perform(post("/api/v1/inquiries")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.channel").value("email"));
    }

    @Test
    @WithMockUser
    @DisplayName("should_getAllInquiries_when_authenticated")
    void should_getAllInquiries_when_authenticated() throws Exception {
        given(inquiryService.getAllInquiries(any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/v1/inquiries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    @DisplayName("should_getInquiryById_when_authenticated")
    void should_getInquiryById_when_authenticated() throws Exception {
        given(inquiryService.getInquiryById(1L)).willReturn(sampleResponse());

        mockMvc.perform(get("/api/v1/inquiries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("should_updateStatus_when_authenticated")
    void should_updateStatus_when_authenticated() throws Exception {
        InquiryResponse updated = InquiryResponse.builder()
                .id(1L).channel("email").status(InquiryStatus.RESOLVED)
                .senderName("Customer").message("Q").createdAt(LocalDateTime.now()).build();

        given(inquiryService.updateInquiryStatus(anyLong(), any(InquiryStatusUpdateRequest.class)))
                .willReturn(updated);

        InquiryStatusUpdateRequest request = TestDataFactory.createInquiryStatusUpdateRequest(InquiryStatus.RESOLVED);

        mockMvc.perform(patch("/api/v1/inquiries/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    @WithMockUser
    @DisplayName("should_addMessage_when_authenticated")
    void should_addMessage_when_authenticated() throws Exception {
        InquiryMessageResponse msgResponse = InquiryMessageResponse.builder()
                .id(1L).inquiryId(1L).role(InquiryMessageRole.AGENT)
                .message("Response").createdAt(LocalDateTime.now()).build();

        given(inquiryService.addMessage(anyLong(), any(InquiryMessageRequest.class)))
                .willReturn(msgResponse);

        InquiryMessageRequest request = TestDataFactory.createInquiryMessageRequest();

        mockMvc.perform(post("/api/v1/inquiries/1/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.role").value("AGENT"));
    }

    @Test
    @WithMockUser
    @DisplayName("should_convertToTask_when_authenticated")
    void should_convertToTask_when_authenticated() throws Exception {
        InquiryResponse converted = InquiryResponse.builder()
                .id(1L).channel("email").status(InquiryStatus.ESCALATED)
                .taskId(10L).senderName("Customer").message("Q")
                .createdAt(LocalDateTime.now()).build();

        given(inquiryService.convertToTask(anyLong(), any(ConvertToTaskRequest.class)))
                .willReturn(converted);

        ConvertToTaskRequest request = TestDataFactory.createConvertToTaskRequest();

        mockMvc.perform(post("/api/v1/inquiries/1/convert-to-task")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ESCALATED"))
                .andExpect(jsonPath("$.data.taskId").value(10));
    }

    @Test
    @WithMockUser
    @DisplayName("should_deleteInquiry_when_authenticated")
    void should_deleteInquiry_when_authenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/inquiries/1").with(csrf()))
                .andExpect(status().isOk());
    }
}
