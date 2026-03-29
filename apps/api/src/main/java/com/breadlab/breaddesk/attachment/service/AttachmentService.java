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
import java.util.Set;
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

    // 허용된 확장자 (소문자)
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif",
            "pdf", "txt", "log",
            "csv", "xlsx", "doc", "docx",
            "zip"
    );

    // 허용된 MIME 타입
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf",
            "text/plain", "text/csv",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/zip", "application/x-zip-compressed"
    );

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

        // 파일 보안 검증
        validateFile(file);

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

    /**
     * 파일 보안 검증
     * - 확장자 화이트리스트
     * - MIME 타입 검증
     * - 파일명 새니타이징 (경로 순회 방지)
     */
    private void validateFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new BusinessException("Filename is required", "INVALID_FILENAME");
        }

        // 1. 파일명 새니타이징 (경로 순회 공격 방지)
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new BusinessException("Invalid filename: path traversal detected", "INVALID_FILENAME");
        }

        // 2. 확장자 검증
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < originalFilename.length() - 1) {
            extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("File type not allowed: " + extension, "INVALID_FILE_TYPE");
        }

        // 3. MIME 타입 검증 (Content-Type 헤더)
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new BusinessException("MIME type not allowed: " + mimeType, "INVALID_MIME_TYPE");
        }

        // TODO Phase 2: 파일 시그니처 (magic bytes) 검증 추가
        // Apache Tika 또는 Files.probeContentType() 활용
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
