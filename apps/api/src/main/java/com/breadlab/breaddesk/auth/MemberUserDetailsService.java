package com.breadlab.breaddesk.auth;

import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found"));
        return new User(
                member.getEmail(),
                member.getPasswordHash(),
                member.isActive(),
                true,
                true,
                true,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
    }
}
