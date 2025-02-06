package sopio.acha.domain.member.application;

import static sopio.acha.common.handler.EncryptionHandler.decrypt;
import static sopio.acha.common.handler.ExtractorHandler.requestAuthentication;
import static sopio.acha.common.handler.ExtractorHandler.requestAuthenticationAndUserInfo;

import java.time.Duration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.application.RefreshTokenService;
import sopio.acha.common.auth.jwt.JwtCreator;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.exception.MemberNotFoundException;
import sopio.acha.domain.member.presentation.request.MemberBasicInformationRequest;
import sopio.acha.domain.member.presentation.response.MemberBasicInformationResponse;
import sopio.acha.domain.member.presentation.response.MemberSummaryResponse;
import sopio.acha.domain.member.presentation.response.MemberTokenResponse;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final JwtCreator jwtCreator;

	public MemberSummaryResponse getMemberInformationFromExtractor(Member currentMember) {
		try {
			return new ObjectMapper().readValue(
				requestAuthenticationAndUserInfo(currentMember.getId(),
					decrypt(currentMember.getPassword())),
				MemberSummaryResponse.class);
		} catch (JsonProcessingException e) {
			throw new ExtractorErrorException();
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
		String access = jwtCreator.generateToken(loginMember, Duration.ofHours(2));

		String refreshToken = refreshTokenService.getRefreshToken(studentId);
		if (refreshToken == null) {
			refreshToken = jwtCreator.generateToken(loginMember, Duration.ofDays(7));
			refreshTokenService.saveRefreshToken(studentId, refreshToken);
		}
		return MemberTokenResponse.of(access, refreshToken);
	}

	public void updateBasicMemberInformation(Member currentMember, MemberBasicInformationRequest request) {
		currentMember.updateBasicInformation(request.name(), request.college(), request.department(), request.major());
		memberRepository.save(currentMember);
	}

	public Member getMemberById(String studentId) {
		return memberRepository.findMemberById(studentId)
			.orElseThrow(MemberNotFoundException::new);
	}

	private void updateBasicMemberInfoFromExtractor(Member currentMember) {
		MemberSummaryResponse updatedInfo = getMemberInformationFromExtractor(currentMember);
		currentMember.updateBasicInformation(updatedInfo.name(), updatedInfo.college(), updatedInfo.department(), updatedInfo.major());
		memberRepository.save(currentMember);
	}

	private Member validateLoginMember(final String studentId, final String password) {
		if (!isExistMember(studentId)) {
			throw new MemberNotFoundException();
		} else {
			Member existMember = getMemberById(studentId);
			existMember.updatePassword(password);
			updateBasicMemberInfoFromExtractor(existMember);
			return existMember;
		}
	}

	private boolean isExistMember(String studentId) {
		return memberRepository.existsById(studentId);
	}
}
