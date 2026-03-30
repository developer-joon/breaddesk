package com.breadlab.breaddesk.channel.repository;

import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelConfigRepository extends JpaRepository<ChannelConfig, Long> {

    Optional<ChannelConfig> findByChannelType(String channelType);

    List<ChannelConfig> findByIsActiveTrue();
}
