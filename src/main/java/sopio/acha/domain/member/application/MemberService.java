package sopio.acha.domain.member.application;

import static sopio.acha.common.handler.ExtractorHandler.requestAuthentication;
import static sopio.acha.common.handler.ExtractorHandler.requestAuthenticationAndUserInfo;

import java.time.Duration;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import sopio.acha.common.auth.jwt.JwtCreator;
import sopio.acha.common.exception.ExtractorErrorException;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.exception.MemberNotAuthenticatedException;
import sopio.acha.domain.member.presentation.exception.MemberNotFoundException;
import sopio.acha.domain.member.presentation.response.MemberSummaryResponse;
import sopio.acha.domain.member.presentation.response.MemberTokenResponse;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final JwtCreator jwtCreator;

	public MemberSummaryResponse getMemberInformationFromExtractor() {
		Member currentMember = me();
		try {
			return new ObjectMapper().readValue(
				requestAuthenticationAndUserInfo(currentMember.getId(), currentMember.getPassword()), MemberSummaryResponse.class);
		} catch (JsonProcessingException e) {
			throw new ExtractorErrorException();
		}
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

	public Member getMemberById(String studentId) {
		return memberRepository.findMemberById(studentId)
			.orElseThrow(MemberNotFoundException::new);
	}

	private Member validateLoginMember(final String studentId, final String password) {
		if (!isExistMember(studentId)) {
			Member newMember = Member.createEmptyMember(studentId, password);
			memberRepository.save(newMember);
			return newMember;
		} else {
			Member existMember = getMemberById(studentId);
			existMember.updatePassword(password);
			return existMember;
		}
	}

	private boolean isExistMember(String studentId) {
		return memberRepository.existsById(studentId);
	}

	public Member me() {
		try {
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String userId = ((UserDetails)principal).getUsername();
			return getMemberById(userId);
		} catch (Exception e) {
			throw new MemberNotAuthenticatedException();
		}
	}
}
