package com.breadlab.breaddesk.personal.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.personal.dto.PersonalNoteRequest;
import com.breadlab.breaddesk.personal.dto.PersonalNoteResponse;
import com.breadlab.breaddesk.personal.entity.PersonalNote;
import com.breadlab.breaddesk.personal.repository.PersonalNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalNoteService {

    private final PersonalNoteRepository personalNoteRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public PersonalNoteResponse createNote(Long memberId, PersonalNoteRequest request) {
        PersonalNote note = PersonalNote.builder()
                .member(memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member not found")))
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toResponse(personalNoteRepository.save(note));
    }

    public Page<PersonalNoteResponse> getNotesByMember(Long memberId, Pageable pageable) {
        return personalNoteRepository.findByMemberId(memberId, pageable).map(this::toResponse);
    }

    public PersonalNoteResponse getNoteById(Long id) {
        PersonalNote note = personalNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        return toResponse(note);
    }

    @Transactional
    public PersonalNoteResponse updateNote(Long id, PersonalNoteRequest request) {
        PersonalNote note = personalNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        note.setContent(request.getContent());
        note.setUpdatedAt(LocalDateTime.now());

        return toResponse(personalNoteRepository.save(note));
    }

    @Transactional
    public void deleteNote(Long id) {
        personalNoteRepository.deleteById(id);
    }

    private PersonalNoteResponse toResponse(PersonalNote note) {
        return PersonalNoteResponse.builder()
                .id(note.getId())
                .memberId(note.getMember().getId())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
