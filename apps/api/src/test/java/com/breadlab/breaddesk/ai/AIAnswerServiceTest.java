package com.breadlab.breaddesk.ai;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessage;
import com.breadlab.breaddesk.inquiry.entity.InquiryMessageRole;
import com.breadlab.breaddesk.inquiry.entity.InquiryResolvedBy;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryMessageRepository;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.service.VectorSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AIAnswerService 테스트")
class AIAnswerServiceTest {

    @Mock
    private LLMProvider llmProvider;

    @Mock
    private VectorSearchService vectorSearchService;

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private InquiryMessageRepository inquiryMessageRepository;

    @InjectMocks
    private AIAnswerService aiAnswerService;

    private Inquiry testInquiry;
    private KnowledgeDocumentEntity testDoc;

    @BeforeEach
    void setUp() {
        testInquiry = new Inquiry();
        testInquiry.setId(1L);
        testInquiry.setMessage("배송은 얼마나 걸리나요?");
        testInquiry.setStatus(InquiryStatus.OPEN);
        testInquiry.setCreatedAt(LocalDateTime.now());

        testDoc = new KnowledgeDocumentEntity();
        testDoc.setId(1L);
        testDoc.setTitle("배송 안내");
        testDoc.setContent("일반 배송은 2-3일 소요됩니다.");
        testDoc.setUrl("https://example.com/shipping");
    }

    @Test
    @DisplayName("tryAutoAnswer - LLM 사용 불가 시 false 반환")
    void tryAutoAnswer_whenLLMUnavailable_shouldReturnFalse() {
        // given
        when(llmProvider.isAvailable()).thenReturn(false);

        // when
        boolean result = aiAnswerService.tryAutoAnswer(testInquiry);

        // then
        assertThat(result).isFalse();
        verify(llmProvider).isAvailable();
        verifyNoMoreInteractions(vectorSearchService, inquiryRepository, inquiryMessageRepository);
    }

