package com.breadlab.breaddesk.auth;

import com.breadlab.breaddesk.auth.dto.LoginRequest;
import com.breadlab.breaddesk.auth.dto.PasswordChangeRequest;
import com.breadlab.breaddesk.auth.dto.RefreshTokenRequest;
import com.breadlab.breaddesk.auth.dto.RegisterRequest;
import com.breadlab.breaddesk.auth.dto.TokenResponse;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.member.entity.Member;
import com.breadlab.breaddesk.member.entity.MemberRole;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.breadlab.breaddesk.member.service.MemberService;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Authentication", description = "인증 및 토큰 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final AuthUtils authUtils;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<TokenResponse>> register(@Valid @RequestBody RegisterRequest request) {
        // 이메일 중복 체크
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 새 멤버 생성
        Member member = Member.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(MemberRole.AGENT)
                .active(true)
                .build();
        
        member = memberRepository.save(member);
        
        // 자동 로그인 (토큰 발급)
        TokenResponse response = buildTokens(member);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
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

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.")
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

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호를 검증하고 새로운 비밀번호로 변경합니다.")
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest request) {
        Long memberId = authUtils.getMemberId(userDetails);
        memberService.changePassword(memberId, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    private TokenResponse buildTokens(Member member) {
        String role = member.getRole().name();
        return TokenResponse.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(member.getEmail(), role))
                .refreshToken(jwtTokenProvider.generateRefreshToken(member.getEmail(), role))
                .build();
    }
}
