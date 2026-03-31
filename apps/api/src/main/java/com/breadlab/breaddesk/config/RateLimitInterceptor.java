package com.breadlab.breaddesk.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Simple in-memory rate limiter using sliding window.
 * - Login endpoints: 10 requests per minute
 * - General API: 100 requests per minute
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int LOGIN_LIMIT = 10;
    private static final int GENERAL_LIMIT = 100;
    private static final long WINDOW_MS = 60_000; // 1 minute

    private final ConcurrentHashMap<String, List<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientId = getClientIdentifier(request);
        String requestUri = request.getRequestURI();

        int limit = isLoginEndpoint(requestUri) ? LOGIN_LIMIT : GENERAL_LIMIT;

        if (isRateLimited(clientId, limit)) {
            log.warn("Rate limit exceeded for client: {} on endpoint: {}", clientId, requestUri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return false;
        }

        return true;
    }

    private boolean isRateLimited(String clientId, int limit) {
        long now = System.currentTimeMillis();
        List<Long> timestamps = requestTimestamps.computeIfAbsent(clientId, k -> new CopyOnWriteArrayList<>());

        // Remove old timestamps outside the window
        timestamps.removeIf(timestamp -> now - timestamp > WINDOW_MS);

        // Check if limit exceeded
        if (timestamps.size() >= limit) {
            return true;
        }

        // Add current timestamp
        timestamps.add(now);
        return false;
    }

    private boolean isLoginEndpoint(String requestUri) {
        return requestUri != null && (requestUri.contains("/auth/login") || requestUri.contains("/auth/refresh"));
    }

    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get real IP from proxy headers
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * For testing or monitoring: get current request count for a client
     */
    public int getCurrentRequestCount(String clientId) {
        long now = System.currentTimeMillis();
        List<Long> timestamps = requestTimestamps.get(clientId);
        if (timestamps == null) {
            return 0;
        }
        timestamps.removeIf(timestamp -> now - timestamp > WINDOW_MS);
        return timestamps.size();
    }

    /**
     * For testing: clear all rate limit data
     */
    public void clearAll() {
        requestTimestamps.clear();
    }
}
