package sopio.acha.domain.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.presentation.response.MemberTokenResponse;

@RestController
@Tag(name = "Member", description = "회원관리")
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/join")
    @Operation(summary = "join", description = "정보 가져와서 회원가입")
    public ResponseEntity<Void> saveMember(
        @RequestParam String studentId,
        @RequestParam String password
    ) {
        memberService.saveMemberInfo(studentId, password);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "login", description = "로그인")
    public ResponseEntity<MemberTokenResponse> authenticateMemberAndGenerateToken(
        @RequestParam String studentId,
        @RequestParam String password
    ) {
        MemberTokenResponse response = memberService.authenticateMemberAndGenerateToken(studentId, password);
        return ResponseEntity.ok().body(response);
    }
}
