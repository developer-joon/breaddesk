package com.breadlab.breaddesk.channel.webchat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time web chat
 * Maintains active sessions and broadcasts messages
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    
    // sessionId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = extractSessionId(session);
        sessions.put(sessionId, session);
        log.info("WebSocket connected: {}", sessionId);
        
        // Send welcome message
        sendToSession(sessionId, Map.of(
                "type", "connected",
                "sessionId", sessionId,
                "message", "Connected to BreadDesk chat"
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = extractSessionId(session);
        String payload = message.getPayload();
        
        log.debug("Received message from {}: {}", sessionId, payload);
        
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            
            String type = (String) data.get("type");
            
            switch (type) {
                case "ping":
                    sendToSession(sessionId, Map.of("type", "pong"));
                    break;
                case "typing":
                    // Could broadcast typing indicator to agents
                    log.debug("User {} is typing", sessionId);
                    break;
                default:
                    log.warn("Unknown message type: {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to handle message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = extractSessionId(session);
        sessions.remove(sessionId);
        log.info("WebSocket disconnected: {} (status: {})", sessionId, status);
    }

    /**
     * Send message to specific session
     */
    public void sendToSession(String sessionId, Object message) {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("Failed to send message to {}: {}", sessionId, e.getMessage());
            }
        }
    }

    /**
     * Broadcast message to all connected sessions
     */
    public void broadcast(Object message) {
        sessions.forEach((id, session) -> sendToSession(id, message));
    }

    private String extractSessionId(WebSocketSession session) {
        // Extract sessionId from query params or generate new
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query != null && query.contains("sessionId=")) {
            return query.split("sessionId=")[1].split("&")[0];
        }
        return session.getId();
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
