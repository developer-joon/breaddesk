package com.breadlab.breaddesk.webchat;

import com.breadlab.breaddesk.webchat.dto.WebchatMessageRequest;
import com.breadlab.breaddesk.webchat.dto.WebchatMessageResponse;
import com.breadlab.breaddesk.webchat.dto.WebchatSessionRequest;
import com.breadlab.breaddesk.webchat.dto.WebchatSessionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/webchat")
@RequiredArgsConstructor
public class WebchatController {

    private final WebchatService webchatService;

    /**
     * 웹챗 세션 생성
     * POST /api/v1/webchat/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<WebchatSessionResponse> createSession(@RequestBody WebchatSessionRequest request) {
        log.info("웹챗 세션 생성 요청: senderName={}", request.getSenderName());
        WebchatSessionResponse response = webchatService.createSession(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 웹챗 메시지 전송
     * POST /api/v1/webchat/sessions/{sessionId}/messages
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<WebchatMessageResponse> sendMessage(
            @PathVariable String sessionId,
            @Valid @RequestBody WebchatMessageRequest request) {
        log.info("웹챗 메시지 전송: sessionId={}, message={}", sessionId, request.getMessage());
        WebchatMessageResponse response = webchatService.sendMessage(sessionId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 웹챗 메시지 히스토리 조회
     * GET /api/v1/webchat/sessions/{sessionId}/messages
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<List<WebchatMessageResponse>> getMessages(@PathVariable String sessionId) {
        log.info("웹챗 메시지 히스토리 조회: sessionId={}", sessionId);
        List<WebchatMessageResponse> messages = webchatService.getMessages(sessionId);
        return ResponseEntity.ok(messages);
    }
}
