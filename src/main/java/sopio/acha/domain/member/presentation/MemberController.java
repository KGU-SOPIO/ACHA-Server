package sopio.acha.domain.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.presentation.dto.MemberDto;

@RestController
@Tag(name = "Member", description = "회원관리")
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/login")
    @Operation(summary = "login", description = "로그인")
    public ResponseEntity<?> login(
            @RequestParam String id,
            @RequestParam String password
    ) {
        try {
            MemberDto memberDto = memberService.login(id, password);
            return ResponseEntity.ok().body(memberDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join")
    @Operation(summary = "join", description = "회원가입")
    public ResponseEntity<?> join(
            @RequestBody MemberDto memberDto
    ) {
        try {
            MemberDto responseMemberDto = memberService.join(memberDto);
            return ResponseEntity.ok(responseMemberDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

}
