package com.breadlab.breaddesk.auth.service;

import com.breadlab.breaddesk.auth.dto.MemberRequest;
import com.breadlab.breaddesk.auth.dto.MemberResponse;
import com.breadlab.breaddesk.auth.entity.Member;
import com.breadlab.breaddesk.auth.repository.MemberRepository;
import com.breadlab.breaddesk.common.exception.BusinessException;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public MemberResponse createMember(MemberRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already exists", "EMAIL_DUPLICATE");
        }

        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Member.MemberRole.AGENT)
                .skills(request.getSkills())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        Member saved = memberRepository.save(member);
        log.info("Created member: {} ({})", saved.getName(), saved.getEmail());

        return MemberResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));
        return MemberResponse.from(member);
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberResponse::from);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest.Update request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member", id));

        if (request.getName() != null) {
            member.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            member.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            member.setRole(request.getRole());
        }
        if (request.getSkills() != null) {
            member.setSkills(request.getSkills());
        }
        if (request.getIsActive() != null) {
            member.setIsActive(request.getIsActive());
        }

        Member updated = memberRepository.save(member);
        log.info("Updated member: {}", updated.getId());

        return MemberResponse.from(updated);
    }

    @Transactional
    public void deleteMember(Long id) {
        if (!memberRepository.existsById(id)) {
            throw new ResourceNotFoundException("Member", id);
        }
        memberRepository.deleteById(id);
        log.info("Deleted member: {}", id);
    }
}
