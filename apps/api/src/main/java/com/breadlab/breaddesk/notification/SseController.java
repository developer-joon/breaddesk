package com.breadlab.breaddesk.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Server-Sent Events (SSE) Controller for real-time notifications
 * 
 * Replaces polling with push-based event streaming
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
public class SseController {

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * GET /api/v1/notifications/stream
     * 
     * Opens an SSE connection for real-time notifications
     * 
     * Client usage (JavaScript):
     * const eventSource = new EventSource('/api/v1/notifications/stream');
     * eventSource.addEventListener('notification', (event) => {
     *   const data = JSON.parse(event.data);
     *   console.log('Notification:', data);
     * });
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        
        emitters.add(emitter);
        log.info("New SSE connection established. Total connections: {}", emitters.size());

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.info("SSE connection completed. Remaining connections: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.warn("SSE connection timed out. Remaining connections: {}", emitters.size());
        });

        emitter.onError((ex) -> {
            emitters.remove(emitter);
            log.error("SSE connection error: {}", ex.getMessage());
        });

        // Send initial connection message
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "message", "Connected to BreadDesk notifications",
                            "timestamp", System.currentTimeMillis()
                    )));
        } catch (IOException e) {
            log.error("Failed to send initial SSE message", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * Listen to notification events and broadcast to all SSE clients
     */
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.debug("Broadcasting notification to {} clients: {}", emitters.size(), event.getType());
        
        CopyOnWriteArrayList<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(event));
            } catch (IOException e) {
                log.warn("Failed to send notification to client, marking for removal: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        // Remove dead connections
        emitters.removeAll(deadEmitters);
    }

    /**
     * Send heartbeat every 30 seconds to keep connections alive
     */
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }

        log.trace("Sending heartbeat to {} SSE clients", emitters.size());
        
        CopyOnWriteArrayList<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data(Map.of("timestamp", System.currentTimeMillis())));
            } catch (IOException e) {
                log.warn("Heartbeat failed for client, marking for removal: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }

    /**
     * Get current connection count (for monitoring)
     */
    public int getActiveConnectionCount() {
        return emitters.size();
    }
}
