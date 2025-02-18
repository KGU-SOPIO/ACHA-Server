package sopio.acha.domain.member.application;

import static sopio.acha.common.handler.ExtractorHandler.requestAuthenticationAndUserInfo;

import java.time.Duration;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.jwt.JwtCreator;
import sopio.acha.domain.member.domain.AccessToken;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.domain.RefreshToken;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.exception.FailedParsingMemberDataException;
import sopio.acha.domain.member.presentation.exception.InvalidStudentIdOrPasswordException;
import sopio.acha.domain.member.presentation.exception.MemberNotAuthenticatedException;
import sopio.acha.domain.member.presentation.exception.MemberNotFoundException;
import sopio.acha.domain.member.presentation.request.MemberLoginRequest;
import sopio.acha.domain.member.presentation.request.MemberSaveRequest;
import sopio.acha.domain.member.presentation.request.MemberSignOutRequest;
import sopio.acha.domain.member.presentation.request.RefreshTokenRequest;
import sopio.acha.domain.member.presentation.response.AccessTokenResponse;
import sopio.acha.domain.member.presentation.response.MemberBasicInformationResponse;
import sopio.acha.domain.member.presentation.response.MemberSummaryResponse;
import sopio.acha.domain.member.presentation.response.MemberTokenResponse;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final JwtCreator jwtCreator;

	@Transactional(readOnly = true)
	public MemberBasicInformationResponse getMemberBasicInformation(Member currentMember) {
		return MemberBasicInformationResponse.from(currentMember);
	}

	public MemberTokenResponse validateIsAchaMemberAndLogin(MemberLoginRequest request) {
		validateIsAchaMember(request.studentId());
		Member loginMember = validatePasswordAndGetMemberInfoFromExtractor(request.studentId(), request.password());
		return issueAndSaveMemberToken(loginMember);
	}

	public MemberSummaryResponse getNewMemberDataFromLMS(MemberLoginRequest request) {
		JSONObject json = new JSONObject(requestAuthenticationAndUserInfo(request.studentId(), request.password()));
		if (!json.optBoolean("verification", false))
			throw new InvalidStudentIdOrPasswordException();
		try {
			return new ObjectMapper().readValue(
				json.getJSONObject("userData").toString(), MemberSummaryResponse.class);
		} catch (JsonProcessingException e) {
			throw new FailedParsingMemberDataException();
		}
	}

	public MemberTokenResponse saveMemberAndLogin(MemberSaveRequest request) {
		Member savedMember = memberRepository.save(
			Member.save(request.studentId(), request.password(), request.name(), request.college(),
				request.department(), request.major())
		);
		return issueAndSaveMemberToken(savedMember);
	}

	public Member validatePasswordAndGetMemberInfoFromExtractor(final String studentId, final String password) {
		JSONObject json = new JSONObject(requestAuthenticationAndUserInfo(studentId, password));
		if (!json.optBoolean("verification", false))
			throw new InvalidStudentIdOrPasswordException();
		try {
			MemberSummaryResponse response = new ObjectMapper().readValue(
				json.getJSONObject("userData").toString(), MemberSummaryResponse.class);
			Member member = getMemberById(studentId);
			member.updateBasicInformation(response.name(), response.college(), response.department(), response.major());
			return memberRepository.save(member);
		} catch (JsonProcessingException e) {
			throw new FailedParsingMemberDataException();
		}
	}

	public AccessTokenResponse reissueAccessToken(RefreshTokenRequest request) {
		RefreshToken refreshToken = refreshTokenService.getRefreshTokenObject(request);
		Member loginMember = getMemberById(refreshToken.getStudentId());
		if (loginMember.getDeletedAt() != null) throw new MemberNotAuthenticatedException();
		AccessToken accessToken = AccessToken.of(jwtCreator.generateToken(loginMember, Duration.ofHours(2)));
		return AccessTokenResponse.of(accessToken.getAccessToken());
	}

	public Member getMemberById(String studentId) {
		return memberRepository.findMemberById(studentId)
			.orElseThrow(MemberNotFoundException::new);
	}

	public void signOutAchaMember(Member currentMember, MemberSignOutRequest request) {
		currentMember.validatePassword(request.password());
		currentMember.delete();
		memberRepository.save(currentMember);
	}

	private MemberTokenResponse issueAndSaveMemberToken(Member member) {
		if (member.getDeletedAt() != null) throw new MemberNotAuthenticatedException();
		AccessToken accessToken = AccessToken.of(jwtCreator.generateToken(member, Duration.ofHours(2)));
		RefreshToken refreshToken = RefreshToken.of(member.getId(),
			jwtCreator.generateToken(member, Duration.ofDays(7)));
		refreshTokenService.saveRefreshToken(refreshToken);
		return MemberTokenResponse.of(accessToken.getAccessToken(), refreshToken.getRefreshToken());
	}

	private void validateIsAchaMember(final String studentId) {
		if (!isExistMember(studentId))
			throw new MemberNotFoundException();
	}

	private boolean isExistMember(String studentId) {
		return memberRepository.existsById(studentId);
	}
}
