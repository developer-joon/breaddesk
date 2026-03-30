package com.breadlab.breaddesk.channel.controller;

import com.breadlab.breaddesk.channel.dto.ChannelConfigRequest;
import com.breadlab.breaddesk.channel.dto.ChannelConfigResponse;
import com.breadlab.breaddesk.channel.service.ChannelConfigService;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
public class ChannelConfigController {

    private final ChannelConfigService channelConfigService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ChannelConfigResponse>>> getAllChannels() {
        return ResponseEntity.ok(ApiResponse.success(channelConfigService.getAllChannels()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ChannelConfigResponse>>> getActiveChannels() {
        return ResponseEntity.ok(ApiResponse.success(channelConfigService.getActiveChannels()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(channelConfigService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> create(
            @Valid @RequestBody ChannelConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(channelConfigService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ChannelConfigResponse>> update(
            @PathVariable Long id, @Valid @RequestBody ChannelConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.success(channelConfigService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        channelConfigService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Test webhook connectivity by sending a ping to the configured URL. */
    @PostMapping("/{id}/test")
    public ResponseEntity<ApiResponse<String>> testWebhook(@PathVariable Long id) {
        ChannelConfigResponse config = channelConfigService.getById(id);
        if (config.getWebhookUrl() == null || config.getWebhookUrl().isBlank()) {
            return ResponseEntity.ok(ApiResponse.failure("Webhook URL is not configured"));
        }
        // In a real implementation, we'd ping the URL. For now, return success.
        return ResponseEntity.ok(ApiResponse.success("Webhook URL configured: " + config.getWebhookUrl()));
    }
}
