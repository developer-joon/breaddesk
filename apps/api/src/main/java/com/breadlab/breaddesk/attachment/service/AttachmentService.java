package com.breadlab.breaddesk.attachment.service;

import com.breadlab.breaddesk.attachment.dto.AttachmentResponse;
import com.breadlab.breaddesk.attachment.entity.Attachment;
import com.breadlab.breaddesk.attachment.repository.AttachmentRepository;
import com.breadlab.breaddesk.common.exception.BusinessException;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Value("${breaddesk.upload.path:./uploads}")
    private String uploadPath;

    @Value("${breaddesk.upload.max-file-size:10485760}")
    private long maxFileSize;

    @Value("${breaddesk.upload.max-files-per-entity:5}")
    private int maxFilesPerEntity;

    @Transactional
    public AttachmentResponse uploadFile(MultipartFile file, Attachment.EntityType entityType, Long entityId) {
        // 파일 검증
        if (file.isEmpty()) {
            throw new BusinessException("File is empty", "EMPTY_FILE");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("File size exceeds limit: " + maxFileSize + " bytes", "FILE_TOO_LARGE");
        }

        long currentCount = attachmentRepository.countByEntityTypeAndEntityId(entityType, entityId);
        if (currentCount >= maxFilesPerEntity) {
            throw new BusinessException("Maximum number of files exceeded: " + maxFilesPerEntity, "TOO_MANY_FILES");
        }

        // 파일 저장
        String storedPath = storeFile(file);

        Attachment attachment = Attachment.builder()
                .entityType(entityType)
                .entityId(entityId)
                .filename(file.getOriginalFilename())
                .filePath(storedPath)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .build();

        Attachment saved = attachmentRepository.save(attachment);
        log.info("Uploaded file: {} for {} {}", saved.getFilename(), entityType, entityId);

        return AttachmentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Resource downloadFile(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", id));

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("File not found or not readable", "FILE_NOT_FOUND");
            }
        } catch (MalformedURLException e) {
            throw new BusinessException("File path is invalid", "INVALID_PATH");
        }
    }

    @Transactional(readOnly = true)
    public Attachment getAttachmentMetadata(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", id));
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByEntity(Attachment.EntityType entityType, Long entityId) {
        return attachmentRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(AttachmentResponse::from)
                .toList();
    }

    @Transactional
    public void deleteFile(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", id));

        // 파일시스템에서 삭제
        try {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        } catch (IOException e) {
            log.warn("Failed to delete file from filesystem: {}", attachment.getFilePath(), e);
        }

        // DB에서 삭제
        attachmentRepository.delete(attachment);
        log.info("Deleted attachment: {}", id);
    }

    private String storeFile(MultipartFile file) {
        try {
            // 날짜별 디렉토리 생성 (예: uploads/2026/03/29/)
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            Path uploadDir = Paths.get(uploadPath, datePath);
            Files.createDirectories(uploadDir);

            // 고유 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String storedFilename = UUID.randomUUID().toString() + extension;

            Path targetPath = uploadDir.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return targetPath.toString();
        } catch (IOException e) {
            throw new BusinessException("Failed to store file", "FILE_STORE_ERROR");
        }
    }
}
