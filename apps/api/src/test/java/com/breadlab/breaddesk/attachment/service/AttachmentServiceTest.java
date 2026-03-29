package com.breadlab.breaddesk.attachment.service;

import com.breadlab.breaddesk.attachment.dto.AttachmentResponse;
import com.breadlab.breaddesk.attachment.entity.Attachment;
import com.breadlab.breaddesk.attachment.entity.AttachmentEntityType;
import com.breadlab.breaddesk.attachment.repository.AttachmentRepository;
import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.common.exception.BusinessException;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private AttachmentService attachmentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(attachmentService, "maxFileSize", 10485760L);
        ReflectionTestUtils.setField(attachmentService, "maxFilesPerEntity", 5);
    }

    @Test
    @DisplayName("should_uploadFile_when_validFile")
    void should_uploadFile_when_validFile() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "PDF content".getBytes());

        given(attachmentRepository.countByEntityTypeAndEntityId(AttachmentEntityType.TASK, 1L)).willReturn(0L);
        given(attachmentRepository.save(any(Attachment.class))).willAnswer(inv -> {
            Attachment saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        AttachmentResponse response = attachmentService.uploadFile(
                AttachmentEntityType.TASK, 1L, file, null);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getFilename()).isEqualTo("test.pdf");
        assertThat(response.getMimeType()).isEqualTo("application/pdf");
    }

    @Test
    @DisplayName("should_throwException_when_fileIsEmpty")
    void should_throwException_when_fileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() ->
                attachmentService.uploadFile(AttachmentEntityType.TASK, 1L, file, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("File is empty");
    }

    @Test
    @DisplayName("should_throwException_when_fileTooLarge")
    void should_throwException_when_fileTooLarge() {
        ReflectionTestUtils.setField(attachmentService, "maxFileSize", 10L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.pdf", "application/pdf", "large content here!!".getBytes());

        assertThatThrownBy(() ->
                attachmentService.uploadFile(AttachmentEntityType.TASK, 1L, file, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("File size exceeds");
    }

    @Test
    @DisplayName("should_throwException_when_maxFilesReached")
    void should_throwException_when_maxFilesReached() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes());
        given(attachmentRepository.countByEntityTypeAndEntityId(AttachmentEntityType.TASK, 1L)).willReturn(5L);

        assertThatThrownBy(() ->
                attachmentService.uploadFile(AttachmentEntityType.TASK, 1L, file, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Maximum number of files");
    }

    @Test
    @DisplayName("should_getAttachments_when_called")
    void should_getAttachments_when_called() {
        // Given
        Attachment attachment = TestDataFactory.createAttachment();
        attachment.setId(1L);
        given(attachmentRepository.findByEntityTypeAndEntityId(AttachmentEntityType.TASK, 1L))
                .willReturn(List.of(attachment));

        // When
        List<AttachmentResponse> responses = attachmentService.getAttachments(AttachmentEntityType.TASK, 1L);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFilename()).isEqualTo("test.pdf");
    }

    @Test
    @DisplayName("should_getAttachmentById_when_exists")
    void should_getAttachmentById_when_exists() {
        Attachment attachment = TestDataFactory.createAttachment();
        attachment.setId(1L);
        given(attachmentRepository.findById(1L)).willReturn(Optional.of(attachment));

        AttachmentResponse response = attachmentService.getAttachmentById(1L);

        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("should_throwException_when_attachmentNotFound")
    void should_throwException_when_attachmentNotFound() {
        given(attachmentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.getAttachmentById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_deleteAttachment_when_exists")
    void should_deleteAttachment_when_exists() throws Exception {
        // Given - create a real temp file
        Path testFile = tempDir.resolve("to-delete.pdf");
        java.nio.file.Files.writeString(testFile, "content");

        Attachment attachment = TestDataFactory.createAttachment();
        attachment.setId(1L);
        attachment.setFilePath(testFile.toString());
        given(attachmentRepository.findById(1L)).willReturn(Optional.of(attachment));

        // When
        attachmentService.deleteAttachment(1L);

        // Then
        verify(attachmentRepository).delete(attachment);
        assertThat(java.nio.file.Files.exists(testFile)).isFalse();
    }
}
