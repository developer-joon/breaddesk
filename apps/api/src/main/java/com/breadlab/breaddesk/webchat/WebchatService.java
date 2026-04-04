package com.breadlab.breaddesk.webchat;

import com.breadlab.breaddesk.ai.AIAnswerService;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.webchat.dto.WebchatMessageRequest;
import com.breadlab.breaddesk.webchat.dto.WebchatMessageResponse;
import com.breadlab.breaddesk.webchat.dto.WebchatSessionRequest;
import com.breadlab.breaddesk.webchat.dto.WebchatSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WebchatService {

    private final InquiryRepository inquiryRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final AIAnswerService aiAnswerService;

    /**
     * 웹챗 세션 생성
     */
    @Transactional
    public WebchatSessionResponse createSession(WebchatSessionRequest request) {
        String sessionId = UUID.randomUUID().toString();
        String token = "webchat_" + sessionId; // 간단한 토큰 (실제 환경에선 JWT 사용 권장)
        
        log.info("웹챗 세션 생성: sessionId={}, senderName={}, senderEmail={}", 
                sessionId, request.getSenderName(), request.getSenderEmail());
        
        return WebchatSessionResponse.builder()
                .sessionId(sessionId)
                .token(token)
                .build();
    }

    /**
     * 웹챗 메시지 전송 (첫 메시지면 Inquiry 생성)
     */
    @Transactional
    public WebchatMessageResponse sendMessage(String sessionId, WebchatMessageRequest request) {
        // sessionId로 기존 문의 조회 (channelMeta에 sessionId 저장)
        List<Inquiry> existingInquiries = inquiryRepository.findByChannelAndChannelMeta("WEBCHAT", sessionId);
        
        Inquiry inquiry;
        InquiryMessage userMessage;
        
        if (existingInquiries.isEmpty()) {
            // 첫 메시지 → 새 Inquiry 생성
            inquiry = Inquiry.builder()
                    .channel("WEBCHAT")
                    .channelMeta(sessionId)
                    .senderName("웹챗 사용자") // 기본값, 나중에 업데이트 가능
                    .senderEmail(null)
                    .message(request.getMessage())
                    .status(InquiryStatus.OPEN)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            inquiry = inquiryRepository.save(inquiry);
            log.info("새 웹챗 문의 생성: inquiryId={}, sessionId={}", inquiry.getId(), sessionId);
            
            // 사용자 메시지 저장
            userMessage = InquiryMessage.builder()
                    .inquiry(inquiry)
                    .role(InquiryMessageRole.USER)
                    .message(request.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();
            inquiryMessageRepository.save(userMessage);
            
            // AI 자동 답변 시도
            boolean aiResolved = aiAnswerService.tryAutoAnswer(inquiry);
            
            // 최신 상태 다시 로드
            inquiry = inquiryRepository.findById(inquiry.getId()).orElseThrow();
            
        } else {
            // 기존 문의에 메시지 추가
            inquiry = existingInquiries.get(0);
            
            userMessage = InquiryMessage.builder()
                    .inquiry(inquiry)
                    .role(InquiryMessageRole.USER)
                    .message(request.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();
            inquiryMessageRepository.save(userMessage);
            
            log.info("기존 웹챗 문의에 메시지 추가: inquiryId={}, sessionId={}", inquiry.getId(), sessionId);
        }
        
        return WebchatMessageResponse.builder()
                .messageId(userMessage.getId())
                .role("USER")
                .message(userMessage.getMessage())
                .aiResponse(inquiry.getAiResponse())
                .aiConfidence(inquiry.getAiConfidence())
                .createdAt(userMessage.getCreatedAt())
                .build();
    }

    /**
     * 웹챗 메시지 히스토리 조회
     */
    public List<WebchatMessageResponse> getMessages(String sessionId) {
        List<Inquiry> inquiries = inquiryRepository.findByChannelAndChannelMeta("WEBCHAT", sessionId);
        
        if (inquiries.isEmpty()) {
            return List.of();
        }
        
        Inquiry inquiry = inquiries.get(0);
        List<InquiryMessage> messages = inquiryMessageRepository.findByInquiryIdOrderByCreatedAtAsc(inquiry.getId());
        
        return messages.stream()
                .map(msg -> WebchatMessageResponse.builder()
                        .messageId(msg.getId())
                        .role(msg.getRole().name())
                        .message(msg.getMessage())
                        .createdAt(msg.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
