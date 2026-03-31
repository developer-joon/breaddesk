package com.breadlab.breaddesk.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * IP 기반 Rate Limiting 인터셉터
 * - 로그인: 분당 10회
 * - 일반 API: 분당 100회
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int LOGIN_LIMIT = 10;
    private static final int API_LIMIT = 100;
    private static final long WINDOW_SIZE_MS = 60_000; // 1분

    // IP별 요청 카운터 (key: IP, value: 요청 정보)
    private final Map<String, RateLimitInfo> loginLimits = new ConcurrentHashMap<>();
    private final Map<String, RateLimitInfo> apiLimits = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientIp = getClientIp(request);
        String requestUri = request.getRequestURI();

        // 로그인 요청인지 확인
        if (requestUri.contains("/api/v1/auth/login")) {
            return checkRateLimit(clientIp, loginLimits, LOGIN_LIMIT, response, "로그인");
        } else if (requestUri.startsWith("/api/v1/")) {
            return checkRateLimit(clientIp, apiLimits, API_LIMIT, response, "API");
        }

        return true;
    }

    private boolean checkRateLimit(String clientIp, Map<String, RateLimitInfo> limits, int maxRequests,
            HttpServletResponse response, String type) throws Exception {
        long now = System.currentTimeMillis();
        RateLimitInfo info = limits.computeIfAbsent(clientIp, k -> new RateLimitInfo());

        synchronized (info) {
            // 시간 윈도우가 지났으면 리셋
            if (now - info.windowStart > WINDOW_SIZE_MS) {
                info.windowStart = now;
                info.count.set(0);
            }

            int currentCount = info.count.incrementAndGet();
            if (currentCount > maxRequests) {
                log.warn("{} rate limit exceeded for IP: {} ({}회/분)", type, clientIp, currentCount);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write(String.format(
                        "{\"success\":false,\"message\":\"%s 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.\",\"data\":null}",
                        type));
                return false;
            }
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 첫 번째 IP만 사용 (프록시 체인의 경우)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private static class RateLimitInfo {
        long windowStart = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}
