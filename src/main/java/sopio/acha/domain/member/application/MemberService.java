package sopio.acha.domain.member.application;

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
import sopio.acha.common.exception.ConvertErrorException;
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

	@Transactional
	public void saveMemberInfo(final String studentId, final String password) {
		try {
			MemberSummaryResponse response = new ObjectMapper().readValue(
				requestAuthenticationAndUserInfo(studentId, password), MemberSummaryResponse.class);
			Member newMember = Member.create(studentId, password, response.name(), response.college(),
				response.department(), response.major());
			memberRepository.save(newMember);
		} catch (JsonProcessingException e) {
			throw new ConvertErrorException();
		}
	}

	@Transactional
	public MemberTokenResponse authenticateMemberAndGenerateToken(final String studentId, final String password) {
		Member member = getMemberById(studentId);
		member.validatePassword(password);
		return MemberTokenResponse.of(
			jwtCreator.generateToken(member, Duration.ofHours(2)),
			jwtCreator.generateToken(member, Duration.ofDays(7))
		);
	}

	public Member getMemberById(String studentId) {
		return memberRepository.findMemberById(studentId)
			.orElseThrow(MemberNotFoundException::new);
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
