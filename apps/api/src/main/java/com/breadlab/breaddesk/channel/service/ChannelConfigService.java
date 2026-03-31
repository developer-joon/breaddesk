package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.dto.ChannelConfigRequest;
import com.breadlab.breaddesk.channel.dto.ChannelConfigResponse;
import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChannelConfigService {

    private final ChannelConfigRepository channelConfigRepository;

    public List<ChannelConfigResponse> getAllChannels() {
        return channelConfigRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ChannelConfigResponse> getActiveChannels() {
        return channelConfigRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public ChannelConfigResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public ChannelConfigResponse getByChannelType(String channelType) {
        return toResponse(channelConfigRepository.findByChannelType(channelType)
                .orElseThrow(() -> new ResourceNotFoundException("Channel config not found: " + channelType)));
    }

    @Transactional
    public ChannelConfigResponse create(ChannelConfigRequest request) {
        ChannelConfig config = ChannelConfig.builder()
                .channelType(request.getChannelType().toLowerCase())
                .webhookUrl(request.getWebhookUrl())
                .authToken(request.getAuthToken())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .config(request.getConfig() != null ? request.getConfig() : "{}")
                .build();

        return toResponse(channelConfigRepository.save(config));
    }

    @Transactional
    public ChannelConfigResponse update(Long id, ChannelConfigRequest request) {
        ChannelConfig config = findOrThrow(id);

        if (request.getWebhookUrl() != null) {
            config.setWebhookUrl(request.getWebhookUrl());
        }
        if (request.getAuthToken() != null) {
            config.setAuthToken(request.getAuthToken());
        }
        if (request.getIsActive() != null) {
            config.setIsActive(request.getIsActive());
        }
        if (request.getConfig() != null) {
            config.setConfig(request.getConfig());
        }

        return toResponse(channelConfigRepository.save(config));
    }

    @Transactional
    public void delete(Long id) {
        ChannelConfig config = findOrThrow(id);
        channelConfigRepository.delete(config);
    }

    private ChannelConfig findOrThrow(Long id) {
        return channelConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel config not found: " + id));
    }

    private ChannelConfigResponse toResponse(ChannelConfig entity) {
        return ChannelConfigResponse.builder()
                .id(entity.getId())
                .channelType(entity.getChannelType())
                .webhookUrl(entity.getWebhookUrl())
                .isActive(entity.getIsActive())
                .config(entity.getConfig())
                .hasAuthToken(entity.getAuthToken() != null && !entity.getAuthToken().isBlank())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
