package com.breadlab.breaddesk.auth;

import com.breadlab.breaddesk.auth.dto.LoginRequest;
import com.breadlab.breaddesk.auth.dto.RefreshTokenRequest;
import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthController.class,
        includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
                classes = {JwtAuthenticationFilter.class, SecurityConfig.class}))
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private MemberRepository memberRepository;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private com.breadlab.breaddesk.config.RateLimitInterceptor rateLimitInterceptor;

    @Test
    @DisplayName("should_login_when_validCredentials")
    void should_login_when_validCredentials() throws Exception {
        // Given
        Member member = TestDataFactory.createMember();
        member.setId(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken("agent@test.com", "password");
        given(authenticationManager.authenticate(any())).willReturn(auth);
        given(memberRepository.findByEmail("agent@test.com")).willReturn(Optional.of(member));
        given(jwtTokenProvider.generateAccessToken(anyString(), anyString())).willReturn("access-token");
        given(jwtTokenProvider.generateRefreshToken(anyString(), anyString())).willReturn("refresh-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("agent@test.com");
        request.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));
    }

    @Test
    @DisplayName("should_returnUnauthorized_when_invalidCredentials")
    void should_returnUnauthorized_when_invalidCredentials() throws Exception {
        // Given
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setEmail("wrong@test.com");
        request.setPassword("wrongpassword");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("should_refreshToken_when_validRefreshToken")
    void should_refreshToken_when_validRefreshToken() throws Exception {
        // Given
        Member member = TestDataFactory.createMember();
        member.setId(1L);

        given(jwtTokenProvider.validateToken("valid-refresh")).willReturn(true);
        given(jwtTokenProvider.extractSubject("valid-refresh")).willReturn("agent@test.com");
        given(memberRepository.findByEmail("agent@test.com")).willReturn(Optional.of(member));
        given(jwtTokenProvider.generateAccessToken(anyString(), anyString())).willReturn("new-access");
        given(jwtTokenProvider.generateRefreshToken(anyString(), anyString())).willReturn("new-refresh");

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid-refresh");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("new-access"));
    }

    @Test
    @DisplayName("should_failRefresh_when_invalidRefreshToken")
    void should_failRefresh_when_invalidRefreshToken() throws Exception {
        // Given
        given(jwtTokenProvider.validateToken("invalid-token")).willReturn(false);

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid-token");

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound()); // ResourceNotFoundException
    }
}
