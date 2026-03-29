package com.breadlab.breaddesk.auth.controller;

import com.breadlab.breaddesk.auth.dto.MemberRequest;
import com.breadlab.breaddesk.auth.dto.MemberResponse;
import com.breadlab.breaddesk.auth.service.MemberService;
import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MemberResponse> createMember(@Valid @RequestBody MemberRequest request) {
        return ApiResponse.success("Member created successfully", memberService.createMember(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<MemberResponse> getMember(@PathVariable Long id) {
        return ApiResponse.success(memberService.getMember(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<MemberResponse>> getAllMembers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<MemberResponse> members = memberService.getAllMembers(pageable);
        return ApiResponse.success(PageResponse.of(members));
    }

    @PatchMapping("/{id}")
    public ApiResponse<MemberResponse> updateMember(
            @PathVariable Long id,
            @RequestBody MemberRequest.Update request) {
        return ApiResponse.success("Member updated successfully", memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
    }
}
