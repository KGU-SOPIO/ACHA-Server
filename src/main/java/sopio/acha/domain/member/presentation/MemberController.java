package sopio.acha.domain.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.presentation.request.MemberBasicInformationRequest;
import sopio.acha.domain.member.presentation.response.MemberSummaryResponse;
import sopio.acha.domain.member.presentation.response.MemberTokenResponse;

@RestController
@Tag(name = "Member", description = "회원관리")
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/info")
    @Operation(summary = "get basic member information from extractor", description = "회원가입 시 추출기에서 정보 불러오기")
    public ResponseEntity<MemberSummaryResponse> getMemberInformationFromExtractor() {
        MemberSummaryResponse response = memberService.getMemberInformationFromExtractor();
        return ResponseEntity.ok().body(response);
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

    @PatchMapping("/update")
    @Operation(summary = "update basic member information", description = "회원 기본 정보 업데이트")
    public ResponseEntity<Void> updateMemberInformation(
        @RequestBody MemberBasicInformationRequest request
    ) {
        memberService.updateBasicMemberInformation(request);
        return ResponseEntity.ok().build();
    }

}
