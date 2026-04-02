package com.breadlab.breaddesk.csat;

import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CsatService 테스트")
class CsatServiceTest {

    @Mock
    private CsatRepository csatRepository;

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private CsatService csatService;

    private Inquiry testInquiry;
    private CsatSurvey testSurvey;

    @BeforeEach
    void setUp() {
        testInquiry = new Inquiry();
        testInquiry.setId(1L);
        testInquiry.setMessage("테스트 문의");

        testSurvey = new CsatSurvey();
        testSurvey.setId(1L);
        testSurvey.setInquiryId(1L);
        testSurvey.setToken("test-token-123");
        testSurvey.setSentAt(LocalDateTime.now());
        testSurvey.setResponded(false);
    }

    @Test
    @DisplayName("createSurvey - 새 설문 생성 성공")
    void createSurvey_shouldCreateSuccessfully() {
        // given
        when(csatRepository.existsByInquiryId(1L)).thenReturn(false);
        when(inquiryRepository.findById(1L)).thenReturn(Optional.of(testInquiry));
        when(csatRepository.save(any(CsatSurvey.class))).thenAnswer(invocation -> {
            CsatSurvey survey = invocation.getArgument(0);
            survey.setId(1L);
            return survey;
        });

        // when
        CsatSurvey result = csatService.createSurvey(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getInquiryId()).isEqualTo(1L);
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getResponded()).isFalse();
        verify(csatRepository).save(any(CsatSurvey.class));
    }

    @Test
    @DisplayName("createSurvey - 이미 존재하는 설문 반환")
    void createSurvey_whenExists_shouldReturnExisting() {
        // given
        when(csatRepository.existsByInquiryId(1L)).thenReturn(true);
        when(csatRepository.findByInquiryId(1L)).thenReturn(Optional.of(testSurvey));

        // when
        CsatSurvey result = csatService.createSurvey(1L);

        // then
        assertThat(result).isEqualTo(testSurvey);
        verify(csatRepository, never()).save(any(CsatSurvey.class));
    }

    @Test
    @DisplayName("createSurvey - 문의 없을 시 예외 발생")
    void createSurvey_whenInquiryNotFound_shouldThrowException() {
        // given
        when(csatRepository.existsByInquiryId(999L)).thenReturn(false);
        when(inquiryRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> csatService.createSurvey(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Inquiry not found");
    }

    @Test
    @DisplayName("recordResponse - 응답 기록 성공")
    void recordResponse_shouldRecordSuccessfully() {
        // given
        when(csatRepository.findByToken("test-token-123")).thenReturn(Optional.of(testSurvey));
        when(csatRepository.save(any(CsatSurvey.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CsatSurvey result = csatService.recordResponse("test-token-123", 5, "매우 만족합니다");

        // then
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getFeedback()).isEqualTo("매우 만족합니다");
        assertThat(result.getResponded()).isTrue();
        assertThat(result.getRespondedAt()).isNotNull();
        verify(csatRepository).save(testSurvey);
    }

    @Test
    @DisplayName("recordResponse - 이미 응답한 설문 시 예외 발생")
    void recordResponse_whenAlreadyResponded_shouldThrowException() {
        // given
        testSurvey.setResponded(true);
        when(csatRepository.findByToken("test-token-123")).thenReturn(Optional.of(testSurvey));

        // when & then
        assertThatThrownBy(() -> csatService.recordResponse("test-token-123", 5, "좋음"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Survey already completed");
        verify(csatRepository, never()).save(any(CsatSurvey.class));
    }

    @Test
    @DisplayName("recordResponse - 잘못된 토큰 시 예외 발생")
    void recordResponse_whenInvalidToken_shouldThrowException() {
        // given
        when(csatRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> csatService.recordResponse("invalid-token", 5, "좋음"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Survey not found");
    }

    @Test
    @DisplayName("getSurveyByToken - 설문 조회 성공")
    void getSurveyByToken_shouldReturnSurvey() {
        // given
        when(csatRepository.findByToken("test-token-123")).thenReturn(Optional.of(testSurvey));

        // when
        CsatSurvey result = csatService.getSurveyByToken("test-token-123");

        // then
        assertThat(result).isEqualTo(testSurvey);
        verify(csatRepository).findByToken("test-token-123");
    }

    @Test
    @DisplayName("getSurveyByToken - 설문 없을 시 예외 발생")
    void getSurveyByToken_whenNotFound_shouldThrowException() {
        // given
        when(csatRepository.findByToken("invalid-token")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> csatService.getSurveyByToken("invalid-token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Survey not found");
    }

    @Test
    @DisplayName("getAverageRating - 평균 평점 계산")
    void getAverageRating_shouldCalculateAverage() {
        // given
        CsatSurvey survey1 = new CsatSurvey();
        survey1.setResponded(true);
        survey1.setRating(5);

        CsatSurvey survey2 = new CsatSurvey();
        survey2.setResponded(true);
        survey2.setRating(4);

        CsatSurvey survey3 = new CsatSurvey();
        survey3.setResponded(false);

        when(csatRepository.findAll()).thenReturn(Arrays.asList(survey1, survey2, survey3));

        // when
        double average = csatService.getAverageRating();

        // then
        assertThat(average).isEqualTo(4.5);
    }

    @Test
    @DisplayName("getAverageRating - 응답 없을 시 0 반환")
    void getAverageRating_whenNoResponses_shouldReturnZero() {
        // given
        when(csatRepository.findAll()).thenReturn(Arrays.asList());

        // when
        double average = csatService.getAverageRating();

        // then
        assertThat(average).isEqualTo(0.0);
    }
}
