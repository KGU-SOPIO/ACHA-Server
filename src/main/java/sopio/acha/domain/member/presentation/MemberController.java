package sopio.acha.domain.member.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;

import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.presentation.dto.MemberDto;

@RestController
@Tag(name = "Member", description = "회원관리")
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/join")
    @Operation(summary = "join", description = "정보 가져와서 회원가입")
    public ResponseEntity<Void> join(
        @RequestParam String id,
        @RequestParam String password
    ) {
        memberService.saveMemberInfo(id, password);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @Operation(summary = "login", description = "로그인")
    public void login() {
    }

}
