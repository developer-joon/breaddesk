package com.breadlab.breaddesk.inquiry.dto;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponse {
    private Long id;
    private String channel;
    private Map<String, Object> channelMeta;
    private String senderName;
    private String senderEmail;
    private String message;
    private String aiResponse;
    private Double aiConfidence;
    private Inquiry.InquiryStatus status;
    private Long taskId;
    private Inquiry.ResolvedBy resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private List<MessageItem> conversation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageItem {
        private Long id;
        private InquiryMessage.MessageRole role;
        private String message;
        private LocalDateTime createdAt;

        public static MessageItem from(InquiryMessage msg) {
            return MessageItem.builder()
                    .id(msg.getId())
                    .role(msg.getRole())
                    .message(msg.getMessage())
                    .createdAt(msg.getCreatedAt())
                    .build();
        }
    }

    public static InquiryResponse from(Inquiry inquiry, List<InquiryMessage> messages) {
        InquiryResponseBuilder builder = InquiryResponse.builder()
                .id(inquiry.getId())
                .channel(inquiry.getChannel())
                .channelMeta(inquiry.getChannelMeta())
                .senderName(inquiry.getSenderName())
                .senderEmail(inquiry.getSenderEmail())
                .message(inquiry.getMessage())
                .aiResponse(inquiry.getAiResponse())
                .aiConfidence(inquiry.getAiConfidence())
                .status(inquiry.getStatus())
                .taskId(inquiry.getTaskId())
                .resolvedBy(inquiry.getResolvedBy())
                .createdAt(inquiry.getCreatedAt())
                .resolvedAt(inquiry.getResolvedAt());

        if (messages != null) {
            builder.conversation(messages.stream()
                    .map(MessageItem::from)
                    .toList());
        }

        return builder.build();
    }

    public static InquiryResponse from(Inquiry inquiry) {
        return from(inquiry, null);
    }
}
