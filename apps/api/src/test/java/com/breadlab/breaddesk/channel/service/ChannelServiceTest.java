package com.breadlab.breaddesk.channel.service;

import com.breadlab.breaddesk.channel.entity.ChannelMessage;
import com.breadlab.breaddesk.channel.entity.ChannelType;
import com.breadlab.breaddesk.channel.repository.ChannelMessageRepository;
import com.breadlab.breaddesk.inquiry.dto.InquiryRequest;
import com.breadlab.breaddesk.inquiry.dto.InquiryResponse;
import com.breadlab.breaddesk.inquiry.service.InquiryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChannelService 테스트")
class ChannelServiceTest {

    @Mock
    private ChannelMessageRepository channelMessageRepository;

    @Mock
    private InquiryService inquiryService;

    @InjectMocks
    private ChannelService channelService;

    private ChannelMessage testMessage;
    private InquiryResponse testInquiryResponse;

    @BeforeEach
    void setUp() {
        testMessage = new ChannelMessage();
        testMessage.setId(1L);
        testMessage.setChannelType(ChannelType.EMAIL);
        testMessage.setSource("test@example.com");
        testMessage.setContent("배송 문의입니다");
        testMessage.setSenderInfo("{\"name\":\"홍길동\",\"email\":\"hong@example.com\"}");
        testMessage.setChannelMetadata("{}");
        testMessage.setCreatedAt(LocalDateTime.now());
        testMessage.setProcessed(false);

        testInquiryResponse = new InquiryResponse();
        testInquiryResponse.setId(1L);
    }

    @Test
    @DisplayName("receiveMessage - 메시지 수신 및 처리")
    void receiveMessage_shouldSaveAndProcess() {
        // given
        when(channelMessageRepository.save(any(ChannelMessage.class))).thenAnswer(invocation -> {
            ChannelMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            return msg;
        });
        when(inquiryService.createInquiry(any(InquiryRequest.class))).thenReturn(testInquiryResponse);

        // when
        ChannelMessage result = channelService.receiveMessage(
                ChannelType.EMAIL,
                "test@example.com",
                "배송 문의입니다",
                "{\"name\":\"홍길동\",\"email\":\"hong@example.com\"}",
                "{}"
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(channelMessageRepository, atLeastOnce()).save(any(ChannelMessage.class));
    }

    @Test
    @DisplayName("processMessage - 문의 생성 성공")
    void processMessage_shouldCreateInquiry() {
        // given
        when(inquiryService.createInquiry(any(InquiryRequest.class))).thenReturn(testInquiryResponse);
        when(channelMessageRepository.save(any(ChannelMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        channelService.processMessage(testMessage);

        // then
        assertThat(testMessage.isProcessed()).isTrue();
        assertThat(testMessage.getInquiryId()).isEqualTo(1L);
        assertThat(testMessage.getProcessedAt()).isNotNull();
        verify(inquiryService).createInquiry(any(InquiryRequest.class));
        verify(channelMessageRepository).save(testMessage);
    }

    @Test
    @DisplayName("processMessage - 이미 처리된 메시지는 스킵")
    void processMessage_whenAlreadyProcessed_shouldSkip() {
        // given
        testMessage.setProcessed(true);

        // when
        channelService.processMessage(testMessage);

        // then
        verify(inquiryService, never()).createInquiry(any());
        verify(channelMessageRepository, never()).save(any());
    }

    @Test
    @DisplayName("processUnprocessedMessages - 미처리 메시지 일괄 처리")
    void processUnprocessedMessages_shouldProcessAll() {
        // given
        ChannelMessage msg2 = new ChannelMessage();
        msg2.setId(2L);
        msg2.setChannelType(ChannelType.WEB_CHAT);
        msg2.setContent("환불 문의");
        msg2.setSenderInfo("{\"name\":\"김철수\"}");
        msg2.setChannelMetadata("{}");
        msg2.setProcessed(false);

        List<ChannelMessage> unprocessed = new ArrayList<>();
        unprocessed.add(testMessage);
        unprocessed.add(msg2);

        when(channelMessageRepository.findByProcessedFalseOrderByCreatedAtAsc()).thenReturn(unprocessed);
        when(inquiryService.createInquiry(any(InquiryRequest.class))).thenReturn(testInquiryResponse);
        when(channelMessageRepository.save(any(ChannelMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        channelService.processUnprocessedMessages();

        // then
        verify(inquiryService, times(2)).createInquiry(any(InquiryRequest.class));
        verify(channelMessageRepository, times(2)).save(any(ChannelMessage.class));
    }

    @Test
    @DisplayName("receiveMessage - 처리 실패해도 메시지는 저장")
    void receiveMessage_whenProcessFails_shouldStillSave() {
        // given
        when(channelMessageRepository.save(any(ChannelMessage.class))).thenAnswer(invocation -> {
            ChannelMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            return msg;
        });
        when(inquiryService.createInquiry(any(InquiryRequest.class))).thenThrow(new RuntimeException("Process error"));

        // when
        ChannelMessage result = channelService.receiveMessage(
                ChannelType.EMAIL,
                "test@example.com",
                "배송 문의입니다",
                "{\"name\":\"홍길동\"}",
                "{}"
        );

        // then
        assertThat(result).isNotNull();
        verify(channelMessageRepository, atLeastOnce()).save(any(ChannelMessage.class));
    }
}
