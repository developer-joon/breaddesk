package com.breadlab.breaddesk.personal.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.personal.dto.PersonalNoteRequest;
import com.breadlab.breaddesk.personal.dto.PersonalNoteResponse;
import com.breadlab.breaddesk.personal.entity.PersonalNote;
import com.breadlab.breaddesk.personal.repository.PersonalNoteRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PersonalNoteServiceTest {

    @Mock
    private PersonalNoteRepository personalNoteRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private PersonalNoteService personalNoteService;

    private Member member;
    private PersonalNote note;

    @BeforeEach
    void setUp() {
        member = TestDataFactory.createMember();
        member.setId(1L);
        note = TestDataFactory.createPersonalNote(member);
        note.setId(1L);
    }

    @Test
    @DisplayName("should_createNote_when_memberExists")
    void should_createNote_when_memberExists() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(personalNoteRepository.save(any(PersonalNote.class))).willAnswer(inv -> {
            PersonalNote saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        PersonalNoteRequest request = TestDataFactory.createPersonalNoteRequest();

        // When
        PersonalNoteResponse response = personalNoteService.createNote(1L, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEqualTo("New note content");
    }

    @Test
    @DisplayName("should_throwException_when_memberNotFoundForNote")
    void should_throwException_when_memberNotFoundForNote() {
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> personalNoteService.createNote(999L, TestDataFactory.createPersonalNoteRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("should_getNotesByMember_when_called")
    void should_getNotesByMember_when_called() {
        Pageable pageable = PageRequest.of(0, 10);
        given(personalNoteRepository.findByMemberId(1L, pageable))
                .willReturn(new PageImpl<>(List.of(note)));

        Page<PersonalNoteResponse> page = personalNoteService.getNotesByMember(1L, pageable);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("should_getNoteById_when_exists")
    void should_getNoteById_when_exists() {
        given(personalNoteRepository.findById(1L)).willReturn(Optional.of(note));

        PersonalNoteResponse response = personalNoteService.getNoteById(1L);

        assertThat(response.getContent()).isEqualTo("My personal note");
    }

    @Test
    @DisplayName("should_updateNote_when_exists")
    void should_updateNote_when_exists() {
        // Given
        given(personalNoteRepository.findById(1L)).willReturn(Optional.of(note));
        given(personalNoteRepository.save(any(PersonalNote.class))).willReturn(note);

        PersonalNoteRequest request = TestDataFactory.createPersonalNoteRequest();
        request.setContent("Updated note");

        // When
        PersonalNoteResponse response = personalNoteService.updateNote(1L, request);

        // Then
        assertThat(response.getContent()).isEqualTo("Updated note");
    }

    @Test
    @DisplayName("should_deleteNote_when_called")
    void should_deleteNote_when_called() {
        personalNoteService.deleteNote(1L);

        verify(personalNoteRepository).deleteById(1L);
    }
}
