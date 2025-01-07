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

    @PostMapping("/join")
    @Operation(summary = "join", description = "정보 가져와서 회원가입")
    public ResponseEntity<?> getInfo(
            @RequestParam String id,
            @RequestParam String password
    ) {
        MemberDto memberDto = memberService.getInfo(id, password);
        return ResponseEntity.ok().body(memberDto);
    }

    @PostMapping("/login")
    @Operation(summary = "login", description = "로그인")
    public void login() {
    }

}
