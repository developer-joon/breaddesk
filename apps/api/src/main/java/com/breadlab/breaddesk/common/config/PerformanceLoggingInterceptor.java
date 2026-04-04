package com.breadlab.breaddesk.common.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class PerformanceLoggingInterceptor implements HandlerInterceptor {

    private static final long SLOW_REQUEST_THRESHOLD_MS = 200;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute("startTime");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            if (duration > SLOW_REQUEST_THRESHOLD_MS) {
                log.warn("Slow request: {} {} took {}ms", 
                        request.getMethod(), 
                        request.getRequestURI(), 
                        duration);
            }
        }
    }
}