    @Test
    @DisplayName("tryAutoAnswer - confidence >= 0.7 시 AI_ANSWERED 상태로 변경")
    void tryAutoAnswer_whenHighConfidence_shouldAutoResolve() {
        // given
        when(llmProvider.isAvailable()).thenReturn(true);
        
        Object[] searchResult = new Object[]{testDoc, 0.85};
        List<Object[]> searchResults = Collections.singletonList(searchResult);
        when(vectorSearchService.search(anyString(), anyInt()))
                .thenReturn(searchResults);

        LLMResponse highConfidenceResponse = new LLMResponse(
                "일반 배송은 2-3일 소요됩니다.",
                0.85f,
                Map.of()
        );
        when(llmProvider.chat(anyString(), anyString(), anyList()))
                .thenReturn(highConfidenceResponse);

        when(inquiryMessageRepository.save(any(InquiryMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inquiryRepository.save(any(Inquiry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        boolean result = aiAnswerService.tryAutoAnswer(testInquiry);

        // then
        assertThat(result).isTrue();
        assertThat(testInquiry.getStatus()).isEqualTo(InquiryStatus.AI_ANSWERED);
        assertThat(testInquiry.getResolvedBy()).isEqualTo(InquiryResolvedBy.AI);
        assertThat(testInquiry.getAiResponse()).isEqualTo("일반 배송은 2-3일 소요됩니다.");
        assertThat(testInquiry.getAiConfidence()).isEqualTo(0.85f);

        verify(vectorSearchService).search(eq("배송은 얼마나 걸리나요?"), eq(5));
        verify(llmProvider).chat(anyString(), eq("배송은 얼마나 걸리나요?"), anyList());
        verify(inquiryMessageRepository).save(argThat(msg ->
                msg.getRole() == InquiryMessageRole.AI &&
                msg.getMessage().equals("일반 배송은 2-3일 소요됩니다.")
        ));
        verify(inquiryRepository).save(testInquiry);
    }

    @Test
    @DisplayName("tryAutoAnswer - confidence < 0.7 시 false 반환 (에스컬레이션 필요)")
    void tryAutoAnswer_whenLowConfidence_shouldReturnFalse() {
        // given
        when(llmProvider.isAvailable()).thenReturn(true);

        Object[] searchResult = new Object[]{testDoc, 0.5};
        List<Object[]> searchResults = Collections.singletonList(searchResult);
        when(vectorSearchService.search(anyString(), anyInt()))
                .thenReturn(searchResults);

        LLMResponse lowConfidenceResponse = new LLMResponse(
                "담당자 확인이 필요합니다.",
                0.5f,
                Map.of()
        );
        when(llmProvider.chat(anyString(), anyString(), anyList()))
                .thenReturn(lowConfidenceResponse);

        when(inquiryMessageRepository.save(any(InquiryMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(inquiryRepository.save(any(Inquiry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        boolean result = aiAnswerService.tryAutoAnswer(testInquiry);

        // then
        assertThat(result).isFalse();
        assertThat(testInquiry.getStatus()).isEqualTo(InquiryStatus.OPEN); // 상태 변경 없음
        assertThat(testInquiry.getAiConfidence()).isEqualTo(0.5f);

        verify(inquiryRepository).save(testInquiry);
    }

    @Test
    @DisplayName("tryAutoAnswer - 예외 발생 시 false 반환")
    void tryAutoAnswer_whenException_shouldReturnFalse() {
        // given
        when(llmProvider.isAvailable()).thenReturn(true);
        when(vectorSearchService.search(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Vector search error"));

        // when
        boolean result = aiAnswerService.tryAutoAnswer(testInquiry);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("suggestReply - LLM 사용 불가 시 기본 응답 반환")
    void suggestReply_whenLLMUnavailable_shouldReturnDefaultResponse() {
        // given
        when(llmProvider.isAvailable()).thenReturn(false);

        // when
        LLMResponse response = aiAnswerService.suggestReply(testInquiry);

        // then
        assertThat(response.content()).isEqualTo("AI 서비스를 사용할 수 없습니다.");
        assertThat(response.confidence()).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("suggestReply - 정상 동작 시 AI 응답 추천 반환")
    void suggestReply_whenAvailable_shouldReturnSuggestion() {
        // given
        when(llmProvider.isAvailable()).thenReturn(true);

        Object[] searchResult = new Object[]{testDoc, 0.9};
        List<Object[]> searchResults = Collections.singletonList(searchResult);
        when(vectorSearchService.search(anyString(), anyInt()))
                .thenReturn(searchResults);

        LLMResponse expectedResponse = new LLMResponse(
                "안녕하세요. 일반 배송은 2-3일 소요됩니다. [확인 필요: 특급 배송 가능 여부]",
                0.75f,
                Map.of()
        );
        when(llmProvider.chat(anyString(), anyString(), anyList()))
                .thenReturn(expectedResponse);

        // when
        LLMResponse response = aiAnswerService.suggestReply(testInquiry);

        // then
        assertThat(response.content()).contains("일반 배송은 2-3일 소요됩니다");
        assertThat(response.confidence()).isEqualTo(0.75f);
        verify(vectorSearchService).search(eq("배송은 얼마나 걸리나요?"), eq(5));
    }

    @Test
    @DisplayName("suggestReply - 예외 발생 시 에러 메시지 반환")
    void suggestReply_whenException_shouldReturnErrorMessage() {
        // given
        when(llmProvider.isAvailable()).thenReturn(true);
        when(vectorSearchService.search(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Search failed"));

        // when
        LLMResponse response = aiAnswerService.suggestReply(testInquiry);

        // then
        assertThat(response.content()).isEqualTo("AI 응답 추천에 실패했습니다.");
        assertThat(response.confidence()).isEqualTo(0.0f);
    }

    @Test
    @DisplayName("rewriteReply - friendly 톤으로 리라이트")
    void rewriteReply_withFriendlyTone_shouldRewrite() {
        // given
        String originalReply = "배송은 2-3일 걸립니다.";
        when(llmProvider.isAvailable()).thenReturn(true);

        LLMResponse rewrittenResponse = new LLMResponse(
                "배송은 2-3일 정도 소요돼요! 😊",
                0.9f,
                Map.of()
        );
        when(llmProvider.chat(anyString(), eq(originalReply), eq(List.of())))
                .thenReturn(rewrittenResponse);

        // when
        String result = aiAnswerService.rewriteReply(originalReply, "friendly");

        // then
        assertThat(result).isEqualTo("배송은 2-3일 정도 소요돼요! 😊");
        verify(llmProvider).chat(contains("친근하고 따뜻한 톤"), eq(originalReply), eq(List.of()));
    }

    @Test
    @DisplayName("rewriteReply - formal 톤으로 리라이트")
    void rewriteReply_withFormalTone_shouldRewrite() {
        // given
        String originalReply = "배송은 2-3일 걸려요~";
        when(llmProvider.isAvailable()).thenReturn(true);

        LLMResponse rewrittenResponse = new LLMResponse(
                "배송은 2-3일 소요됩니다.",
                0.9f,
                Map.of()
        );
        when(llmProvider.chat(anyString(), eq(originalReply), eq(List.of())))
                .thenReturn(rewrittenResponse);

        // when
        String result = aiAnswerService.rewriteReply(originalReply, "formal");

        // then
        assertThat(result).isEqualTo("배송은 2-3일 소요됩니다.");
        verify(llmProvider).chat(contains("공식적이고 격식있는 톤"), eq(originalReply), eq(List.of()));
    }

    @Test
    @DisplayName("rewriteReply - LLM 사용 불가 시 원본 반환")
    void rewriteReply_whenLLMUnavailable_shouldReturnOriginal() {
        // given
        String originalReply = "배송은 2-3일 걸립니다.";
        when(llmProvider.isAvailable()).thenReturn(false);

        // when
        String result = aiAnswerService.rewriteReply(originalReply, "friendly");

        // then
        assertThat(result).isEqualTo(originalReply);
        verifyNoMoreInteractions(llmProvider);
    }

    @Test
    @DisplayName("rewriteReply - 예외 발생 시 원본 반환")
    void rewriteReply_whenException_shouldReturnOriginal() {
        // given
        String originalReply = "배송은 2-3일 걸립니다.";
        when(llmProvider.isAvailable()).thenReturn(true);
        when(llmProvider.chat(anyString(), anyString(), anyList()))
                .thenThrow(new RuntimeException("LLM error"));

        // when
        String result = aiAnswerService.rewriteReply(originalReply, "friendly");

        // then
        assertThat(result).isEqualTo(originalReply);
    }
}
