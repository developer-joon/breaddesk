package com.breadlab.breaddesk.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Correlation ID 필터 - 요청별 추적 ID 생성 및 MDC 설정
 * Loki/로그 분석에서 요청 추적을 용이하게 함
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            // 헤더에서 Correlation ID 가져오거나 새로 생성
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = generateCorrelationId();
            }

            // MDC에 설정 (로그에 자동 포함됨)
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

            // 응답 헤더에도 추가
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            // 요청 처리 후 MDC 정리 (메모리 누수 방지)
            MDC.clear();
        }
    }

    private String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
