package sopio.acha.domain.member.application;

import static sopio.acha.common.handler.ExtractorHandler.requestAuthenticationAndUserInfo;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sopio.acha.common.api.RestTemplateService;
import sopio.acha.common.api.dto.ResponseDto;
import sopio.acha.common.exception.ConvertErrorException;
import sopio.acha.common.handler.ExtractorHandler;
import sopio.acha.domain.member.domain.Member;
import sopio.acha.domain.member.infrastructure.MemberRepository;
import sopio.acha.domain.member.presentation.dto.MemberDto;
import sopio.acha.domain.member.presentation.response.MemberSummaryResponse;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final RestTemplateService restTemplateService;

	public MemberDto join(String id, String password) {
		Member member = memberRepository.findMemberById(id);

		if (member == null) {
			ResponseDto responseDto = restTemplateService.isExtract(id, password, true);

			if (responseDto.isVerification()) {
				MemberDto memberDto = new MemberDto();

				memberDto.setId(id);
				memberDto.setPassword(password);
				memberDto.setName(responseDto.getUserData().getName());
				memberDto.setCollege(responseDto.getUserData().getCollege());
				memberDto.setDepartment(responseDto.getUserData().getDepartment());
				memberDto.setMajor(responseDto.getUserData().getMajor());
				memberDto.setRole("ROLE_USER");

				Member newMember = Member.of(memberDto, bCryptPasswordEncoder);

				memberRepository.save(newMember);
				return memberDto;
			} else {
				throw new IllegalArgumentException("인증 실패: " + responseDto.getMessage());
			}
		}
		return MemberDto.of(member);
	}

	public Member me() {
		return memberRepository.findMemberById("202211516"); // 임시로 고정, 추후 SecurityContextHolder 에서 가져와야함
	}

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
}
