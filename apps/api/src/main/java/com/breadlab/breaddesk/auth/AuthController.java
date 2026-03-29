package com.breadlab.breaddesk.auth;

import com.breadlab.breaddesk.auth.dto.LoginRequest;
import com.breadlab.breaddesk.auth.dto.RefreshTokenRequest;
import com.breadlab.breaddesk.auth.dto.TokenResponse;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Member member = memberRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        TokenResponse response = buildTokens(member);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
            throw new ResourceNotFoundException("Invalid refresh token");
        }
        String email = jwtTokenProvider.extractSubject(request.getRefreshToken());
        Member member = memberRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        TokenResponse response = buildTokens(member);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private TokenResponse buildTokens(Member member) {
        String role = member.getRole().name();
        return TokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(member.getEmail(), role))
                .refreshToken(jwtTokenProvider.generateRefreshToken(member.getEmail(), role))
                .build();
    }
}
