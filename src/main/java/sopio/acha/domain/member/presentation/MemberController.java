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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.presentation.request.MemberBasicInformationRequest;
import sopio.acha.domain.member.presentation.request.MemberLoginRequest;
import sopio.acha.domain.member.presentation.response.MemberBasicInformationResponse;
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
    public ResponseEntity<MemberSummaryResponse> getMemberInformationFromExtractor(
        @RequestParam String studentId,
        @RequestParam String password
    ) {
        MemberSummaryResponse response = memberService.getMemberInformationFromExtractor(studentId, password);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    @Operation(summary = "get basic member information in home page", description = "회원 기본 정보 불러오기")
    public ResponseEntity<MemberBasicInformationResponse> getMemberInformation(
        @CurrentMember Member currentMember
    ) {
        MemberBasicInformationResponse response = memberService.getMemberBasicInformation(currentMember);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "로그인 API", description = "로그인")
    public ResponseEntity<MemberTokenResponse> authenticateMemberAndGenerateToken(
        @RequestParam String studentId,
        @RequestParam String password
    ) {
        MemberTokenResponse response = memberService.authenticateMemberAndGenerateToken(studentId, password);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/join")
    @Operation(summary = "회원가입 API", description = "로그인 시도 이후 비회원 인 경우 회원 가입 API 호출")
    public ResponseEntity<MemberTokenResponse> joinMember(
        @RequestBody MemberLoginRequest request
    ) {
        memberService.joinMember(request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/update")
    @Operation(summary = "update basic member information", description = "회원 기본 정보 업데이트")
    public ResponseEntity<Void> updateMemberInformation(
        @CurrentMember Member currentMember,
        @RequestBody MemberBasicInformationRequest request
    ) {
        memberService.updateBasicMemberInformation(currentMember, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @Operation(summary = "아차 로그인 API", description = "아차 서비스 회원 여부를 판단하고 로그인 합니다.")
    public ResponseEntity<MemberTokenResponse> validateIsAchaMemberAndLogin (
        @RequestBody MemberLoginRequest request
    ) {
        MemberTokenResponse response = memberService.validateIsAchaMemberAndLogin(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/data")
    @Operation(summary = "신규 회원 LMS 학생 정보 추출 API", description = "신규 회원 가입 시에 LMS에서 학생 정보를 추출합니다.")
    public ResponseEntity<MemberSummaryResponse> getNewMemberDataFromLMS(
        @RequestBody MemberLoginRequest request
    ) {
        MemberSummaryResponse response = memberService.getNewMemberDataFromLMS(request);
        return ResponseEntity.ok().body(response);
    }
}
