package com.breadlab.breaddesk.attachment.service;

import com.breadlab.breaddesk.attachment.dto.AttachmentResponse;
import com.breadlab.breaddesk.attachment.entity.Attachment;
import com.breadlab.breaddesk.attachment.entity.AttachmentEntityType;
import com.breadlab.breaddesk.attachment.repository.AttachmentRepository;
import com.breadlab.breaddesk.common.exception.BusinessException;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final MemberRepository memberRepository;

    @Value("${breaddesk.upload.path:./uploads}")
    private String uploadPath;

    @Value("${breaddesk.upload.max-file-size:10485760}")
    private long maxFileSize;

    @Value("${breaddesk.upload.max-files-per-entity:5}")
    private int maxFilesPerEntity;

    @Transactional
    public AttachmentResponse uploadFile(AttachmentEntityType entityType, Long entityId, MultipartFile file, Long uploadedBy) {
        validateFile(file);
        validateEntityFileCount(entityType, entityId);

        try {
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String storedFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(storedFilename);

            Files.copy(file.getInputStream(), filePath);

            Attachment attachment = Attachment.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .filename(originalFilename)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .uploadedBy(uploadedBy != null ? memberRepository.findById(uploadedBy).orElse(null) : null)
                    .createdAt(LocalDateTime.now())
                    .build();

            return toResponse(attachmentRepository.save(attachment));

        } catch (IOException e) {
            throw new BusinessException("Failed to upload file: " + e.getMessage());
        }
    }

    public List<AttachmentResponse> getAttachments(AttachmentEntityType entityType, Long entityId) {
        return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AttachmentResponse getAttachmentById(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));
        return toResponse(attachment);
    }

    @Transactional
    public void deleteAttachment(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment not found"));

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
            attachmentRepository.delete(attachment);
        } catch (IOException e) {
            throw new BusinessException("Failed to delete file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("File is empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("File size exceeds maximum allowed size");
        }
    }

    private void validateEntityFileCount(AttachmentEntityType entityType, Long entityId) {
        long count = attachmentRepository.countByEntityTypeAndEntityId(entityType, entityId);
        if (count >= maxFilesPerEntity) {
            throw new BusinessException("Maximum number of files per entity reached");
        }
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .entityType(attachment.getEntityType())
                .entityId(attachment.getEntityId())
                .filename(attachment.getFilename())
                .filePath(attachment.getFilePath())
                .fileSize(attachment.getFileSize())
                .mimeType(attachment.getMimeType())
                .uploadedBy(attachment.getUploadedBy() != null ? attachment.getUploadedBy().getId() : null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }
}
