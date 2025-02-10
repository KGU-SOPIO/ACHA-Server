package sopio.acha.domain.member.application;

import static sopio.acha.common.handler.ExtractorHandler.requestAuthentication;
import static sopio.acha.common.handler.ExtractorHandler.requestAuthenticationAndUserInfo;

import java.time.Duration;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.jwt.JwtCreator;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.exception.FailedParsingMemberDataException;
import sopio.acha.domain.member.presentation.exception.InvalidStudentIdOrPasswordException;
import sopio.acha.domain.member.presentation.exception.MemberAlreadyExistsException;
import sopio.acha.domain.member.presentation.exception.MemberNotFoundException;
import sopio.acha.domain.member.presentation.request.MemberBasicInformationRequest;
import sopio.acha.domain.member.presentation.request.MemberLoginRequest;
import sopio.acha.domain.member.presentation.response.MemberBasicInformationResponse;
import sopio.acha.domain.member.presentation.response.MemberSummaryResponse;
import sopio.acha.domain.member.presentation.response.MemberTokenResponse;

/**
 * [로그인 및 회원가입 플로우]
 * DB에 학번이 존재하는지 여부를 검사 (1)
 * DB에 학번이 없으면 -> 404 MEMBER_NOT_FOUND
 * 회원 가입 요청 API를 호출 POST -> 서버가 추출기에 인증+데이터추출(false) -> 데이터 확인 API GET(학번, 비번) 쿼리 파라미터-> 사용자가 OK PATCH
 * 만약 학번 비번이 달라서 실패하면 -> INVALID_STUDENT_ID_OR_PASSWORD 다시 1번으로 돌아감
 *
 * DB에 학번이 있으면 -> 서버가 추출기에 인증 실패하면 -> INVALID_STUDENT_ID_OR_PASSWORD 다시 1번으로 돌아감
 * 인증 성공하면 -> 회원 정보 업데이트 + 토큰 반환
 */

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final JwtCreator jwtCreator;

	@Transactional
	public MemberTokenResponse validateIsAchaMemberAndLogin(MemberLoginRequest request) {
		validateIsAchaMember(request.studentId());
		Member loginMember = validatePasswordAndGetMemberInfoFromExtractor(request.studentId(), request.password());
		return MemberTokenResponse.of(
			jwtCreator.generateToken(loginMember, Duration.ofHours(2)),
			jwtCreator.generateToken(loginMember, Duration.ofDays(7))
		);
	}

	public MemberSummaryResponse getMemberInformationFromExtractor(String studentId, String password) {
		try {
			return new ObjectMapper().readValue(
				requestAuthenticationAndUserInfo(studentId, password),
				MemberSummaryResponse.class);
		} catch (JsonProcessingException e) {
			throw new ExtractorErrorException();
		}
	}

	public Member validatePasswordAndGetMemberInfoFromExtractor(final String studentId, final String password) {
		JSONObject json = new JSONObject(requestAuthenticationAndUserInfo(studentId, password));
		if (!json.optBoolean("verification", false)) throw new InvalidStudentIdOrPasswordException();
		try {
			MemberSummaryResponse response = new ObjectMapper().readValue(
				json.getJSONObject("userData").toString(), MemberSummaryResponse.class);
			Member member = getMemberById(studentId);
			member.updateBasicInformation(response.name(), response.college(), response.department(), response.major());
			return member;
		} catch (JsonProcessingException e) {
			throw new FailedParsingMemberDataException();
		}
	}

	@Transactional(readOnly = true)
	public MemberBasicInformationResponse getMemberBasicInformation(Member currentMember) {
		return MemberBasicInformationResponse.from(currentMember);
	}

	@Transactional
	public MemberTokenResponse authenticateMemberAndGenerateToken(final String studentId, final String password) {
		requestAuthentication(studentId, password);
		Member loginMember = validateLoginMember(studentId, password);
		return MemberTokenResponse.of(
			jwtCreator.generateToken(loginMember, Duration.ofHours(2)),
			jwtCreator.generateToken(loginMember, Duration.ofDays(7))
		);
	}

	public void joinMember(MemberLoginRequest request) {
		validateIsMemberExists(request.studentId());
		Member newMember = Member.createEmptyMember(request.studentId(), request.password());
		memberRepository.save(newMember);
	}

	public void updateBasicMemberInformation(Member currentMember, MemberBasicInformationRequest request) {
		currentMember.updateBasicInformation(request.name(), request.college(), request.department(), request.major());
		memberRepository.save(currentMember);
	}

	public Member getMemberById(String studentId) {
		return memberRepository.findMemberById(studentId)
			.orElseThrow(MemberNotFoundException::new);
	}

	// private void updateBasicMemberInfoFromExtractor(Member currentMember) {
	// 	MemberSummaryResponse updatedInfo = getMemberInformationFromExtractor(currentMember);
	// 	currentMember.updateBasicInformation(updatedInfo.name(), updatedInfo.college(), updatedInfo.department(), updatedInfo.major());
	// 	memberRepository.save(currentMember);
	// }

	private Member validateLoginMember(final String studentId, final String password) {
		if (!isExistMember(studentId)) {
			throw new MemberNotFoundException();
		} else {
			Member existMember = getMemberById(studentId);
			existMember.updatePassword(password);
			// updateBasicMemberInfoFromExtractor(existMember);
			return existMember;
		}
	}

	private void validateIsAchaMember(final String studentId) {
		if(!isExistMember(studentId)) throw new MemberNotFoundException();
	}

	private void validateIsMemberExists(String studentId) {
		if (isExistMember(studentId)) throw new MemberAlreadyExistsException();
	}

	private boolean isExistMember(String studentId) {
		return memberRepository.existsById(studentId);
	}
}
