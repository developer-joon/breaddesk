package com.breadlab.breaddesk.member.service;

import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.dto.MemberRequest;
import com.breadlab.breaddesk.member.dto.MemberResponse;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberResponse create(MemberRequest request) {
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(encodePassword(request.getPassword()))
                .role(request.getRole())
                .skills(request.getSkills())
                .active(request.getActive() == null || request.getActive())
                .build();
        return toResponse(memberRepository.save(member));
    }

    public MemberResponse update(Long id, MemberRequest request) {
        Member member = getEntity(id);
        member.setName(request.getName());
        member.setEmail(request.getEmail());
        member.setRole(request.getRole());
        member.setSkills(request.getSkills());
        if (request.getActive() != null) {
            member.setActive(request.getActive());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            member.setPasswordHash(encodePassword(request.getPassword()));
        }
        return toResponse(member);
    }

    @Transactional(readOnly = true)
    public MemberResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> list() {
        return memberRepository.findAll().stream().map(this::toResponse).toList();
    }

    public void delete(Long id) {
        memberRepository.delete(getEntity(id));
    }

    private Member getEntity(Long id) {
        return memberRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    private String encodePassword(String rawPassword) {
        return rawPassword != null ? passwordEncoder.encode(rawPassword) : null;
    }

    private MemberResponse toResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .skills(member.getSkills())
                .active(member.isActive())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
