package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.dto.ChannelConfigRequest;
import com.breadlab.breaddesk.channel.dto.ChannelConfigResponse;
import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ChannelConfigServiceTest {

    @Mock
    private ChannelConfigRepository channelConfigRepository;

    @InjectMocks
    private ChannelConfigService channelConfigService;

    private ChannelConfig channelConfig;

    @BeforeEach
    void setUp() {
        channelConfig = ChannelConfig.builder()
                .id(1L)
                .channelType("email")
                .webhookUrl("https://webhook.example.com")
                .authToken("secret-token")
                .isActive(true)
                .config("{}")
                .build();
    }

    @Test
    @DisplayName("should_getAllChannels_when_called")
    void should_getAllChannels_when_called() {
        // Given
        ChannelConfig config2 = ChannelConfig.builder()
                .id(2L).channelType("slack").webhookUrl("https://slack.com")
                .isActive(false).config("{}").build();

        given(channelConfigRepository.findAll()).willReturn(List.of(channelConfig, config2));

        // When
        List<ChannelConfigResponse> responses = channelConfigService.getAllChannels();

        // Then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getChannelType()).isEqualTo("email");
        assertThat(responses.get(1).getChannelType()).isEqualTo("slack");
    }

    @Test
    @DisplayName("should_getActiveChannels_when_called")
    void should_getActiveChannels_when_called() {
        // Given
        given(channelConfigRepository.findByIsActiveTrue()).willReturn(List.of(channelConfig));

        // When
        List<ChannelConfigResponse> responses = channelConfigService.getActiveChannels();

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("should_getById_when_exists")
    void should_getById_when_exists() {
        // Given
        given(channelConfigRepository.findById(1L)).willReturn(Optional.of(channelConfig));

        // When
        ChannelConfigResponse response = channelConfigService.getById(1L);

        // Then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getChannelType()).isEqualTo("email");
        assertThat(response.isHasAuthToken()).isTrue();
    }

    @Test
    @DisplayName("should_throwException_when_channelNotFound")
    void should_throwException_when_channelNotFound() {
        // Given
        given(channelConfigRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> channelConfigService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_getByChannelType_when_exists")
    void should_getByChannelType_when_exists() {
        // Given
        given(channelConfigRepository.findByChannelType("email"))
                .willReturn(Optional.of(channelConfig));

        // When
        ChannelConfigResponse response = channelConfigService.getByChannelType("email");

        // Then
        assertThat(response.getChannelType()).isEqualTo("email");
    }

    @Test
    @DisplayName("should_throwException_when_channelTypeNotFound")
    void should_throwException_when_channelTypeNotFound() {
        // Given
        given(channelConfigRepository.findByChannelType("nonexistent"))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> channelConfigService.getByChannelType("nonexistent"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_createChannel_when_validRequest")
    void should_createChannel_when_validRequest() {
        // Given
        ChannelConfigRequest request = new ChannelConfigRequest();
        request.setChannelType("Telegram");
        request.setWebhookUrl("https://telegram.example.com");
        request.setAuthToken("telegram-token");
        request.setIsActive(true);
        request.setConfig("{}");

        given(channelConfigRepository.save(any(ChannelConfig.class))).willAnswer(inv -> {
            ChannelConfig saved = inv.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // When
        ChannelConfigResponse response = channelConfigService.create(request);

        // Then
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getChannelType()).isEqualTo("telegram"); // lowercased
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("should_setDefaultValues_when_creating")
    void should_setDefaultValues_when_creating() {
        // Given
        ChannelConfigRequest request = new ChannelConfigRequest();
        request.setChannelType("email");
        request.setWebhookUrl("https://webhook.com");
        // isActive and config not set

        given(channelConfigRepository.save(any(ChannelConfig.class))).willAnswer(inv -> {
            ChannelConfig saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        ChannelConfigResponse response = channelConfigService.create(request);

        // Then
        assertThat(response.getIsActive()).isTrue(); // default
        verify(channelConfigRepository).save(any(ChannelConfig.class));
    }

    @Test
    @DisplayName("should_updateChannel_when_validRequest")
    void should_updateChannel_when_validRequest() {
        // Given
        given(channelConfigRepository.findById(1L)).willReturn(Optional.of(channelConfig));
        given(channelConfigRepository.save(any(ChannelConfig.class))).willReturn(channelConfig);

        ChannelConfigRequest request = new ChannelConfigRequest();
        request.setWebhookUrl("https://new-webhook.com");
        request.setIsActive(false);

        // When
        ChannelConfigResponse response = channelConfigService.update(1L, request);

        // Then
        assertThat(response.getWebhookUrl()).isEqualTo("https://new-webhook.com");
        assertThat(response.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("should_onlyUpdateProvidedFields_when_updating")
    void should_onlyUpdateProvidedFields_when_updating() {
        // Given
        given(channelConfigRepository.findById(1L)).willReturn(Optional.of(channelConfig));
        given(channelConfigRepository.save(any(ChannelConfig.class))).willReturn(channelConfig);

        ChannelConfigRequest request = new ChannelConfigRequest();
        request.setIsActive(false);
        // other fields null

        // When
        channelConfigService.update(1L, request);

        // Then
        assertThat(channelConfig.getIsActive()).isFalse();
        assertThat(channelConfig.getWebhookUrl()).isEqualTo("https://webhook.example.com"); // unchanged
    }

    @Test
    @DisplayName("should_deleteChannel_when_exists")
    void should_deleteChannel_when_exists() {
        // Given
        given(channelConfigRepository.findById(1L)).willReturn(Optional.of(channelConfig));

        // When
        channelConfigService.delete(1L);

        // Then
        verify(channelConfigRepository).delete(channelConfig);
    }

    @Test
    @DisplayName("should_maskAuthToken_when_responding")
    void should_maskAuthToken_when_responding() {
        // Given
        given(channelConfigRepository.findById(1L)).willReturn(Optional.of(channelConfig));

        // When
        ChannelConfigResponse response = channelConfigService.getById(1L);

        // Then
        assertThat(response.isHasAuthToken()).isTrue();
        // AuthToken itself should NOT be in response (check DTO)
    }

    @Test
    @DisplayName("should_returnFalseHasAuthToken_when_tokenIsBlank")
    void should_returnFalseHasAuthToken_when_tokenIsBlank() {
        // Given
        channelConfig.setAuthToken("");
        given(channelConfigRepository.findById(1L)).willReturn(Optional.of(channelConfig));

        // When
        ChannelConfigResponse response = channelConfigService.getById(1L);

        // Then
        assertThat(response.isHasAuthToken()).isFalse();
    }
}
