package com.breadlab.breaddesk.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터 (Phase 1 스텁 구현)
 * 
 * TODO Phase 2: 실제 JWT 검증 로직 구현
 * - Authorization 헤더에서 토큰 추출
 * - 토큰 유효성 검증
 * - SecurityContext에 인증 정보 설정
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        // Phase 1: 인증 스킵 (모든 요청 허용)
        // TODO Phase 2: JWT 검증 로직 추가
        log.trace("JWT filter (Phase 1 stub): {}", request.getRequestURI());
        
        filterChain.doFilter(request, response);
    }
}
