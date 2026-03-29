package com.breadlab.breaddesk.member.service;

import com.breadlab.breaddesk.common.TestDataFactory;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.dto.MemberRequest;
import com.breadlab.breaddesk.member.dto.MemberResponse;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private MemberService memberService;

    private Member member;
    private MemberRequest request;

    @BeforeEach
    void setUp() {
        member = TestDataFactory.createMember();
        member.setId(1L);
        request = TestDataFactory.createMemberRequest();
    }

    @Test
    @DisplayName("should_createMember_when_validRequest")
    void should_createMember_when_validRequest() {
        // Given
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        MemberResponse response = memberService.create(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getEmail()).isEqualTo(request.getEmail());
        assertThat(response.getRole()).isEqualTo(MemberRole.AGENT);
        assertThat(response.isActive()).isTrue();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("should_getMember_when_existingId")
    void should_getMember_when_existingId() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // When
        MemberResponse response = memberService.get(1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo(member.getName());
    }

    @Test
    @DisplayName("should_throwException_when_memberNotFound")
    void should_throwException_when_memberNotFound() {
        // Given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> memberService.get(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Member not found");
    }

    @Test
    @DisplayName("should_updateMember_when_validRequest")
    void should_updateMember_when_validRequest() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        request.setName("Updated Name");
        request.setPassword(null);

        // When
        MemberResponse response = memberService.update(1L, request);

        // Then
        assertThat(response.getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName("should_updatePassword_when_passwordProvided")
    void should_updatePassword_when_passwordProvided() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(passwordEncoder.encode("newPassword")).willReturn("encodedNewPassword");

        request.setPassword("newPassword");

        // When
        memberService.update(1L, request);

        // Then
        assertThat(member.getPasswordHash()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("should_listAllMembers_when_called")
    void should_listAllMembers_when_called() {
        // Given
        Member member2 = TestDataFactory.createMember("Agent 2", "agent2@test.com");
        member2.setId(2L);
        given(memberRepository.findAll()).willReturn(List.of(member, member2));

        // When
        List<MemberResponse> responses = memberService.list();

        // Then
        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("should_deleteMember_when_existingId")
    void should_deleteMember_when_existingId() {
        // Given
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        // When
        memberService.delete(1L);

        // Then
        verify(memberRepository).delete(member);
    }
}
