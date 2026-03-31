package com.breadlab.breaddesk.auth;

import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    private final MemberRepository memberRepository;

    /**
     * Get the member ID from the authenticated user.
     * The JWT subject is the member's email address.
     */
    public Long getMemberId(UserDetails userDetails) {
        return memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Member not found: " + userDetails.getUsername()))
                .getId();
    }

    /**
     * Get the Member entity from the authenticated user.
     */
    public Member getMember(UserDetails userDetails) {
        return memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Member not found: " + userDetails.getUsername()));
    }
}
