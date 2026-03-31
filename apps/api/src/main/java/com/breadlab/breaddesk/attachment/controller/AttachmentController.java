package com.breadlab.breaddesk.attachment.controller;

import com.breadlab.breaddesk.attachment.dto.AttachmentResponse;
import com.breadlab.breaddesk.auth.AuthUtils;
import com.breadlab.breaddesk.attachment.entity.AttachmentEntityType;
import com.breadlab.breaddesk.attachment.service.AttachmentService;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<AttachmentResponse>> uploadFile(
            @RequestParam AttachmentEntityType entityType,
            @RequestParam Long entityId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long uploadedBy = authUtils.getMemberId(userDetails);
        AttachmentResponse response = attachmentService.uploadFile(entityType, entityId, file, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> getAttachments(
            @RequestParam AttachmentEntityType entityType,
            @RequestParam Long entityId) {
        List<AttachmentResponse> responses = attachmentService.getAttachments(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttachmentResponse>> getAttachmentById(@PathVariable Long id) {
        AttachmentResponse response = attachmentService.getAttachmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        AttachmentResponse attachment = attachmentService.getAttachmentById(id);
        Resource resource = new FileSystemResource(Paths.get(attachment.getFilePath()));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
