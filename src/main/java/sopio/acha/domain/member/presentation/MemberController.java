package sopio.acha.domain.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.annotation.CurrentMember;
import sopio.acha.domain.member.application.MemberService;
import sopio.acha.domain.member.domain.AccessToken;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.presentation.request.MemberLoginRequest;
import sopio.acha.domain.member.presentation.request.MemberLogoutRequest;
import sopio.acha.domain.member.presentation.request.MemberRequest;
import sopio.acha.domain.member.presentation.request.MemberSaveRequest;
import sopio.acha.domain.member.presentation.request.MemberSignOutRequest;
import sopio.acha.domain.member.presentation.request.RefreshTokenRequest;
import sopio.acha.domain.member.presentation.response.AccessTokenResponse;
import sopio.acha.domain.member.presentation.response.MemberBasicInformationResponse;
import sopio.acha.domain.member.presentation.response.MemberSummaryResponse;
import sopio.acha.domain.member.presentation.response.MemberTokenResponse;

@RestController
@Tag(name = "Member", description = "회원 관리")
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    @Operation(summary = "회원 기본 정보 조회 API", description = "홈 화면에서 보여줄 회원 기본 정보를 조회합니다.")
    public ResponseEntity<MemberBasicInformationResponse> getMemberInformation(
        @CurrentMember Member currentMember
    ) {
        MemberBasicInformationResponse response = memberService.getMemberBasicInformation(currentMember);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signin")
    @Operation(summary = "아차 로그인 API", description = "아차 서비스 회원 여부를 판단하고 로그인 합니다.")
    public ResponseEntity<MemberTokenResponse> validateIsAchaMemberAndLogin (
        @RequestBody MemberLoginRequest request
    ) {
        MemberTokenResponse response = memberService.validateIsAchaMemberAndLogin(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/student-data")
    @Operation(summary = "신규 회원 LMS 학생 정보 추출 API", description = "신규 회원 가입 시에 LMS에서 학생 정보를 추출합니다.")
    public ResponseEntity<MemberSummaryResponse> getNewMemberDataFromLMS(
        @RequestBody MemberRequest request
    ) {
        MemberSummaryResponse response = memberService.getNewMemberDataFromLMS(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/signup")
    @Operation(summary = "회원가입 및 자동 로그인 API", description = "데이터베이스에 회원 정보를 저장하고 가입 처리 및 자동 로그인 처리 합니다.")
    public ResponseEntity<MemberTokenResponse> saveMemberAndLogin(
        @RequestBody MemberSaveRequest request
    ) {
        MemberTokenResponse response = memberService.saveMemberAndLogin(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reissue")
    @Operation(summary = "액세스토큰 재발급 API", description = "리프레시토큰을 기반으로 액세스 토큰을 재발급 합니다.")
    public ResponseEntity<AccessTokenResponse> reissueAccessToken(
        @RequestBody RefreshTokenRequest request
    ) {
        AccessTokenResponse response = memberService.reissueAccessToken(request);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/signout")
    @Operation(summary = "아차 회원 탈퇴 API", description = "아차 계정을 비활성화하고 탈퇴합니다.")
    public ResponseEntity<Void> signOutAchaMember(
        @CurrentMember Member currentMember,
        @RequestBody MemberSignOutRequest request
    ) {
        memberService.signOutAchaMember(currentMember, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃 API", description = "현재 로그인된 회원을 로그아웃 처리하고 디바이스 토큰을 삭제합니다.")
    public ResponseEntity<Void> logoutMember(
        @CurrentMember Member currentMember,
        @RequestBody MemberLogoutRequest request
    ) {
        memberService.logoutMemberAndDeleteDeviceToken(currentMember, request);
        return ResponseEntity.ok().build();
    }

}
