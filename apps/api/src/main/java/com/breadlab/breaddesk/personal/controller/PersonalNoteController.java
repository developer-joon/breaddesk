package com.breadlab.breaddesk.personal.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.personal.dto.PersonalNoteRequest;
import com.breadlab.breaddesk.personal.dto.PersonalNoteResponse;
import com.breadlab.breaddesk.personal.service.PersonalNoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/personal-notes")
@RequiredArgsConstructor
public class PersonalNoteController {

    private final PersonalNoteService personalNoteService;

    @PostMapping
    public ResponseEntity<ApiResponse<PersonalNoteResponse>> createNote(
            @Valid @RequestBody PersonalNoteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        PersonalNoteResponse response = personalNoteService.createNote(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PersonalNoteResponse>>> getMyNotes(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable) {
        Long memberId = Long.parseLong(userDetails.getUsername());
        Page<PersonalNoteResponse> responses = personalNoteService.getNotesByMember(memberId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonalNoteResponse>> getNoteById(@PathVariable Long id) {
        PersonalNoteResponse response = personalNoteService.getNoteById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PersonalNoteResponse>> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody PersonalNoteRequest request) {
        PersonalNoteResponse response = personalNoteService.updateNote(id, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable Long id) {
        personalNoteService.deleteNote(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
