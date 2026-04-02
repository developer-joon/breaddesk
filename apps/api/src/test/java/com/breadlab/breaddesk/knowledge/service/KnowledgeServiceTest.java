package com.breadlab.breaddesk.knowledge.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import com.breadlab.breaddesk.knowledge.entity.KnowledgeDocumentEntity;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeConnectorRepository;
import com.breadlab.breaddesk.knowledge.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("KnowledgeService 테스트")
class KnowledgeServiceTest {

    @Mock
    private KnowledgeDocumentRepository documentRepository;

    @Mock
    private KnowledgeConnectorRepository connectorRepository;

    @InjectMocks
    private KnowledgeService knowledgeService;

    private KnowledgeDocumentEntity testDoc;
    private KnowledgeConnectorEntity testConnector;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testDoc = new KnowledgeDocumentEntity();
        testDoc.setId(1L);
        testDoc.setTitle("배송 안내");
        testDoc.setContent("배송은 2-3일 소요됩니다");
        testDoc.setSource("manual");

        testConnector = new KnowledgeConnectorEntity();
        testConnector.setId(1L);
        testConnector.setSourceType("notion");
        testConnector.setConfig("{}");
        testConnector.setSyncIntervalMin(60);
        testConnector.setActive(true);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("getDocuments - 키워드 없이 전체 조회")
    void getDocuments_withoutKeyword_shouldReturnAll() {
        // given
        List<KnowledgeDocumentEntity> docs = new ArrayList<>();
        docs.add(testDoc);
        Page<KnowledgeDocumentEntity> page = new PageImpl<>(docs);
        when(documentRepository.findAll(any(Pageable.class))).thenReturn(page);

        // when
        Page<KnowledgeDocumentEntity> result = knowledgeService.getDocuments(null, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        verify(documentRepository).findAll(pageable);
        verify(documentRepository, never()).findByTitleContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("getDocuments - 키워드로 검색")
    void getDocuments_withKeyword_shouldSearch() {
        // given
        List<KnowledgeDocumentEntity> docs = new ArrayList<>();
        docs.add(testDoc);
        Page<KnowledgeDocumentEntity> page = new PageImpl<>(docs);
        when(documentRepository.findByTitleContainingIgnoreCase("배송", pageable)).thenReturn(page);

        // when
        Page<KnowledgeDocumentEntity> result = knowledgeService.getDocuments("배송", pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("배송 안내");
        verify(documentRepository).findByTitleContainingIgnoreCase("배송", pageable);
    }

    @Test
    @DisplayName("getDocument - 문서 조회 성공")
    void getDocument_shouldReturnDocument() {
        // given
        when(documentRepository.findById(1L)).thenReturn(Optional.of(testDoc));

        // when
        KnowledgeDocumentEntity result = knowledgeService.getDocument(1L);

        // then
        assertThat(result).isEqualTo(testDoc);
        verify(documentRepository).findById(1L);
    }

    @Test
    @DisplayName("getDocument - 문서 없을 시 예외 발생")
    void getDocument_whenNotFound_shouldThrowException() {
        // given
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> knowledgeService.getDocument(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("문서를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("getConnectors - 모든 커넥터 조회")
    void getConnectors_shouldReturnAll() {
        // given
        List<KnowledgeConnectorEntity> connectors = new ArrayList<>();
        connectors.add(testConnector);
        when(connectorRepository.findAll()).thenReturn(connectors);

        // when
        List<KnowledgeConnectorEntity> result = knowledgeService.getConnectors();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSourceType()).isEqualTo("notion");
        verify(connectorRepository).findAll();
    }

    @Test
    @DisplayName("createConnector - 커넥터 생성 성공")
    void createConnector_shouldCreateSuccessfully() {
        // given
        when(connectorRepository.save(any(KnowledgeConnectorEntity.class))).thenAnswer(invocation -> {
            KnowledgeConnectorEntity c = invocation.getArgument(0);
            c.setId(1L);
            return c;
        });

        // when
        KnowledgeConnectorEntity result = knowledgeService.createConnector(testConnector);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        verify(connectorRepository).save(testConnector);
    }

    @Test
    @DisplayName("updateConnector - 커넥터 수정 성공")
    void updateConnector_shouldUpdateSuccessfully() {
        // given
        when(connectorRepository.findById(1L)).thenReturn(Optional.of(testConnector));
        when(connectorRepository.save(any(KnowledgeConnectorEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KnowledgeConnectorEntity update = new KnowledgeConnectorEntity();
        update.setSourceType("confluence");
        update.setConfig("{\"apiKey\":\"new\"}");
        update.setSyncIntervalMin(30);
        update.setActive(false);

        // when
        KnowledgeConnectorEntity result = knowledgeService.updateConnector(1L, update);

        // then
        assertThat(result.getSourceType()).isEqualTo("confluence");
        assertThat(result.getSyncIntervalMin()).isEqualTo(30);
        assertThat(result.isActive()).isFalse();
        verify(connectorRepository).save(testConnector);
    }

    @Test
    @DisplayName("updateConnector - 커넥터 없을 시 예외 발생")
    void updateConnector_whenNotFound_shouldThrowException() {
        // given
        when(connectorRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> knowledgeService.updateConnector(999L, testConnector))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("커넥터를 찾을 수 없습니다");
    }
}
