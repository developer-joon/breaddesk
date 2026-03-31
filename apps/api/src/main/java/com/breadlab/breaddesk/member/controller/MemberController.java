package com.breadlab.breaddesk.member.controller;

import com.breadlab.breaddesk.common.dto.ApiResponse;
import com.breadlab.breaddesk.member.dto.MemberRequest;
import com.breadlab.breaddesk.member.dto.MemberResponse;
import com.breadlab.breaddesk.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Member", description = "멤버 관리 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(memberService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> create(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> update(
            @PathVariable Long id, @Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(ApiResponse.success(memberService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        memberService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
