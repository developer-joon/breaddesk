package com.breadlab.breaddesk.channel.repository;

import com.breadlab.breaddesk.channel.entity.ChannelMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelMessageRepository extends JpaRepository<ChannelMessage, Long> {
    List<ChannelMessage> findByProcessedFalseOrderByCreatedAtAsc();
    List<ChannelMessage> findByInquiryIdOrderByCreatedAtAsc(Long inquiryId);
}
