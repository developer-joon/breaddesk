package com.breadlab.breaddesk.attachment.controller;

import com.breadlab.breaddesk.attachment.dto.AttachmentResponse;
import com.breadlab.breaddesk.attachment.entity.Attachment;
import com.breadlab.breaddesk.attachment.service.AttachmentService;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AttachmentResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") Attachment.EntityType entityType,
            @RequestParam("entityId") Long entityId) {
        return ApiResponse.success("File uploaded successfully",
                attachmentService.uploadFile(file, entityType, entityId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Resource resource = attachmentService.downloadFile(id);
        Attachment metadata = attachmentService.getAttachmentMetadata(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        metadata.getMimeType() != null ? metadata.getMimeType() : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping
    public ApiResponse<List<AttachmentResponse>> getAttachmentsByEntity(
            @RequestParam Attachment.EntityType entityType,
            @RequestParam Long entityId) {
        return ApiResponse.success(attachmentService.getAttachmentsByEntity(entityType, entityId));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFile(@PathVariable Long id) {
        attachmentService.deleteFile(id);
    }
}
