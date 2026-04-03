package com.breadlab.breaddesk.channel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

/**
 * Unified incoming message from any channel
 */
@Getter
@Setter
@Entity
@Table(name = "channel_messages")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType channelType;

    @Column(nullable = false, length = 200)
    private String source; // e.g., "slack:C12345", "email:support@example.com"

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sender_info", columnDefinition = "jsonb")
    private String senderInfo; // JSON: { "name": "...", "email": "...", "userId": "..." }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channel_metadata", columnDefinition = "jsonb")
    private String channelMetadata; // Channel-specific metadata

    @Column(name = "inquiry_id")
    private Long inquiryId; // Link to created inquiry

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;
}
