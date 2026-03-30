package com.breadlab.breaddesk.channel.entity;

import com.breadlab.breaddesk.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "channel_configs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChannelConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_type", nullable = false, unique = true, length = 50)
    private String channelType;

    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    @Column(name = "auth_token", length = 500)
    private String authToken;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(columnDefinition = "jsonb default '{}'")
    @Builder.Default
    private String config = "{}";
}
