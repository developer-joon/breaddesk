package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.adapter.ChannelAdapter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Registry that collects all ChannelAdapter beans and allows
 * lookup by channel type.
 */
@Component
public class ChannelAdapterRegistry {

    private final Map<String, ChannelAdapter> adapters;

    public ChannelAdapterRegistry(List<ChannelAdapter> adapterList) {
        this.adapters = adapterList.stream()
                .collect(Collectors.toMap(
                        a -> a.getChannelType().toLowerCase(),
                        Function.identity()));
    }

    public Optional<ChannelAdapter> getAdapter(String channelType) {
        return Optional.ofNullable(adapters.get(channelType.toLowerCase()));
    }

    public ChannelAdapter getAdapterOrThrow(String channelType) {
        return getAdapter(channelType)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No adapter registered for channel: " + channelType));
    }
}
